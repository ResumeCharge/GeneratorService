package com.portfolio.generator.deployments.status;

import com.portfolio.generator.models.staticsite.DeploymentProvider;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public class StatusPollerResult {
  public final String deployedUrl;
  public final String deploymentId;
  public final String pendingDeploymentId;
  public final DeploymentProvider deploymentProvider;

  private StatusPollerResult(final Builder builder) {
    this.deployedUrl = builder.deployedUrl;
    this.deploymentId = builder.deploymentId;
    this.pendingDeploymentId = builder.pendingDeploymentId;
    this.deploymentProvider = builder.deploymentProvider;
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this);
  }

  public static class Builder {
    private String deployedUrl;
    private String deploymentId;
    private String pendingDeploymentId;
    private DeploymentProvider deploymentProvider;

    public Builder withDeployedUrl(final String deployedUrl) {
      this.deployedUrl = deployedUrl;
      return this;
    }

    public Builder withDeploymentId(final String deploymentId) {
      this.deploymentId = deploymentId;
      return this;
    }

    public Builder withPendingDeploymentId(final String pendingDeploymentId) {
      this.pendingDeploymentId = pendingDeploymentId;
      return this;
    }

    public Builder withDeploymentProvider(final DeploymentProvider deploymentProvider) {
      this.deploymentProvider = deploymentProvider;
      return this;
    }

    public StatusPollerResult build() {
      return new StatusPollerResult(this);
    }
  }
}
