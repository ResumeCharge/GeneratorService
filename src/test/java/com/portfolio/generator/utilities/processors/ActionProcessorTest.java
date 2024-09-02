package com.portfolio.generator.utilities.processors;

import com.portfolio.generator.models.*;
import com.portfolio.generator.models.staticsite.StaticSiteRequestModel;
import com.portfolio.generator.processors.ActionProcessor;
import com.portfolio.generator.utilities.IO.IIOFactory;
import com.portfolio.generator.utilities.exceptions.ActionProcessingFailedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActionProcessorTest {
  @Mock
  private IIOFactory ioFactory;
  private ActionProcessor actionProcessor;

  @BeforeEach
  public void setUp() {
    actionProcessor = new ActionProcessor(ioFactory);
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
    actionProcessor.setUserWebsiteAssetsLocation("static-sites");
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
    actionProcessor.setUserWebsiteAssetsLocation("static-sites");
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
    actionProcessor.setUserWebsiteAssetsLocation("static-sites");
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
    actionProcessor.setUserWebsiteAssetsLocation("static-sites");
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
    actionProcessor.setUserWebsiteAssetsLocation("static-sites");
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
    actionProcessor.setUserWebsiteAssetsLocation("static-sites");
    final ActionResultModel actionResult = actionProcessor.processAction(action, request);
    verify(ioFactory, never()).copyFile(any(File.class), any(File.class));
    assertTrue(actionResult.getIsSuccessful());
  }

  @Test
  public void testCopyDirectory() throws ActionProcessingFailedException, IOException {
    actionProcessor.setStaticAssetsLocation("./assets");
    final ResumeModel resumeModel = new ResumeModel();
    resumeModel.setUUID("UUID");
    final StaticSiteRequestModel staticSiteRequest = new StaticSiteRequestModel.Builder().setResume(resumeModel).build();
    final Path mockPath = mock(Path.class);
    final File mockFile = mock(File.class);
    final ActionsModel action = new ActionsModel();
    action.setActionType(ActionType.COPY_DIRECTORY);
    action.setInputLocation("/somepathinput");
    action.setOutputLocation("/somepathoutput");
    when(ioFactory.getPath(anyString())).thenReturn(mockPath);
    when(mockPath.toFile()).thenReturn(mockFile);
    ioFactory.copyDirectory(any(File.class), any(File.class));
    final ActionResultModel actionResult = actionProcessor.processAction(action, staticSiteRequest);
    assertTrue(actionResult.getIsSuccessful());
  }

}