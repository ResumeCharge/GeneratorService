package com.portfolio.generator.deployments.jobs;

import com.mongodb.MongoException;
import com.portfolio.generator.deployments.status.IPendingDeploymentDeployActionProcessor;
import com.portfolio.generator.deployments.status.StatusPollerResult;
import com.portfolio.generator.managers.GithubPortfolioGenerationTaskManager;
import com.portfolio.generator.models.databaseModels.DbDeploymentModel;
import com.portfolio.generator.models.databaseModels.DbPendingDeploymentModel;
import com.portfolio.generator.models.databaseModels.DbResumeModel;
import com.portfolio.generator.models.databaseModels.DeploymentStatus;
import com.portfolio.generator.models.staticsite.DeploymentProvider;
import com.portfolio.generator.services.UserService;
import com.portfolio.generator.services.database.IDatabaseService;
import com.portfolio.generator.utilities.factories.IDateTimeFactory;
import com.portfolio.generator.utilities.helpers.DeploymentStatusType;
import com.portfolio.generator.utilities.helpers.IDeploymentStatusHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PendingDeploymentSweeperJobTest {
  private static final ZoneId TIMEZONE_ID = ZoneId.of("Z");
  private static final int MAX_RETRIES = 3;
  @Mock
  private GithubPortfolioGenerationTaskManager githubPortfolioGenerationTaskManager;
  @Mock
  private IDatabaseService databaseService;
  @Mock
  private UserService userService;
  @Mock
  private IDateTimeFactory dateTimeFactory;
  @Mock
  private IDeploymentStatusHelper deploymentStatusHelper;
  @Mock
  private IPendingDeploymentDeployActionProcessor deploymentStatusPollerScheduler;
  private IJob pendingDeploymentSweeperJob;

  @BeforeEach
  void setUp() {
    pendingDeploymentSweeperJob =
        new PendingDeploymentSweeperJob(
            githubPortfolioGenerationTaskManager,
            databaseService,
            userService,
            dateTimeFactory,
            deploymentStatusHelper,
            deploymentStatusPollerScheduler);
  }

  @Test
  void runWithPendingDeployment_github() throws IOException {
    final DbPendingDeploymentModel pendingDeploymentModel = new DbPendingDeploymentModel();
    pendingDeploymentModel.set_id("635d6be57c842c8265a3b0c4");
    final DbDeploymentModel deploymentModel = getBasicDbDeploymentGitHub();
    deploymentModel.setStatus(DeploymentStatus.PENDING);
    when(databaseService.getDeployments(anyList())).thenReturn(List.of(deploymentModel));
    when(databaseService.getResume("123")).thenReturn(mock(DbResumeModel.class));
    when(databaseService.getPendingDeployments()).thenReturn(List.of(pendingDeploymentModel));
    when(userService.getUserOAuthToken(anyString())).thenReturn("token");
    pendingDeploymentSweeperJob.run();
    verify(githubPortfolioGenerationTaskManager).schedulePortfolioGenerationTask(any(PendingDeploymentSweeperAction.class));
    verify(dateTimeFactory).getLocalDateTimeNow(ZoneId.of("Z"));
  }

  @Test
  void runWithInProgressDeployment() {
    final DbPendingDeploymentModel pendingDeploymentModel = new DbPendingDeploymentModel();
    final DbDeploymentModel deploymentModel = getBasicDbDeploymentGitHub();
    deploymentModel.setStatus(DeploymentStatus.PROCESSING);
    when(databaseService.getDeployments(anyList())).thenReturn(List.of(deploymentModel));
    when(databaseService.getPendingDeployments()).thenReturn(List.of(pendingDeploymentModel));
    pendingDeploymentSweeperJob.run();
    verify(githubPortfolioGenerationTaskManager, never()).schedulePortfolioGenerationTask(any(PendingDeploymentSweeperAction.class));
  }

  @Test
  void runWithInProgressDeployment_ProcessingTooLong() throws IOException {
    final Future future = mock(Future.class);
    final LocalDateTime now = LocalDateTime.now(ZoneId.of("Z"));
    final DbPendingDeploymentModel pendingDeploymentModel = new DbPendingDeploymentModel();
    final DbDeploymentModel deploymentModel = getBasicDbDeploymentGitHub();
    deploymentModel.setStatus(DeploymentStatus.PENDING);
    when(databaseService.getDeployments(anyList()))
        .thenReturn(List.of(deploymentModel));
    when(databaseService.getResume("123")).thenReturn(mock(DbResumeModel.class));
    when(databaseService.getPendingDeployments()).thenReturn(List.of(pendingDeploymentModel));
    when(dateTimeFactory.getLocalDateTimeNow(any(ZoneId.class)))
        .thenReturn(now.minus(Duration.ofMinutes(15)))
        .thenReturn(now);
    when(userService.getUserOAuthToken(anyString())).thenReturn("oauth-token");
    when(githubPortfolioGenerationTaskManager.schedulePortfolioGenerationTask(any(PendingDeploymentSweeperAction.class)))
            .thenReturn(future);
    when(future.isDone()).thenReturn(false);
    when(future.cancel(true)).thenReturn(true);
    pendingDeploymentSweeperJob.run();
    pendingDeploymentSweeperJob.run();
    verify(future).cancel(true);
    verify(databaseService, times(2)).updateDeploymentRetryCount(anyString(), eq(1));
  }

  @Test
  void runWithInProgressDeployment_RetryCountUpdated() throws IOException {
    final Future future = mock(Future.class);
    final LocalDateTime now = LocalDateTime.now(ZoneId.of("Z"));
    final DbPendingDeploymentModel pendingDeploymentModel = new DbPendingDeploymentModel();
    final DbDeploymentModel deploymentModel = getBasicDbDeploymentGitHub();
    deploymentModel.setStatus(DeploymentStatus.PENDING);
    final DbDeploymentModel deploymentModel2 = getBasicDbDeploymentGitHub();
    deploymentModel2.setStatus(DeploymentStatus.PENDING);
    deploymentModel2.setRetryCount(1);
    when(databaseService.getDeployments(anyList()))
        .thenReturn(List.of(deploymentModel))
        .thenReturn(List.of(deploymentModel2));
    when(userService.getUserOAuthToken(anyString())).thenReturn("oauth-token");
    when(databaseService.getResume("123")).thenReturn(mock(DbResumeModel.class));
    when(databaseService.getPendingDeployments()).thenReturn(List.of(pendingDeploymentModel));
    when(dateTimeFactory.getLocalDateTimeNow(any(ZoneId.class)))
        .thenReturn(now.minus(Duration.ofMinutes(15)))
        .thenReturn(now);
    when(githubPortfolioGenerationTaskManager.schedulePortfolioGenerationTask(any(PendingDeploymentSweeperAction.class)))
        .thenReturn(future);
    when(future.isDone()).thenReturn(false);
    when(future.cancel(true)).thenReturn(true);
    pendingDeploymentSweeperJob.run();
    pendingDeploymentSweeperJob.run();

    verify(future).cancel(true);
    verify(databaseService).updateDeploymentRetryCount(anyString(), eq(1));
    verify(databaseService).updateDeploymentRetryCount(anyString(), eq(2));
  }

  @Test
  void runWithInProgressDeployment_ProcessingTimeNotPastThreshold() throws IOException {
    final Future future = mock(Future.class);
    final LocalDateTime now = LocalDateTime.now(ZoneId.of("Z"));
    final DbPendingDeploymentModel pendingDeploymentModel = new DbPendingDeploymentModel();
    final DbDeploymentModel deploymentModel = getBasicDbDeploymentGitHub();
    when(databaseService.getDeployments(anyList())).thenReturn(List.of(deploymentModel));
    when(databaseService.getResume("123")).thenReturn(mock(DbResumeModel.class));
    when(databaseService.getPendingDeployments())
        .thenReturn(List.of(pendingDeploymentModel));
    when(userService.getUserOAuthToken(anyString())).thenReturn("oauth-token");
    when(dateTimeFactory.getLocalDateTimeNow(any(ZoneId.class)))
        .thenReturn(now.minus(Duration.ofMinutes(9)))
        .thenReturn(now);
    when(githubPortfolioGenerationTaskManager.schedulePortfolioGenerationTask(any(PendingDeploymentSweeperAction.class)))
        .thenReturn(future);
    when(future.isDone()).thenReturn(false);
    pendingDeploymentSweeperJob.run();
    pendingDeploymentSweeperJob.run();
    verify(future, never()).cancel(true);
    verify(databaseService).updateDeploymentRetryCount(anyString(), eq(1));
  }

  @Test
  void runWithInProgressDeployment_TaskFinished() throws ExecutionException, InterruptedException, IOException {
    final Future future = mock(Future.class);
    final PendingDeploymentSweeperAction request = new PendingDeploymentSweeperAction.Builder()
        .setPendingDeploymentId("635d6be57c842c8265a3b0c4")
        .setDeploymentId("635d6be57c842c8265a3b0c4")
        .build();
    final LocalDateTime now = LocalDateTime.now(ZoneId.of("Z"));
    final DbPendingDeploymentModel pendingDeploymentModel = new DbPendingDeploymentModel();
    final DbDeploymentModel deploymentModel = getBasicDbDeploymentGitHub();
    when(databaseService.getDeployments(anyList()))
        .thenReturn(List.of(deploymentModel))
        .thenReturn(Collections.emptyList());
    when(databaseService.getResume("123")).thenReturn(mock(DbResumeModel.class));
    when(databaseService.getPendingDeployments())
        .thenReturn(List.of(pendingDeploymentModel))
        .thenReturn(Collections.emptyList());
    when(dateTimeFactory.getLocalDateTimeNow(any(ZoneId.class)))
        .thenReturn(now.minus(Duration.ofMinutes(9)))
        .thenReturn(now);
    when(userService.getUserOAuthToken(anyString())).thenReturn("oauth-token");
    when(githubPortfolioGenerationTaskManager.schedulePortfolioGenerationTask(any(PendingDeploymentSweeperAction.class)))
            .thenReturn(future);
    when(future.isDone()).thenReturn(true);
    when(future.get()).thenReturn(request);

    pendingDeploymentSweeperJob.run();
    pendingDeploymentSweeperJob.run();

    verify(future, never()).cancel(true);
    verify(databaseService).updateDeploymentRetryCount(anyString(), eq(1));
  }

  @Test
  void runWithInProgressDeployment_TaskFinishedExceptionDeletingTask() throws ExecutionException, InterruptedException, IOException {
    final Future future = mock(Future.class);
    final PendingDeploymentSweeperAction request = new PendingDeploymentSweeperAction.Builder()
        .setPendingDeploymentId("635d6be57c842c8265a3b0c4")
        .setDeploymentId("635d6be57c842c8265a3b0c4")
        .build();
    final LocalDateTime now = LocalDateTime.now(ZoneId.of("Z"));
    final DbPendingDeploymentModel pendingDeploymentModel = new DbPendingDeploymentModel();
    final DbDeploymentModel deploymentModel = getBasicDbDeploymentGitHub();
    when(databaseService.getDeployments(anyList()))
        .thenReturn(List.of(deploymentModel))
        .thenReturn(Collections.emptyList());
    when(databaseService.getResume("123")).thenReturn(mock(DbResumeModel.class));
    when(databaseService.getPendingDeployments())
        .thenReturn(List.of(pendingDeploymentModel))
        .thenReturn(Collections.emptyList());
    when(dateTimeFactory.getLocalDateTimeNow(any(ZoneId.class)))
        .thenReturn(now.minus(Duration.ofMinutes(9)))
        .thenReturn(now);
    when(userService.getUserOAuthToken(anyString())).thenReturn("oauth-token");
    when(githubPortfolioGenerationTaskManager.schedulePortfolioGenerationTask(any(PendingDeploymentSweeperAction.class)))
            .thenReturn(future);
    when(future.isDone()).thenReturn(true);
    when(future.get()).thenReturn(request);
    pendingDeploymentSweeperJob.run();
    pendingDeploymentSweeperJob.run();
    verify(future, never()).cancel(true);
    verify(databaseService).updateDeploymentRetryCount(anyString(), eq(1));
  }

  @Test
  void runWithInProgressDeployment_ExceptionCompletingTask() throws ExecutionException, InterruptedException, IOException {
    final Future future = mock(Future.class);
    final LocalDateTime now = LocalDateTime.now(ZoneId.of("Z"));
    final DbPendingDeploymentModel pendingDeploymentModel = new DbPendingDeploymentModel();
    final DbDeploymentModel deploymentModel = getBasicDbDeploymentGitHub();
    when(databaseService.getDeployments(anyList())).thenReturn(List.of(deploymentModel));
    when(databaseService.getResume("123")).thenReturn(mock(DbResumeModel.class));
    when(databaseService.getPendingDeployments()).thenReturn(List.of(pendingDeploymentModel));
    when(dateTimeFactory.getLocalDateTimeNow(any(ZoneId.class)))
        .thenReturn(now.minus(Duration.ofMinutes(9)))
        .thenReturn(now);
    when(githubPortfolioGenerationTaskManager.schedulePortfolioGenerationTask(any(PendingDeploymentSweeperAction.class)))
        .thenReturn(future);
    when(future.isDone()).thenReturn(true);
    when(future.get()).thenThrow(new ExecutionException("", new IOException()));
    when(userService.getUserOAuthToken(anyString())).thenReturn("oauth-token");
    pendingDeploymentSweeperJob.run();
    pendingDeploymentSweeperJob.run();
    verify(future, never()).cancel(true);
    verify(databaseService, times(2)).updateDeploymentRetryCount(anyString(), eq(1));
    verify(databaseService, never()).deletePendingDeployment(anyString());
  }

  @Test
  void runWithInProgressDeployment_ProcessingFinished() throws IOException {
    final Future future = mock(Future.class);
    final LocalDateTime now = LocalDateTime.now(ZoneId.of("Z"));
    final DbPendingDeploymentModel pendingDeploymentModel = new DbPendingDeploymentModel();
    final DbDeploymentModel deploymentModel = getBasicDbDeploymentGitHub();
    when(databaseService.getDeployments(anyList())).thenReturn(List.of(deploymentModel));
    when(databaseService.getResume("123")).thenReturn(mock(DbResumeModel.class));
    when(databaseService.getPendingDeployments()).thenReturn(List.of(pendingDeploymentModel));
    when(dateTimeFactory.getLocalDateTimeNow(any(ZoneId.class)))
        .thenReturn(now.minus(Duration.ofMinutes(9)))
        .thenReturn(now);
    when(githubPortfolioGenerationTaskManager.schedulePortfolioGenerationTask(any(PendingDeploymentSweeperAction.class)))
            .thenReturn(future);
    when(userService.getUserOAuthToken(anyString())).thenReturn("oauth-token");
    when(future.isDone()).thenReturn(false);
    pendingDeploymentSweeperJob.run();
    pendingDeploymentSweeperJob.run();
    verify(future, never()).cancel(true);
  }

  @Test
  void runWithInProgressDeployment_CancelDeploymentAlreadyFinished() throws IOException {
    final Future future = mock(Future.class);
    final LocalDateTime now = LocalDateTime.now(ZoneId.of("Z"));
    final DbPendingDeploymentModel pendingDeploymentModel = new DbPendingDeploymentModel();
    final DbDeploymentModel deploymentModel = getBasicDbDeploymentGitHub();
    when(databaseService.getDeployments(anyList())).thenReturn(List.of(deploymentModel));
    when(databaseService.getResume("123")).thenReturn(mock(DbResumeModel.class));
    when(databaseService.getPendingDeployments()).thenReturn(List.of(pendingDeploymentModel));
    when(dateTimeFactory.getLocalDateTimeNow(any(ZoneId.class)))
        .thenReturn(now.minus(Duration.ofMinutes(15)))
        .thenReturn(now);
    when(userService.getUserOAuthToken(anyString())).thenReturn("oauth-token");
    when(githubPortfolioGenerationTaskManager.schedulePortfolioGenerationTask(any(PendingDeploymentSweeperAction.class)))
            .thenReturn(future);
    when(future.isDone()).thenReturn(false);
    when(future.cancel(true)).thenReturn(false);
    pendingDeploymentSweeperJob.run();
    pendingDeploymentSweeperJob.run();
    verify(future).cancel(true);
  }

  @Test
  void runWithInProgressDeployment_CancelDeploymentErrorCancellingFuture() throws IOException {
    final Future future = mock(Future.class);
    final LocalDateTime now = LocalDateTime.now(ZoneId.of("Z"));
    final DbPendingDeploymentModel pendingDeploymentModel = new DbPendingDeploymentModel();
    final DbDeploymentModel deploymentModel = getBasicDbDeploymentGitHub();
    when(databaseService.getDeployments(anyList())).thenReturn(List.of(deploymentModel));
    when(databaseService.getResume("123")).thenReturn(mock(DbResumeModel.class));
    when(databaseService.getPendingDeployments()).thenReturn(List.of(pendingDeploymentModel));
    when(dateTimeFactory.getLocalDateTimeNow(any(ZoneId.class)))
        .thenReturn(now.minus(Duration.ofMinutes(15)))
        .thenReturn(now);
    when(userService.getUserOAuthToken(anyString())).thenReturn("oauth-token");
    when(githubPortfolioGenerationTaskManager.schedulePortfolioGenerationTask(any(PendingDeploymentSweeperAction.class)))
            .thenReturn(future);
    when(future.isDone()).thenReturn(false);
    pendingDeploymentSweeperJob.run();
    pendingDeploymentSweeperJob.run();
  }

  @Test
  void runWithInProgressDeployment_ErrorGetUserToken() {
    final DbPendingDeploymentModel pendingDeploymentModel = new DbPendingDeploymentModel();
    final DbDeploymentModel deploymentModel = getBasicDbDeploymentGitHub();
    when(databaseService.getDeployments(anyList())).thenReturn(List.of(deploymentModel));
    when(databaseService.getResume("123")).thenThrow(new MongoException(""));
    when(databaseService.getPendingDeployments()).thenReturn(List.of(pendingDeploymentModel));
    pendingDeploymentSweeperJob.run();
    verify(githubPortfolioGenerationTaskManager, never()).schedulePortfolioGenerationTask(any(PendingDeploymentSweeperAction.class));

  }

  @Test
  void runWithInProgressDeployment_ErrorGettingTokenGitHub() throws IOException {
    final DbPendingDeploymentModel pendingDeploymentModel = new DbPendingDeploymentModel();
    final DbDeploymentModel deploymentModel = getBasicDbDeploymentGitHub();
    when(databaseService.getDeployments(anyList())).thenReturn(List.of(deploymentModel));
    when(databaseService.getResume("123")).thenReturn(mock(DbResumeModel.class));
    when(databaseService.getPendingDeployments()).thenReturn(List.of(pendingDeploymentModel));
    when(userService.getUserOAuthToken(anyString())).thenThrow(new IOException(""));
    pendingDeploymentSweeperJob.run();
    verify(githubPortfolioGenerationTaskManager, never()).schedulePortfolioGenerationTask(any(PendingDeploymentSweeperAction.class));

  }

  @Test
  void cancelDeployment_pending() throws IOException {
    testDeploymentCancelled(DeploymentStatus.PENDING);
  }

  @Test
  void cancelDeployment_pendingRetry() throws IOException {
    testDeploymentCancelled(DeploymentStatus.PENDING_RETRY);
  }

  @Test
  void cancelDeployment_dontCancelIfTaskInProgress() throws IOException {
    testDeploymentIsNotCancelled(DeploymentStatus.PROCESSING);
  }

  @Test
  void cancelDeployment_dontCancelIfTaskFinished() throws IOException {
    testDeploymentIsNotCancelled(DeploymentStatus.SUCCESSFUL);
  }

  @Test
  void cancelDeployment_dontCancelIfSentToGitHub() throws IOException {
    testDeploymentIsNotCancelled(DeploymentStatus.SENT_TO_GITHUB);
  }

  @Test
  void cancelDeployment_dontCancelIfAlreadyFailed() throws IOException {
    testDeploymentIsNotCancelled(DeploymentStatus.FAILED);
  }


  @Test
  void processingDeploymentWithNoTaskShouldBeFailed() throws IOException {
    final ArgumentCaptor<com.portfolio.generator.utilities.helpers.DeploymentStatus> argumentCaptor =
        ArgumentCaptor.forClass(com.portfolio.generator.utilities.helpers.DeploymentStatus.class);
    final DbPendingDeploymentModel pendingDeploymentModel = new DbPendingDeploymentModel();
    final DbDeploymentModel deploymentModel = getBasicDbDeploymentGitHub();
    deploymentModel.setStatus(DeploymentStatus.PROCESSING);
    deploymentModel.setCancellationRequested(true);
    when(databaseService.getDeployments(anyList())).thenReturn(List.of(deploymentModel));
    when(databaseService.getPendingDeployments()).thenReturn(List.of(pendingDeploymentModel));
    pendingDeploymentSweeperJob.run();
    verify(githubPortfolioGenerationTaskManager, never()).schedulePortfolioGenerationTask(any(PendingDeploymentSweeperAction.class));

    verify(deploymentStatusHelper).updateDeploymentProgress(argumentCaptor.capture());
    assertThat(argumentCaptor.getValue().getProgress()).isEqualTo(0L);
    assertThat(argumentCaptor.getValue().getStatus()).isEqualTo(DeploymentStatusType.FAILED);
  }

  @Test
  void cancelledTaskIsRetried() throws IOException {
    final ArgumentCaptor<com.portfolio.generator.utilities.helpers.DeploymentStatus> argumentCaptor =
        ArgumentCaptor.forClass(com.portfolio.generator.utilities.helpers.DeploymentStatus.class);
    final Future future = mock(Future.class);
    final LocalDateTime now = LocalDateTime.now(ZoneId.of("Z"));
    final DbPendingDeploymentModel pendingDeploymentModel = new DbPendingDeploymentModel();
    final DbDeploymentModel deploymentModel = getBasicDbDeploymentGitHub();
    when(databaseService.getDeployments(anyList()))
        .thenReturn(List.of(deploymentModel));
    when(databaseService.getResume("123")).thenReturn(mock(DbResumeModel.class));
    when(databaseService.getPendingDeployments()).thenReturn(List.of(pendingDeploymentModel));
    when(dateTimeFactory.getLocalDateTimeNow(any(ZoneId.class)))
        .thenReturn(now.minus(Duration.ofMinutes(15)))
        .thenReturn(now);
    when(userService.getUserOAuthToken(anyString())).thenReturn("oauth-token");
    when(githubPortfolioGenerationTaskManager.schedulePortfolioGenerationTask(any(PendingDeploymentSweeperAction.class)))
            .thenReturn(future);
    when(future.isCancelled()).thenReturn(true);
    pendingDeploymentSweeperJob.run();
    pendingDeploymentSweeperJob.run();
    verify(databaseService, times(2)).updateDeploymentRetryCount(anyString(), eq(1));
    verify(deploymentStatusHelper).updateDeploymentProgress(argumentCaptor.capture());
    assertThat(argumentCaptor.getValue().getProgress()).isEqualTo(0L);
    assertThat(argumentCaptor.getValue().getStatus()).isEqualTo(DeploymentStatusType.PENDING_RETRY);
  }

  @Test
  void cancelledTaskIsNotRetriedIfMaxRetriesReached() throws IOException {
    final ArgumentCaptor<com.portfolio.generator.utilities.helpers.DeploymentStatus> argumentCaptor =
        ArgumentCaptor.forClass(com.portfolio.generator.utilities.helpers.DeploymentStatus.class);
    final Future future = mock(Future.class);
    final LocalDateTime now = LocalDateTime.now(ZoneId.of("Z"));
    final DbPendingDeploymentModel pendingDeploymentModel = new DbPendingDeploymentModel();
    final DbDeploymentModel deploymentModel = getBasicDbDeploymentGitHub();
    deploymentModel.setRetryCount(MAX_RETRIES - 1);
    final DbDeploymentModel deploymentModel2 = getBasicDbDeploymentGitHub();
    deploymentModel2.setStatus(DeploymentStatus.PENDING_RETRY);
    deploymentModel2.setRetryCount(MAX_RETRIES);
    deploymentModel2.setCancellationRequested(false);
    when(databaseService.getDeployments(anyList()))
        .thenReturn(List.of(deploymentModel))
        .thenReturn(List.of(deploymentModel2));
    when(databaseService.getResume("123")).thenReturn(mock(DbResumeModel.class));
    when(databaseService.getPendingDeployments()).thenReturn(List.of(pendingDeploymentModel));
    when(dateTimeFactory.getLocalDateTimeNow(any(ZoneId.class)))
        .thenReturn(now.minus(Duration.ofMinutes(15)))
        .thenReturn(now);
    when(userService.getUserOAuthToken(anyString())).thenReturn("oauth-token");
    when(githubPortfolioGenerationTaskManager.schedulePortfolioGenerationTask(any(PendingDeploymentSweeperAction.class)))
            .thenReturn(future);
    when(future.isCancelled()).thenReturn(true);
    pendingDeploymentSweeperJob.run();
    pendingDeploymentSweeperJob.run();
    verify(databaseService, times(1)).updateDeploymentRetryCount(anyString(), eq(MAX_RETRIES));
    verify(deploymentStatusHelper, times(2)).updateDeploymentProgress(argumentCaptor.capture());
    assertThat(argumentCaptor.getAllValues().get(0).getProgress()).isEqualTo(0L);
    assertThat(argumentCaptor.getAllValues().get(0).getStatus()).isEqualTo(DeploymentStatusType.PENDING_RETRY);
    assertThat(argumentCaptor.getAllValues().get(1).getProgress()).isEqualTo(0L);
    assertThat(argumentCaptor.getAllValues().get(1).getStatus()).isEqualTo(DeploymentStatusType.FAILED);
  }

  @Test
  void sentToGitHubDeploymentWithNoTaskShouldDoNothing() throws IOException {
    testDeploymentWithNoTask(DeploymentStatus.SUCCESSFUL);
  }

  @Test
  void sentToCancelledTaskShouldDoNothing() throws IOException {
    testDeploymentWithNoTask(DeploymentStatus.CANCELLED);
  }

  @Test
  void deploymentExceedMaxRetriedShouldBeFailed() throws IOException {
    final ArgumentCaptor<com.portfolio.generator.utilities.helpers.DeploymentStatus> argumentCaptor =
        ArgumentCaptor.forClass(com.portfolio.generator.utilities.helpers.DeploymentStatus.class);
    final DbPendingDeploymentModel pendingDeploymentModel = new DbPendingDeploymentModel();
    final DbDeploymentModel deploymentModel = getBasicDbDeploymentGitHub();
    deploymentModel.setStatus(DeploymentStatus.PENDING_RETRY);
    deploymentModel.setRetryCount(5);
    when(databaseService.getDeployments(anyList())).thenReturn(List.of(deploymentModel));
    when(databaseService.getPendingDeployments()).thenReturn(List.of(pendingDeploymentModel));
    pendingDeploymentSweeperJob.run();
    verify(githubPortfolioGenerationTaskManager, never()).schedulePortfolioGenerationTask(any(PendingDeploymentSweeperAction.class));

    verify(deploymentStatusHelper).updateDeploymentProgress(argumentCaptor.capture());
    assertThat(argumentCaptor.getValue().getProgress()).isEqualTo(0L);
    assertThat(argumentCaptor.getValue().getStatus()).isEqualTo(DeploymentStatusType.FAILED);
  }

  @Test
  void exceptionUpdatingStatusToCancelDeployment() throws IOException {
    final ArgumentCaptor<com.portfolio.generator.utilities.helpers.DeploymentStatus> argumentCaptor =
        ArgumentCaptor.forClass(com.portfolio.generator.utilities.helpers.DeploymentStatus.class);
    final DbPendingDeploymentModel pendingDeploymentModel = new DbPendingDeploymentModel();
    final DbDeploymentModel deploymentModel = getBasicDbDeploymentGitHub();
    deploymentModel.setStatus(DeploymentStatus.PENDING_RETRY);
    deploymentModel.setRetryCount(5);
    when(databaseService.getDeployments(anyList())).thenReturn(List.of(deploymentModel));
    when(databaseService.getPendingDeployments()).thenReturn(List.of(pendingDeploymentModel));
    doThrow(new IOException("Failing during test")).when(deploymentStatusHelper).updateDeploymentProgress(any());
    pendingDeploymentSweeperJob.run();
    verify(githubPortfolioGenerationTaskManager, never()).schedulePortfolioGenerationTask(any(PendingDeploymentSweeperAction.class));

    verify(deploymentStatusHelper).updateDeploymentProgress(argumentCaptor.capture());
    assertThat(argumentCaptor.getValue().getProgress()).isEqualTo(0L);
    assertThat(argumentCaptor.getValue().getStatus()).isEqualTo(DeploymentStatusType.FAILED);
  }

  private void testDeploymentWithNoTask(final DeploymentStatus deploymentStatus) throws IOException {
    final DbPendingDeploymentModel pendingDeploymentModel = new DbPendingDeploymentModel();
    final DbDeploymentModel deploymentModel = getBasicDbDeploymentGitHub();
    deploymentModel.setStatus(deploymentStatus);
    deploymentModel.setCancellationRequested(true);
    when(databaseService.getDeployments(anyList())).thenReturn(List.of(deploymentModel));
    when(databaseService.getPendingDeployments()).thenReturn(List.of(pendingDeploymentModel));
    pendingDeploymentSweeperJob.run();
    verify(githubPortfolioGenerationTaskManager, never()).schedulePortfolioGenerationTask(any(PendingDeploymentSweeperAction.class));

    verify(deploymentStatusHelper, never()).updateDeploymentProgress(any());
  }


  private void testDeploymentIsNotCancelled(final DeploymentStatus deploymentStatus) throws IOException {
    final Future future = mock(Future.class);
    final LocalDateTime now = LocalDateTime.now(ZoneId.of("Z"));
    final DbPendingDeploymentModel pendingDeploymentModel = new DbPendingDeploymentModel();

    final DbDeploymentModel deploymentModel = getBasicDbDeploymentGitHub();

    final DbDeploymentModel deploymentModel2 = getBasicDbDeploymentGitHub();
    deploymentModel2.setStatus(deploymentStatus);
    deploymentModel2.setRetryCount(1);
    deploymentModel2.setCancellationRequested(true);
    when(databaseService.getDeployments(anyList()))
        .thenReturn(List.of(deploymentModel))
        .thenReturn(List.of(deploymentModel2));
    when(databaseService.getResume("123")).thenReturn(mock(DbResumeModel.class));
    when(databaseService.getPendingDeployments()).thenReturn(List.of(pendingDeploymentModel));
    when(dateTimeFactory.getLocalDateTimeNow(any(ZoneId.class)))
        .thenReturn(now);
    when(userService.getUserOAuthToken(anyString())).thenReturn("oauth-token");
    when(githubPortfolioGenerationTaskManager.schedulePortfolioGenerationTask(any(PendingDeploymentSweeperAction.class)))
            .thenReturn(future);

    when(future.isDone()).thenReturn(false);
    pendingDeploymentSweeperJob.run();
    pendingDeploymentSweeperJob.run();

    verify(databaseService).updateDeploymentRetryCount(anyString(), eq(1));
    verify(deploymentStatusHelper, never()).updateDeploymentProgress(any());
  }

  private void testDeploymentCancelled(final DeploymentStatus deploymentStatus) throws IOException {
    final ArgumentCaptor<com.portfolio.generator.utilities.helpers.DeploymentStatus> argumentCaptor =
        ArgumentCaptor.forClass(com.portfolio.generator.utilities.helpers.DeploymentStatus.class);
    final DbPendingDeploymentModel pendingDeploymentModel = new DbPendingDeploymentModel();
    final DbDeploymentModel deploymentModel = getBasicDbDeploymentGitHub();
    deploymentModel.setStatus(deploymentStatus);
    deploymentModel.setCancellationRequested(true);
    when(databaseService.getDeployments(anyList())).thenReturn(List.of(deploymentModel));
    when(databaseService.getPendingDeployments()).thenReturn(List.of(pendingDeploymentModel));
    pendingDeploymentSweeperJob.run();
    verify(githubPortfolioGenerationTaskManager, never()).schedulePortfolioGenerationTask(any(PendingDeploymentSweeperAction.class));

    verify(deploymentStatusHelper).updateDeploymentProgress(argumentCaptor.capture());
    assertThat(argumentCaptor.getValue().getProgress()).isEqualTo(0L);
    assertThat(argumentCaptor.getValue().getStatus()).isEqualTo(DeploymentStatusType.CANCELLED);
  }


  @Test
  void runJobMongoExceptionFetchingPendingDeployments() {
    when(databaseService.getPendingDeployments()).thenThrow(new MongoException("error!"));
    pendingDeploymentSweeperJob.run();
  }

  @Test
  void runJobMongoExceptionFetchingDeployments() {
    final DbPendingDeploymentModel pendingDeploymentModel = new DbPendingDeploymentModel();
    when(databaseService.getDeployments(anyList())).thenThrow(new MongoException("error!"));
    when(databaseService.getPendingDeployments()).thenReturn(List.of(pendingDeploymentModel));
    pendingDeploymentSweeperJob.run();
  }

  @Test
  void externalStatusCompleted_GitHub() throws IOException, ExecutionException, InterruptedException {
    final Future<StatusPollerResult> statusPollerResultFuture = mock(Future.class);
    final StatusPollerResult statusPollerResult = new StatusPollerResult.Builder()
        .withDeploymentId("635d6be57c842c8265a3b0c4")
        .withDeploymentProvider(DeploymentProvider.GITHUB)
        .withDeployedUrl("deployed-url")
        .build();
    final ArgumentCaptor<PendingDeploymentSweeperAction> argumentCaptor =
        ArgumentCaptor.forClass(PendingDeploymentSweeperAction.class);
    final DbPendingDeploymentModel pendingDeploymentModel = new DbPendingDeploymentModel();
    pendingDeploymentModel.set_id("635d6be57c842c8265a3b0c4");
    final DbDeploymentModel deploymentModel = getBasicDbDeploymentGitHub();
    deploymentModel.setStatus(DeploymentStatus.SENT_TO_AWS);
    deploymentModel.setCloudFrontInvalidationId("invalidation-id");
    when(databaseService.getDeployments(anyList())).thenReturn(List.of(deploymentModel))
        .thenReturn(Collections.emptyList());
    when(databaseService.getPendingDeployments()).thenReturn(List.of(pendingDeploymentModel))
        .thenReturn(Collections.emptyList());
    when(deploymentStatusPollerScheduler.pollDeploymentStatusUntilFinished(any(PendingDeploymentSweeperAction.class)))
        .thenReturn(statusPollerResultFuture);
    when(statusPollerResultFuture.isDone()).thenReturn(true);
    when(statusPollerResultFuture.get()).thenReturn(statusPollerResult);
    when(dateTimeFactory.getLocalDateTimeNow(eq(TIMEZONE_ID))).thenReturn(LocalDateTime.now(TIMEZONE_ID));
    when(userService.getUserOAuthToken(anyString())).thenReturn("oauth-token");
    when(userService.getUserGitHubUserName(anyString())).thenReturn("github-username");
    pendingDeploymentSweeperJob.run();
    pendingDeploymentSweeperJob.run();
    verify(dateTimeFactory, times(2)).getLocalDateTimeNow(ZoneId.of("Z"));

    verify(deploymentStatusPollerScheduler).pollDeploymentStatusUntilFinished(argumentCaptor.capture());
    verify(databaseService).deletePendingDeployment(anyString());
    verify(databaseService).updateDeploymentUrl(eq("635d6be57c842c8265a3b0c4"), eq("deployed-url"));
    final PendingDeploymentSweeperAction deploymentSweeperAction = argumentCaptor.getValue();
    assertThat(deploymentSweeperAction).isNotNull();
    assertThat(deploymentSweeperAction.deploymentId).isEqualTo("635d6be57c842c8265a3b0c4");
    assertThat(deploymentSweeperAction.pendingDeploymentId).isEqualTo("635d6be57c842c8265a3b0c4");
    assertThat(deploymentSweeperAction.invalidationId).isEqualTo("invalidation-id");
    assertThat(deploymentSweeperAction.deploymentPhase).isEqualTo(DeploymentPhase.DEPLOY);
  }

  @Test
  void externalStatusChecksScheduled_IOException() throws IOException {
    final ArgumentCaptor<PendingDeploymentSweeperAction> argumentCaptor =
        ArgumentCaptor.forClass(PendingDeploymentSweeperAction.class);
    final DbPendingDeploymentModel pendingDeploymentModel = new DbPendingDeploymentModel();
    pendingDeploymentModel.set_id("635d6be57c842c8265a3b0c4");
    final DbDeploymentModel deploymentModel = getBasicDbDeploymentGitHub();
    deploymentModel.setStatus(DeploymentStatus.SENT_TO_GITHUB);
    deploymentModel.setDeploymentProvider("GITHUB");
    deploymentModel.setCloudFrontInvalidationId("invalidation-id");
    when(userService.getUserOAuthToken(anyString())).thenThrow(new IOException());
    when(databaseService.getDeployments(anyList())).thenReturn(List.of(deploymentModel));
    when(databaseService.getPendingDeployments()).thenReturn(List.of(pendingDeploymentModel));
    pendingDeploymentSweeperJob.run();
    verify(deploymentStatusPollerScheduler, never()).pollDeploymentStatusUntilFinished(argumentCaptor.capture());
  }

  @Test
  void externalStatusChecksScheduled_ExceptionThenSuccess() throws IOException {
    final ArgumentCaptor<PendingDeploymentSweeperAction> argumentCaptor =
        ArgumentCaptor.forClass(PendingDeploymentSweeperAction.class);
    final DbPendingDeploymentModel pendingDeploymentModel = new DbPendingDeploymentModel();
    pendingDeploymentModel.set_id("635d6be57c842c8265a3b0c4");
    final DbDeploymentModel deploymentModel = getBasicDbDeploymentGitHub();
    deploymentModel.setStatus(DeploymentStatus.SENT_TO_GITHUB);
    deploymentModel.setDeploymentProvider("GITHUB");
    deploymentModel.setCloudFrontInvalidationId("invalidation-id");
    when(userService.getUserOAuthToken(anyString()))
        .thenThrow(new IOException())
        .thenReturn("oauth-token");
    when(userService.getUserGitHubUserName(anyString())).thenReturn("github-username");
    when(databaseService.getDeployments(anyList())).thenReturn(List.of(deploymentModel));
    when(databaseService.getPendingDeployments()).thenReturn(List.of(pendingDeploymentModel));
    pendingDeploymentSweeperJob.run();
    pendingDeploymentSweeperJob.run();
    verify(deploymentStatusPollerScheduler).pollDeploymentStatusUntilFinished(argumentCaptor.capture());
    verify(dateTimeFactory).getLocalDateTimeNow(ZoneId.of("Z"));
    final PendingDeploymentSweeperAction deploymentSweeperAction = argumentCaptor.getValue();
    assertThat(deploymentSweeperAction).isNotNull();
    assertThat(deploymentSweeperAction.deploymentId).isEqualTo("635d6be57c842c8265a3b0c4");
    assertThat(deploymentSweeperAction.pendingDeploymentId).isEqualTo("635d6be57c842c8265a3b0c4");
    assertThat(deploymentSweeperAction.invalidationId).isEqualTo("invalidation-id");
    assertThat(deploymentSweeperAction.deploymentProvider).isEqualTo(DeploymentProvider.GITHUB);
    assertThat(deploymentSweeperAction.deploymentPhase).isEqualTo(DeploymentPhase.DEPLOY);
    assertThat(deploymentSweeperAction.oAuthToken).isEqualTo("oauth-token");
    assertThat(deploymentSweeperAction.gitHubUserName).isEqualTo("github-username");
  }

  @Test
  void externalStatusChecksScheduled_GitHub() throws IOException {
    final ArgumentCaptor<PendingDeploymentSweeperAction> argumentCaptor =
        ArgumentCaptor.forClass(PendingDeploymentSweeperAction.class);
    final DbPendingDeploymentModel pendingDeploymentModel = new DbPendingDeploymentModel();
    pendingDeploymentModel.set_id("635d6be57c842c8265a3b0c4");
    final DbDeploymentModel deploymentModel = getBasicDbDeploymentGitHub();
    deploymentModel.setStatus(DeploymentStatus.SENT_TO_GITHUB);
    deploymentModel.setDeploymentProvider("GITHUB");
    deploymentModel.setCloudFrontInvalidationId("invalidation-id");
    when(databaseService.getDeployments(anyList())).thenReturn(List.of(deploymentModel));
    when(databaseService.getPendingDeployments()).thenReturn(List.of(pendingDeploymentModel));
    when(userService.getUserOAuthToken(anyString())).thenReturn("oauth-token");
    when(userService.getUserGitHubUserName(anyString())).thenReturn("github-username");
    pendingDeploymentSweeperJob.run();
    verify(dateTimeFactory).getLocalDateTimeNow(ZoneId.of("Z"));
    verify(deploymentStatusPollerScheduler).pollDeploymentStatusUntilFinished(argumentCaptor.capture());
    verify(userService).getUserOAuthToken(anyString());
    verify(userService).getUserGitHubUserName(anyString());
    final PendingDeploymentSweeperAction deploymentSweeperAction = argumentCaptor.getValue();
    assertThat(deploymentSweeperAction).isNotNull();
    assertThat(deploymentSweeperAction.deploymentId).isEqualTo("635d6be57c842c8265a3b0c4");
    assertThat(deploymentSweeperAction.pendingDeploymentId).isEqualTo("635d6be57c842c8265a3b0c4");
    assertThat(deploymentSweeperAction.invalidationId).isEqualTo("invalidation-id");
    assertThat(deploymentSweeperAction.deploymentProvider).isEqualTo(DeploymentProvider.GITHUB);
    assertThat(deploymentSweeperAction.deploymentPhase).isEqualTo(DeploymentPhase.DEPLOY);
    assertThat(deploymentSweeperAction.oAuthToken).isEqualTo("oauth-token");
    assertThat(deploymentSweeperAction.gitHubUserName).isEqualTo("github-username");
  }

  private DbDeploymentModel getBasicDbDeploymentGitHub() {
    return getBasicDbDeployment("GITHUB");
  }

  private DbDeploymentModel getBasicDbDeployment(final String deploymentProvider) {
    final DbDeploymentModel deploymentModel = new DbDeploymentModel();
    deploymentModel.setUserId("userId");
    deploymentModel.set_id("635d6be57c842c8265a3b0c4");
    deploymentModel.setResumeId("123");
    deploymentModel.setRetryCount(0);
    deploymentModel.setLastUpdatedAt(new Date().getTime());
    deploymentModel.setCancellationRequested(false);
    deploymentModel.setDeploymentProvider(deploymentProvider);
    deploymentModel.setGithubUserName("username");

    final DbDeploymentModel.WebsiteDetails websiteDetails = new DbDeploymentModel.WebsiteDetails();
    websiteDetails.setWebsiteIdentifier("cool-website-1234");
    deploymentModel.setWebsiteDetails(websiteDetails);

    return deploymentModel;
  }

}