package com.portfolio.generator.utilities.processors;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3URI;
import com.portfolio.generator.models.*;
import com.portfolio.generator.models.staticsite.StaticSiteRequestModel;
import com.portfolio.generator.processors.ActionProcessor;
import com.portfolio.generator.processors.IActionProcessor;
import com.portfolio.generator.utilities.IO.IIOFactory;
import com.portfolio.generator.utilities.aws.factories.IAWSClientFactory;
import com.portfolio.generator.utilities.aws.factories.S3ObjectFactory;
import com.portfolio.generator.utilities.exceptions.ActionProcessingFailedException;
import com.portfolio.generator.utilities.exceptions.PortfolioGenerationFailedException;
import com.portfolio.generator.utilities.helpers.s3.IS3BucketHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActionProcessorTest {
  @Mock
  private S3ObjectFactory s3ObjectFactory;
  @Mock
  private IS3BucketHelper s3BucketHelper;
  @Mock
  private IIOFactory ioFactory;
  @Mock
  private IAWSClientFactory awsClientFactory;
  @Mock
  private ResourceLoader resourceLoader;
  private IActionProcessor actionProcessor;


  @BeforeEach
  public void setUp() {
    actionProcessor = new ActionProcessor(s3BucketHelper, s3ObjectFactory, ioFactory, awsClientFactory, resourceLoader);
  }

  @Test
  public void testDeleteFile() throws ActionProcessingFailedException {
    final ResumeModel resumeModel = new ResumeModel();
    resumeModel.setUUID("UUID");
    final StaticSiteRequestModel staticSiteRequest = new StaticSiteRequestModel.Builder().setResume(resumeModel).build();
    final ActionsModel action = new ActionsModel();
    action.setActionType(ActionType.DELETE_FILE);
    final Path mockPath = mock(Path.class);
    final File mockFile = mock(File.class);
    when(ioFactory.getPath(anyString())).thenReturn(mockPath);
    when(mockPath.toFile()).thenReturn(mockFile);
    when(mockFile.delete()).thenReturn(true);
    final ActionResultModel actionResult =
            actionProcessor.processAction(action, staticSiteRequest);
    assertTrue(actionResult.getIsSuccessful());
  }


  @Test
  public void testDeleteFileNullOrEmptyPath() {
    final ActionsModel action = new ActionsModel();
    action.setActionType(ActionType.DELETE_FILE);
    assertThrows(
        ActionProcessingFailedException.class,
        () -> actionProcessor.processAction(action, new StaticSiteRequestModel.Builder().build())
    );
    action.setInputLocation("");
    assertThrows(
        ActionProcessingFailedException.class,
        () -> actionProcessor.processAction(action, new StaticSiteRequestModel.Builder().build())
    );
  }

  @Test
  public void testCopyDirectoryNullOrEmptyPath() {
    final ActionsModel action = new ActionsModel();
    action.setActionType(ActionType.COPY_DIRECTORY);
    assertThrows(ActionProcessingFailedException.class, () -> {
      actionProcessor.processAction(action, new StaticSiteRequestModel.Builder().build());
    });
    action.setInputLocation("");
    assertThrows(ActionProcessingFailedException.class, () -> {
      actionProcessor.processAction(action, new StaticSiteRequestModel.Builder().build());
    });
  }

  @Test
  public void testDownloadResumeFromS3() throws ActionProcessingFailedException {
    final AmazonS3URI amazonS3URI = new AmazonS3URI("s3://test-bucket");
    final WebsiteDetailsModel websiteDetailsModel = new WebsiteDetailsModel();
    final ResumeModel resumeModel = new ResumeModel();
    resumeModel.setUUID("uuid");
    websiteDetailsModel.setResumeS3URI("uri");
    final ActionsModel action = new ActionsModel();
    action.setActionType(ActionType.DOWNLOAD_RESUME_FROM_S3);
    action.setOutputLocation("out");
    final StaticSiteRequestModel request = new StaticSiteRequestModel.Builder()
        .setWebsiteDetails(websiteDetailsModel)
        .setResume(resumeModel)
        .build();
    when(s3ObjectFactory.getAmazonS3URI(anyString())).thenReturn(amazonS3URI);
    s3BucketHelper.downloadFromS3Bucket(any(AmazonS3.class), anyString(), any(Path.class));
    final ActionResultModel actionResult = actionProcessor.processAction(action, request);
    assertTrue(actionResult.getIsSuccessful());
  }

  @Test
  public void testDownloadResumeFromException() throws ActionProcessingFailedException {
    final AmazonS3URI amazonS3URI = new AmazonS3URI("s3://test-bucket");
    final WebsiteDetailsModel websiteDetailsModel = new WebsiteDetailsModel();
    final ResumeModel resumeModel = new ResumeModel();
    resumeModel.setUUID("uuid");
    websiteDetailsModel.setResumeS3URI("uri");
    final ActionsModel action = new ActionsModel();
    action.setActionType(ActionType.DOWNLOAD_RESUME_FROM_S3);
    action.setOutputLocation("out");
    final StaticSiteRequestModel request = new StaticSiteRequestModel.Builder()
            .setWebsiteDetails(websiteDetailsModel)
            .setResume(resumeModel)
            .build();
    when(s3ObjectFactory.getAmazonS3URI(anyString())).thenReturn(amazonS3URI);
    doThrow(new RuntimeException()).when(s3BucketHelper).downloadFromS3Bucket(any(AmazonS3.class), anyString(), any(Path.class));
    final ActionResultModel actionResult = actionProcessor.processAction(action, request);
    assertFalse(actionResult.getIsSuccessful());
  }

  @Test
  public void testDownloadResumeFromS3BlankURI() throws ActionProcessingFailedException {
    final WebsiteDetailsModel websiteDetailsModel = new WebsiteDetailsModel();
    final ResumeModel resumeModel = new ResumeModel();
    resumeModel.setUUID("uuid");
    websiteDetailsModel.setResumeS3URI("");
    final ActionsModel action = new ActionsModel();
    action.setActionType(ActionType.DOWNLOAD_RESUME_FROM_S3);
    action.setOutputLocation("out");
    final StaticSiteRequestModel request = new StaticSiteRequestModel.Builder()
            .setWebsiteDetails(websiteDetailsModel)
            .setResume(resumeModel)
            .build();
    final ActionResultModel actionResult = actionProcessor.processAction(action, request);
    assertTrue(actionResult.getIsSuccessful());
  }

  @Test
  public void testDownloadProfilePictureFromS3() throws ActionProcessingFailedException {
    final AmazonS3URI amazonS3URI = new AmazonS3URI("s3://test-bucket");
    final WebsiteDetailsModel websiteDetailsModel = new WebsiteDetailsModel();
    final ResumeModel resumeModel = new ResumeModel();
    resumeModel.setUUID("uuid");
    websiteDetailsModel.setProfilePictureS3URI("uri");
    final ActionsModel action = new ActionsModel();
    action.setActionType(ActionType.DOWNLOAD_PROFILE_PICTURE_FROM_S3);
    action.setOutputLocation("out");
    final StaticSiteRequestModel request = new StaticSiteRequestModel.Builder()
            .setWebsiteDetails(websiteDetailsModel)
            .setResume(resumeModel)
            .build();
    when(s3ObjectFactory.getAmazonS3URI(anyString())).thenReturn(amazonS3URI);
    s3BucketHelper.downloadFromS3Bucket(any(AmazonS3.class), anyString(), any(Path.class));
    final ActionResultModel actionResult = actionProcessor.processAction(action, request);
    assertTrue(actionResult.getIsSuccessful());
  }

  @Test
  public void testDownloadProfilePictureFromS3BlankURI() throws ActionProcessingFailedException {
    final AmazonS3URI amazonS3URI = new AmazonS3URI("s3://test-bucket");
    final WebsiteDetailsModel websiteDetailsModel = new WebsiteDetailsModel();
    final ResumeModel resumeModel = new ResumeModel();
    resumeModel.setUUID("uuid");
    websiteDetailsModel.setProfilePictureS3URI("");
    final ActionsModel action = new ActionsModel();
    action.setActionType(ActionType.DOWNLOAD_PROFILE_PICTURE_FROM_S3);
    action.setOutputLocation("out");
    final StaticSiteRequestModel request = new StaticSiteRequestModel.Builder()
            .setWebsiteDetails(websiteDetailsModel)
            .setResume(resumeModel)
            .build();
    final ActionResultModel actionResult = actionProcessor.processAction(action, request);
    assertTrue(actionResult.getIsSuccessful());
  }

  @Test
  public void testCopyDirectory() throws ActionProcessingFailedException, IOException {
    final ResumeModel resumeModel = new ResumeModel();
    resumeModel.setUUID("UUID");
    final StaticSiteRequestModel staticSiteRequest = new StaticSiteRequestModel.Builder().setResume(resumeModel).build();
    final Path mockPath = mock(Path.class);
    final File mockFile = mock(File.class);
    final Resource mockResource = mock(Resource.class);
    final ActionsModel action = new ActionsModel();
    action.setActionType(ActionType.COPY_DIRECTORY);
    action.setInputLocation("/somepathinput");
    action.setInputLocation("/somepathoutput");
    when(ioFactory.getPath(anyString())).thenReturn(mockPath);
    when(mockPath.toFile()).thenReturn(mockFile);
    when(resourceLoader.getResource(anyString())).thenReturn(mockResource);
    when(mockResource.getFile()).thenReturn(mockFile);
    ioFactory.copyDirectory(any(File.class), any(File.class));
    final ActionResultModel actionResult = actionProcessor.processAction(action, staticSiteRequest);
    assertTrue(actionResult.getIsSuccessful());
  }

  @Test
  public void testCopyDirectoryResourcePathException() throws IOException {
    final ResumeModel resumeModel = new ResumeModel();
    resumeModel.setUUID("UUID");
    final StaticSiteRequestModel staticSiteRequest = new StaticSiteRequestModel.Builder().setResume(resumeModel).build();
    final Resource mockResource = mock(Resource.class);
    final ActionsModel action = new ActionsModel();
    action.setActionType(ActionType.COPY_DIRECTORY);
    action.setInputLocation("/somepathinput");
    action.setInputLocation("/somepathoutput");
    when(resourceLoader.getResource(anyString())).thenReturn(mockResource);
    when(mockResource.getFile()).thenThrow(new IOException());
    assertThrows(
            ActionProcessingFailedException.class,
            () -> actionProcessor.processAction(action, staticSiteRequest)
    );
  }



}