package com.portfolio.generator.deployments.status;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudfront.AmazonCloudFront;
import com.google.common.util.concurrent.MoreExecutors;
import com.portfolio.generator.concurrency.factory.IExecutorServiceFactory;
import com.portfolio.generator.deployments.jobs.DeploymentPhase;
import com.portfolio.generator.deployments.jobs.PendingDeploymentSweeperAction;
import com.portfolio.generator.models.staticsite.DeploymentProvider;
import com.portfolio.generator.utilities.aws.factories.IAWSClientFactory;
import com.portfolio.generator.utilities.aws.factories.ICredentialsProviderFactory;
import com.portfolio.generator.utilities.exceptions.PortfolioGenerationFailedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PendingDeploymentDeployActionProcessorTest {
  @Mock
  private IExecutorServiceFactory executorServiceFactory;
  @Mock
  private GitHubStatusPoller gitHubStatusPoller;
  @Mock
  private IAWSClientFactory awsClientFactory;
  @Mock
  private ICredentialsProviderFactory credentialsProviderFactory;
  private PendingDeploymentDeployActionProcessor actionProcessor;

  @BeforeEach
  void setUp() {
    actionProcessor = new PendingDeploymentDeployActionProcessor(
        executorServiceFactory,
        gitHubStatusPoller
        );
  }


  @Test
  void pollDeploymentStatusUntilFinished_GitHub()
      throws ExecutionException, InterruptedException, PortfolioGenerationFailedException, IOException {
    final ExecutorService executorService = MoreExecutors.newDirectExecutorService();
    final PendingDeploymentSweeperAction deploymentSweeperAction = new PendingDeploymentSweeperAction.Builder()
        .setDeploymentPhase(DeploymentPhase.DEPLOY)
        .setDeploymentProvider(DeploymentProvider.GITHUB)
        .setDeploymentId("deployment-id")
        .setPendingDeploymentId("deployment-id")
        .build();
    when(executorServiceFactory.getFixedSizedThreadPool(anyInt())).thenReturn(executorService);
    when(gitHubStatusPoller.pollDeploymentStatusUntilFinishedAndReturnDeployedUrl(any(PendingDeploymentSweeperAction.class)))
        .thenReturn("deployed-url");
    final Future<StatusPollerResult> result =
        actionProcessor.pollDeploymentStatusUntilFinished(deploymentSweeperAction);
    final StatusPollerResult statusPollerResult = result.get();
    assertThat(result.isDone()).isTrue();
    assertThat(statusPollerResult.deploymentProvider).isEqualTo(DeploymentProvider.GITHUB);
    assertThat(statusPollerResult.deploymentId).isEqualTo("deployment-id");
    assertThat(statusPollerResult.deployedUrl).isEqualTo("deployed-url");
    assertThat(statusPollerResult.pendingDeploymentId).isEqualTo("deployment-id");
  }

  @Test
  void pollDeploymentStatusUntilFinished_TestExecutorServiceInitialization()
      throws ExecutionException, InterruptedException, PortfolioGenerationFailedException, IOException {
    final ExecutorService executorService = MoreExecutors.newDirectExecutorService();
    final PendingDeploymentSweeperAction deploymentSweeperAction = new PendingDeploymentSweeperAction.Builder()
        .setDeploymentPhase(DeploymentPhase.DEPLOY)
        .setDeploymentProvider(DeploymentProvider.GITHUB)
        .setDeploymentId("deployment-id")
        .setPendingDeploymentId("deployment-id")
        .build();
    when(executorServiceFactory.getFixedSizedThreadPool(anyInt())).thenReturn(executorService);
    when(gitHubStatusPoller.pollDeploymentStatusUntilFinishedAndReturnDeployedUrl(any(PendingDeploymentSweeperAction.class)))
        .thenReturn("deployed-url");
    actionProcessor.pollDeploymentStatusUntilFinished(deploymentSweeperAction);
    final Future<StatusPollerResult> result =
        actionProcessor.pollDeploymentStatusUntilFinished(deploymentSweeperAction);
    final StatusPollerResult statusPollerResult = result.get();
    assertThat(result.isDone()).isTrue();
    assertThat(statusPollerResult.deploymentProvider).isEqualTo(DeploymentProvider.GITHUB);
    assertThat(statusPollerResult.deploymentId).isEqualTo("deployment-id");
    assertThat(statusPollerResult.deployedUrl).isEqualTo("deployed-url");
    assertThat(statusPollerResult.pendingDeploymentId).isEqualTo("deployment-id");
    verify(executorServiceFactory, times(1)).getFixedSizedThreadPool(anyInt());
  }

  @Test
  void pollDeploymentStatusUntilFinished_BuildDeploymentPhase() {
    assertThrows(IllegalArgumentException.class, () -> {
      actionProcessor.pollDeploymentStatusUntilFinished(new PendingDeploymentSweeperAction.Builder().setDeploymentPhase(DeploymentPhase.BUILD).build());
    });
  }
}