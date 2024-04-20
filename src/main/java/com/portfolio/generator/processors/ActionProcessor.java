package com.portfolio.generator.processors;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3URI;
import com.portfolio.generator.models.ActionResultModel;
import com.portfolio.generator.models.ActionsModel;
import com.portfolio.generator.models.staticsite.StaticSiteRequestModel;
import com.portfolio.generator.utilities.IO.IIOFactory;
import com.portfolio.generator.utilities.aws.factories.IAWSClientFactory;
import com.portfolio.generator.utilities.aws.factories.IS3ObjectFactory;
import com.portfolio.generator.utilities.exceptions.ActionProcessingFailedException;
import com.portfolio.generator.utilities.exceptions.PortfolioGenerationFailedException;
import com.portfolio.generator.utilities.helpers.s3.IS3BucketHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * Processes actions defined in generator-config.json. Actions should return
 * ActionResultModel unless there is an exception that we can't handle
 * and need to bubble up.
 */
@Component
public class ActionProcessor implements IActionProcessor {
  private static final String INVALID_ACTION_TYPE_ERROR =
      "Invalid Action Type Provided, Action Type: %s is invalid";
  private static final String ACTION_PROCESSING_EXCEPTION = "Unable to process action %s";
  private static final Logger logger = LoggerFactory.getLogger(ActionProcessor.class);
  private final IS3BucketHelper s3BucketHelper;
  private final IS3ObjectFactory S3ObjectFactory;
  private final IIOFactory ioFactory;
  private final IAWSClientFactory awsClientFactory;
  private final ResourceLoader resourceLoader;

  @Value("${resources.output.root}")
  private String resourceOutputRoot;

  public ActionProcessor(final IS3BucketHelper s3BucketHelper,
                         final IS3ObjectFactory S3ObjectFactory,
                         final IIOFactory ioFactory,
                         final IAWSClientFactory awsClientFactory,
                         final ResourceLoader resourceLoader) {
    this.s3BucketHelper = s3BucketHelper;
    this.S3ObjectFactory = S3ObjectFactory;
    this.ioFactory = ioFactory;
    this.awsClientFactory = awsClientFactory;
    this.resourceLoader = resourceLoader;
  }

  @Override
  public ActionResultModel processAction(
      final ActionsModel action, final StaticSiteRequestModel staticSiteRequestManager
  ) throws ActionProcessingFailedException {
    final ActionResultModel actionResult;
    try {
      switch (action.getActionType()) {
        case DELETE_FILE:
          actionResult = deleteFile(action, staticSiteRequestManager);
          break;
        case COPY_DIRECTORY:
          actionResult = copyDirectory(action, staticSiteRequestManager);
          break;
        case DOWNLOAD_RESUME_FROM_S3:
          actionResult = downloadResumeFromS3(action, staticSiteRequestManager);
          break;
        case DOWNLOAD_PROFILE_PICTURE_FROM_S3:
          actionResult = downloadProfilePictureFromS3(action, staticSiteRequestManager);
          break;
        default:
          throw new IllegalArgumentException(
              String.format(INVALID_ACTION_TYPE_ERROR, action.getActionType()));
      }
      return actionResult;
    } catch (final Exception e) {
      logger.error("failure in action processor", e);
      throw new ActionProcessingFailedException(
          String.format(ACTION_PROCESSING_EXCEPTION, action.getActionType()), e);
    }
  }

  private ActionResultModel deleteFile(
      final ActionsModel action, final StaticSiteRequestModel staticSiteRequestManager
  ) {
    final String filePath = getFilePath(action.getInputLocation(),
        staticSiteRequestManager.resume.getUUID());
    Validate.notBlank(filePath);
    final File fileToDelete = getFileFromFilePathString(filePath);
    final boolean isSuccessful = fileToDelete.delete();
    return new ActionResultModel.Builder()
        .setIsSuccessful(isSuccessful)
        .build();
  }

  private ActionResultModel copyDirectory(
      final ActionsModel action, final StaticSiteRequestModel staticSiteRequest
  )
      throws IOException, PortfolioGenerationFailedException {
    final File inputDirectory = getDirectoryAsFileFromClasspath(action.getInputLocation());
    final String outputDirectory = getFilePath(action.getOutputLocation(),
        staticSiteRequest.resume.getUUID()
    );
    Validate.notNull(inputDirectory);
    Validate.notBlank(outputDirectory);
    final File outputDirectoryFile = getFileFromFilePathString(outputDirectory);
    ioFactory.copyDirectory(inputDirectory, outputDirectoryFile);
    return new ActionResultModel.Builder()
        .setIsSuccessful(true)
        .build();
  }

  private ActionResultModel downloadResumeFromS3(
      final ActionsModel action, final StaticSiteRequestModel staticSiteRequestManager
  ) {
    final String uri = staticSiteRequestManager.websiteDetails.getResumeS3URI();
    if (StringUtils.isBlank(uri)) {
      logger.error("Object URI was blank, not downloading from S3");
      return new ActionResultModel.Builder()
          .setIsSuccessful(true)
          .build();
    }
    final String outputLocation = getFilePath(
        action.getOutputLocation(),
        staticSiteRequestManager.resume.getUUID()
    );
    return downloadFromS3(uri, outputLocation);
  }

  private ActionResultModel downloadProfilePictureFromS3(
      final ActionsModel action, final StaticSiteRequestModel staticSiteRequestManager
  ) {
    final String uri = staticSiteRequestManager.websiteDetails.getProfilePictureS3URI();
    if (StringUtils.isBlank(uri)) {
      logger.error("Object URI was blank, not downloading from S3");
      return new ActionResultModel.Builder()
          .setIsSuccessful(true)
          .build();
    }
    final String outputLocation = getFilePath(
        action.getOutputLocation(),
        staticSiteRequestManager.resume.getUUID()
    );
    return downloadFromS3(uri, outputLocation);
  }


  /**
   * Downloads the specified object from the S3 bucket.
   * Retry 3 times on error
   */
  private ActionResultModel downloadFromS3(
      final String amazonS3URIAsString, final String outputLocation
  ) {
    final ProfileCredentialsProvider profileCredentialsProvider = new ProfileCredentialsProvider("generator-service");
    final AmazonS3 s3Client = awsClientFactory.getS3Client(profileCredentialsProvider, Regions.US_EAST_1);
    for (int retry = 0; retry < 3; retry++) {
      try {
        final AmazonS3URI objectUri = S3ObjectFactory.getAmazonS3URI(amazonS3URIAsString);
        final String objectKey = objectUri.getKey();
        final Path outputDirectoryPath = Paths.get(outputLocation);
        s3BucketHelper.downloadFromS3Bucket(s3Client, objectKey, outputDirectoryPath);
        return new ActionResultModel.Builder()
            .setIsSuccessful(true)
            .build();
      } catch (final Exception e) {
        logger.error(e.toString(), e);
      }
    }
    logger.error("Unable to download from S3 after 3 attempts, aborting");
    return new ActionResultModel.Builder()
        .setIsSuccessful(false)
        .build();
  }

  private File getFileFromFilePathString(final String filePath) {
    Validate.notBlank(filePath);
    final Path outputFilePath = ioFactory.getPath(filePath);
    return outputFilePath.toFile();
  }

  private String getFilePath(final String location, final String... args) {
    return String.format(resourceOutputRoot + "/" + location, args);
  }

  private File getDirectoryAsFileFromClasspath(final String resourcePath) throws PortfolioGenerationFailedException {
    try {
      final Resource resource = resourceLoader.getResource("classpath:" + resourcePath);
      return resource.getFile();
    } catch (IOException e) {
      final String errorMessage = "Exception trying to get directory from classpath";
      logger.error(errorMessage, e);
      throw new PortfolioGenerationFailedException(errorMessage, e);
    }
  }
}
