package com.portfolio.generator.deployments.jobs;

import com.portfolio.generator.models.ResumeModel;
import com.portfolio.generator.models.WebsiteDetailsModel;
import com.portfolio.generator.models.staticsite.DeploymentProvider;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.Objects;

public class PendingDeploymentSweeperAction {
  public final ResumeModel resume;
  public final String deploymentId;
  public final WebsiteDetailsModel websiteDetails;
  public final String oAuthToken;
  public final DeploymentProvider deploymentProvider;
  public final String userId;
  public final String pendingDeploymentId;
  public final DeploymentPhase deploymentPhase;
  public final String invalidationId;
  public final String gitHubUserName;

  private PendingDeploymentSweeperAction(final Builder builder) {
    this.resume = builder.resume;
    this.deploymentId = builder.deploymentId;
    this.websiteDetails = builder.websiteDetails;
    this.oAuthToken = builder.oAuthToken;
    this.deploymentProvider = builder.deploymentProvider;
    this.userId = builder.userId;
    this.pendingDeploymentId = builder.pendingDeploymentId;
    this.deploymentPhase = builder.deploymentPhase;
    this.invalidationId = builder.invalidationId;
    this.gitHubUserName = builder.gitHubUserName;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final PendingDeploymentSweeperAction request = (PendingDeploymentSweeperAction) o;
    return Objects.equals(deploymentId, request.deploymentId);
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this);
  }

  @Override
  public int hashCode() {
    return Objects.hash(deploymentId);
  }

  public static class Builder {
    ResumeModel resume;
    String deploymentId;
    WebsiteDetailsModel websiteDetails;
    String oAuthToken;
    DeploymentProvider deploymentProvider;
    String userId;
    String pendingDeploymentId;
    DeploymentPhase deploymentPhase;
    String invalidationId;
    String gitHubUserName;

    public Builder setResume(final ResumeModel resume) {
      this.resume = resume;
      return this;
    }

    public Builder setDeploymentId(final String deploymentId) {
      this.deploymentId = deploymentId;
      return this;
    }

    public Builder setWebsiteDetails(final WebsiteDetailsModel websiteDetails) {
      this.websiteDetails = websiteDetails;
      return this;
    }

    public Builder setoAuthToken(final String oAuthToken) {
      this.oAuthToken = oAuthToken;
      return this;
    }

    public Builder setDeploymentProvider(final DeploymentProvider deploymentProvider) {
      this.deploymentProvider = deploymentProvider;
      return this;
    }

    public Builder setUserId(final String userId) {
      this.userId = userId;
      return this;
    }

    public Builder setPendingDeploymentId(final String pendingDeploymentId) {
      this.pendingDeploymentId = pendingDeploymentId;
      return this;
    }

    public Builder setDeploymentPhase(final DeploymentPhase deploymentPhase) {
      this.deploymentPhase = deploymentPhase;
      return this;
    }

    public Builder setInvalidationId(final String invalidationId) {
      this.invalidationId = invalidationId;
      return this;
    }

    public Builder setGitHubUserName(final String gitHubUserName) {
      this.gitHubUserName = gitHubUserName;
      return this;
    }

    public PendingDeploymentSweeperAction build() {
      return new PendingDeploymentSweeperAction(this);
    }
  }
}
