package com.portfolio.generator.deployments.status;

import com.portfolio.generator.deployments.jobs.PendingDeploymentSweeperAction;

import java.util.concurrent.Future;

public interface IPendingDeploymentDeployActionProcessor {
  Future<StatusPollerResult> pollDeploymentStatusUntilFinished(final PendingDeploymentSweeperAction checkStatusRequest);
}
