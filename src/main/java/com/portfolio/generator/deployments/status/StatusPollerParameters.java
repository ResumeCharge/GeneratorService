package com.portfolio.generator.deployments.status;

import com.portfolio.generator.deployments.jobs.PendingDeploymentSweeperAction;

public class StatusPollerParameters {
  public final GitHubStatusPoller gitHubStatusPoller;
  public final PendingDeploymentSweeperAction deploymentSweeperAction;

  public StatusPollerParameters(final Builder builder) {
    this.gitHubStatusPoller = builder.gitHubStatusPoller;
    this.deploymentSweeperAction = builder.deploymentSweeperAction;
  }

  static class Builder {
    GitHubStatusPoller gitHubStatusPoller;
    PendingDeploymentSweeperAction deploymentSweeperAction;


    public Builder withGitHubStatusPoller(final GitHubStatusPoller gitHubStatusPoller) {
      this.gitHubStatusPoller = gitHubStatusPoller;
      return this;
    }

    public Builder withDeploymentSweeperAction(final PendingDeploymentSweeperAction deploymentSweeperAction) {
      this.deploymentSweeperAction = deploymentSweeperAction;
      return this;
    }

    public StatusPollerParameters build() {
      return new StatusPollerParameters(this);
    }
  }
}
