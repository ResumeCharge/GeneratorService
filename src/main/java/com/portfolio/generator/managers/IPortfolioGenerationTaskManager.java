package com.portfolio.generator.managers;

import com.portfolio.generator.deployments.jobs.PendingDeploymentSweeperAction;

import java.util.concurrent.Future;

/**
 * Interface for classes that handle portfolio generation task request.
 **/
public interface IPortfolioGenerationTaskManager {
  Future<PendingDeploymentSweeperAction> schedulePortfolioGenerationTask(final PendingDeploymentSweeperAction deploymentRequest);
}
