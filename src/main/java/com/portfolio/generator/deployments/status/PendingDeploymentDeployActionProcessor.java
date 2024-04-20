package com.portfolio.generator.deployments.status;

import com.portfolio.generator.concurrency.factory.IExecutorServiceFactory;
import com.portfolio.generator.deployments.jobs.DeploymentPhase;
import com.portfolio.generator.deployments.jobs.PendingDeploymentSweeperAction;
import com.portfolio.generator.models.staticsite.DeploymentProvider;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Component
public class PendingDeploymentDeployActionProcessor implements IPendingDeploymentDeployActionProcessor {
  private static final Logger log = LoggerFactory.getLogger(PendingDeploymentDeployActionProcessor.class);
  private final IExecutorServiceFactory executorServiceFactory;
  private final GitHubStatusPoller gitHubStatusPoller;
  private ExecutorService executorService;

  public PendingDeploymentDeployActionProcessor(final IExecutorServiceFactory executorServiceFactory,
                                                final GitHubStatusPoller gitHubStatusPoller
) {
    this.executorServiceFactory = executorServiceFactory;
    this.gitHubStatusPoller = gitHubStatusPoller;

  }

  @Override
  public Future<StatusPollerResult> pollDeploymentStatusUntilFinished(final PendingDeploymentSweeperAction deploymentSweeperAction) {
    if (DeploymentPhase.DEPLOY != deploymentSweeperAction.deploymentPhase) {
      throw new IllegalArgumentException(String.format("DeploymentPhase %s is not valid for DeploymentStatusPollerScheduler", deploymentSweeperAction.deploymentPhase));
    }

    if (executorService == null) {
      executorService = executorServiceFactory.getFixedSizedThreadPool(100);
    }
    final StatusPollerCallable checkCallable = getCallableForDeploymentRequest(deploymentSweeperAction);
    log.info(String.format("Submitting check status externally request for deploymentId %s", deploymentSweeperAction));
    return executorService.submit(checkCallable);
  }

  private StatusPollerCallable getCallableForDeploymentRequest(final PendingDeploymentSweeperAction deploymentSweeperAction) {
    final DeploymentProvider deploymentProvider = deploymentSweeperAction.deploymentProvider;
    final StatusPollerParameters.Builder statusPollerParametersBuilder = new StatusPollerParameters.Builder()
        .withDeploymentSweeperAction(deploymentSweeperAction);
    if (deploymentProvider == DeploymentProvider.GITHUB) {
      statusPollerParametersBuilder.withGitHubStatusPoller(gitHubStatusPoller);
      return new StatusPollerCallable(statusPollerParametersBuilder.build());
    } else {
      throw new IllegalArgumentException();
    }

  }

  static class StatusPollerCallable implements Callable<StatusPollerResult> {
    private final StatusPollerParameters statusPollerParameters;

    StatusPollerCallable(final StatusPollerParameters statusPollerParameters) {
      this.statusPollerParameters = statusPollerParameters;
    }

    @Override
    public StatusPollerResult call() throws Exception {
      final StatusPollerResult.Builder statusPollerResultBuilder = new StatusPollerResult.Builder();
      final PendingDeploymentSweeperAction deploymentSweeperAction = statusPollerParameters.deploymentSweeperAction;
      final DeploymentProvider deploymentProvider = deploymentSweeperAction.deploymentProvider;
      final String deploymentId = deploymentSweeperAction.deploymentId;
      Validate.notNull(deploymentSweeperAction);
      Validate.notNull(deploymentProvider);
      Validate.notBlank(deploymentId);
      statusPollerResultBuilder
          .withDeploymentId(deploymentId)
          .withDeploymentProvider(deploymentProvider)
          .withPendingDeploymentId(deploymentId);
      if (deploymentProvider == DeploymentProvider.GITHUB) {
        final GitHubStatusPoller gitHubStatusPoller = statusPollerParameters.gitHubStatusPoller;
        Validate.notNull(gitHubStatusPoller);
        final String deployedUrl =
            gitHubStatusPoller.pollDeploymentStatusUntilFinishedAndReturnDeployedUrl(deploymentSweeperAction);
        statusPollerResultBuilder.withDeployedUrl(deployedUrl);
      }  else {
        throw new IllegalArgumentException(String.format("DeploymentProvider %s not valid", deploymentProvider));
      }
      final StatusPollerResult statusPollerResult = statusPollerResultBuilder.build();
      log.info(String.format("External status checks completed, got result %s", statusPollerResult));
      return statusPollerResult;
    }
  }
}
