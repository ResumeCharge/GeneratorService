package com.portfolio.generator.processors;

import com.portfolio.generator.models.ActionResultModel;
import com.portfolio.generator.models.ActionsModel;
import com.portfolio.generator.models.staticsite.StaticSiteRequestModel;
import com.portfolio.generator.utilities.IO.IIOFactory;
import com.portfolio.generator.utilities.exceptions.ActionProcessingFailedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
  private final IIOFactory ioFactory;

  @Value("${RESOURCES_OUTPUT_ROOT}")
  private String resourceOutputRoot;

  @Value("${USER_WEBSITE_ASSETS_LOCATION}")
  private String userWebsiteAssetsLocation;

  @Value("${STATIC_ASSETS_LOCATION:./assets}")
  private String staticAssetsLocation;

  public ActionProcessor(final IIOFactory ioFactory) {
    this.ioFactory = ioFactory;
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
        case COPY_RESUME_FROM_STATIC_ASSETS:
          actionResult = copyResumeFromStaticAssets(action, staticSiteRequestManager);
          break;
        case COPY_PROFILE_PICTURE_FROM_STATIC_ASSETS:
          actionResult = copyProfilePictureFromStaticAssets(action, staticSiteRequestManager);
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
          throws IOException {
    final Path inputDirectory = Paths.get(staticAssetsLocation, action.getInputLocation());
    final String outputDirectory = getFilePath(action.getOutputLocation(),
            staticSiteRequest.resume.getUUID()
    );
    Validate.notNull(inputDirectory);
    Validate.notBlank(outputDirectory);
    final File outputDirectoryFile = getFileFromFilePathString(outputDirectory);
    ioFactory.copyDirectory(inputDirectory.toFile(), outputDirectoryFile);
    return new ActionResultModel.Builder()
            .setIsSuccessful(true)
            .build();
  }

  private ActionResultModel copyResumeFromStaticAssets(
          final ActionsModel action, final StaticSiteRequestModel staticSiteRequestManager
  ) {
    final String resumeFileName = staticSiteRequestManager.websiteDetails.getResumeFile();
    if (StringUtils.isBlank(resumeFileName)) {
      logger.info("resumeFileName was blank, not copying");
      return new ActionResultModel.Builder()
              .setIsSuccessful(true)
              .build();
    }
    final String outputLocation = getFilePath(
            action.getOutputLocation(),
            staticSiteRequestManager.resume.getUUID()
    );
    boolean successful = copyFileFromUserWebsiteAssets(resumeFileName, outputLocation);
    return new ActionResultModel.Builder()
            .setIsSuccessful(successful)
            .build();
  }

  private ActionResultModel copyProfilePictureFromStaticAssets(
          final ActionsModel action, final StaticSiteRequestModel staticSiteRequestManager
  ) {
    final String profilePictureFileName = staticSiteRequestManager.websiteDetails.getProfilePictureFile();
    if (StringUtils.isBlank(profilePictureFileName)) {
      logger.info("profilePictureFileName was blank, not copying");
      return new ActionResultModel.Builder()
              .setIsSuccessful(true)
              .build();
    }
    final String outputLocation = getFilePath(
            action.getOutputLocation(),
            staticSiteRequestManager.resume.getUUID()
    );
    boolean successful = copyFileFromUserWebsiteAssets(profilePictureFileName, outputLocation);
    return new ActionResultModel.Builder()
            .setIsSuccessful(successful)
            .build();
  }

  private boolean copyFileFromUserWebsiteAssets(final String inputFileName, final String outputFileLocation) {
    if (userWebsiteAssetsLocation == null) {
      logger.error("userWebsiteAssets directory not configured, not able to copy from static assets");
      return true;
    }
    final Path inputFilePath = Paths.get(userWebsiteAssetsLocation, inputFileName);
    final Path outputFilePath = Paths.get(outputFileLocation);
    return copyFile(inputFilePath, outputFilePath);
  }


  private boolean copyFile(final Path inputPath, final Path outputPath) {
    if (!ioFactory.exists(inputPath)) {
      logger.error(String.format("Input file %s not found, ignoring and continuing", inputPath));
      return true;
    }
    try {
      ioFactory.copyFile(inputPath.toFile(), outputPath.toFile());
      return true;
    } catch (final IOException e) {
      logger.error(String.format("Error trying to copy file %s to %s, ignoring and continuing", inputPath, outputPath));
      return false;
    }
  }

  private File getFileFromFilePathString(final String filePath) {
    Validate.notBlank(filePath);
    final Path outputFilePath = ioFactory.getPath(filePath);
    return outputFilePath.toFile();
  }

  private String getFilePath(final String location, final String... args) {
    return String.format(resourceOutputRoot + "/" + location, args);
  }

  public String getResourceOutputRoot() {
    return resourceOutputRoot;
  }

  public void setResourceOutputRoot(String resourceOutputRoot) {
    this.resourceOutputRoot = resourceOutputRoot;
  }

  public String getUserWebsiteAssetsLocation() {
    return userWebsiteAssetsLocation;
  }

  public void setUserWebsiteAssetsLocation(String userWebsiteAssetsLocation) {
    this.userWebsiteAssetsLocation = userWebsiteAssetsLocation;
  }

  public String getStaticAssetsLocation() {
    return staticAssetsLocation;
  }

  public void setStaticAssetsLocation(String staticAssetsLocation) {
    this.staticAssetsLocation = staticAssetsLocation;
  }
}
