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
import com.portfolio.generator.utilities.helpers.s3.IS3BucketHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActionProcessorTest {
  // Define the Environment as a Mockito mock object
  @Mock
  Environment env;
  @Mock
  private IIOFactory ioFactory;
  @Mock
  private ResourceLoader resourceLoader;
  private ActionProcessor actionProcessor;

  @BeforeEach
  public void setUp() {
    actionProcessor = new ActionProcessor(ioFactory, resourceLoader);
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
  public void testCopyResumeFromStaticAssets() throws ActionProcessingFailedException, IOException {
    final WebsiteDetailsModel websiteDetailsModel = new WebsiteDetailsModel();
    final ResumeModel resumeModel = new ResumeModel();
    resumeModel.setUUID("uuid");
    websiteDetailsModel.setResumeFile("some-resume.pdf");
    final ActionsModel action = new ActionsModel();
    action.setActionType(ActionType.COPY_RESUME_FROM_STATIC_ASSETS);
    action.setOutputLocation("out/resume.pdf");
    final StaticSiteRequestModel request = new StaticSiteRequestModel.Builder()
        .setWebsiteDetails(websiteDetailsModel)
        .setResume(resumeModel)
        .build();
    when(ioFactory.exists(any(Path.class))).thenReturn(true);
    actionProcessor.setStaticAssetsDirectory("static-sites");
    final ActionResultModel actionResult = actionProcessor.processAction(action, request);
    verify(ioFactory).copyFile(any(File.class), any(File.class));
    assertTrue(actionResult.getIsSuccessful());
  }

  @Test
  public void testCopyResumeFromStaticAssetsException() throws ActionProcessingFailedException, IOException {
    final WebsiteDetailsModel websiteDetailsModel = new WebsiteDetailsModel();
    final ResumeModel resumeModel = new ResumeModel();
    resumeModel.setUUID("uuid");
    websiteDetailsModel.setResumeFile("some-resume.pdf");
    final ActionsModel action = new ActionsModel();
    action.setActionType(ActionType.COPY_RESUME_FROM_STATIC_ASSETS);
    action.setOutputLocation("out/resume.pdf");
    final StaticSiteRequestModel request = new StaticSiteRequestModel.Builder()
            .setWebsiteDetails(websiteDetailsModel)
            .setResume(resumeModel)
            .build();
    when(ioFactory.exists(any(Path.class))).thenReturn(true);
    doThrow(new IOException()).when(ioFactory).copyFile(any(File.class), any(File.class));
    actionProcessor.setStaticAssetsDirectory("static-sites");
    final ActionResultModel actionResult = actionProcessor.processAction(action, request);
    assertFalse(actionResult.getIsSuccessful());
  }

  @Test
  public void testCopyResumeEmptyResumeFileName() throws ActionProcessingFailedException, IOException {
    final WebsiteDetailsModel websiteDetailsModel = new WebsiteDetailsModel();
    final ResumeModel resumeModel = new ResumeModel();
    resumeModel.setUUID("uuid");
    final ActionsModel action = new ActionsModel();
    action.setActionType(ActionType.COPY_RESUME_FROM_STATIC_ASSETS);
    action.setOutputLocation("out/resume.pdf");
    final StaticSiteRequestModel request = new StaticSiteRequestModel.Builder()
            .setWebsiteDetails(websiteDetailsModel)
            .setResume(resumeModel)
            .build();
    actionProcessor.setStaticAssetsDirectory("static-sites");
    final ActionResultModel actionResult = actionProcessor.processAction(action, request);
    verify(ioFactory, never()).copyFile(any(File.class), any(File.class));
    assertTrue(actionResult.getIsSuccessful());
  }

  @Test
  public void testCopyProfilePictureFromStaticAssets() throws ActionProcessingFailedException, IOException {
    final WebsiteDetailsModel websiteDetailsModel = new WebsiteDetailsModel();
    final ResumeModel resumeModel = new ResumeModel();
    resumeModel.setUUID("uuid");
    websiteDetailsModel.setProfilePictureFile("some-profile-picture.jpeg");
    final ActionsModel action = new ActionsModel();
    action.setActionType(ActionType.COPY_PROFILE_PICTURE_FROM_STATIC_ASSETS);
    action.setOutputLocation("out/profile-picture.jpeg");
    final StaticSiteRequestModel request = new StaticSiteRequestModel.Builder()
            .setWebsiteDetails(websiteDetailsModel)
            .setResume(resumeModel)
            .build();
    when(ioFactory.exists(any(Path.class))).thenReturn(true);
    actionProcessor.setStaticAssetsDirectory("static-sites");
    final ActionResultModel actionResult = actionProcessor.processAction(action, request);
    verify(ioFactory).copyFile(any(File.class), any(File.class));
    assertTrue(actionResult.getIsSuccessful());
  }

  @Test
  public void testCopyProfilePictureFromStaticAssetsException() throws ActionProcessingFailedException, IOException {
    final WebsiteDetailsModel websiteDetailsModel = new WebsiteDetailsModel();
    final ResumeModel resumeModel = new ResumeModel();
    resumeModel.setUUID("uuid");
    websiteDetailsModel.setProfilePictureFile("some-profile-picture.jpeg");
    final ActionsModel action = new ActionsModel();
    action.setActionType(ActionType.COPY_PROFILE_PICTURE_FROM_STATIC_ASSETS);
    action.setOutputLocation("out/profile-picture.jpeg");
    final StaticSiteRequestModel request = new StaticSiteRequestModel.Builder()
            .setWebsiteDetails(websiteDetailsModel)
            .setResume(resumeModel)
            .build();
    when(ioFactory.exists(any(Path.class))).thenReturn(true);
    doThrow(new IOException()).when(ioFactory).copyFile(any(File.class), any(File.class));
    actionProcessor.setStaticAssetsDirectory("static-sites");
    final ActionResultModel actionResult = actionProcessor.processAction(action, request);
    assertFalse(actionResult.getIsSuccessful());
  }

  @Test
  public void testCopyProfilePictureEmptyResumeFileName() throws ActionProcessingFailedException, IOException {
    final WebsiteDetailsModel websiteDetailsModel = new WebsiteDetailsModel();
    final ResumeModel resumeModel = new ResumeModel();
    resumeModel.setUUID("uuid");
    final ActionsModel action = new ActionsModel();
    action.setActionType(ActionType.COPY_PROFILE_PICTURE_FROM_STATIC_ASSETS);
    action.setOutputLocation("out/profile-picture.jpeg");
    final StaticSiteRequestModel request = new StaticSiteRequestModel.Builder()
            .setWebsiteDetails(websiteDetailsModel)
            .setResume(resumeModel)
            .build();
    actionProcessor.setStaticAssetsDirectory("static-sites");
    final ActionResultModel actionResult = actionProcessor.processAction(action, request);
    verify(ioFactory, never()).copyFile(any(File.class), any(File.class));
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