package com.portfolio.generator.models.staticsite;

import java.nio.file.Path;

public class PortfolioGenerationTask {
  public final String UUID;
  public final String oAuthToken;
  public final DeploymentProvider deploymentProvider;
  public final String repoName;
  public final Path pathToLocalPagesWebsiteFolder;
  public final String codeBuildProject;
  public final String websiteIdentifier;
  public final String bucketName;
  public final String cloudFrontDistributionId;
  public final String deploymentId;
  public final String githubUserName;

  private PortfolioGenerationTask(final Builder builder) {
    this.UUID = builder.UUID;
    this.deploymentProvider = builder.deploymentProvider;
    this.repoName = builder.repoName;
    this.pathToLocalPagesWebsiteFolder = builder.pathToLocalPagesWebsiteFolder;
    this.oAuthToken = builder.oAuthToken;
    this.codeBuildProject = builder.codeBuildProject;
    this.websiteIdentifier = builder.websiteIdentifier;
    this.bucketName = builder.bucketName;
    this.cloudFrontDistributionId = builder.cloudFrontDistributionId;
    this.deploymentId = builder.deploymentId;
    this.githubUserName = builder.githubUserName;
  }

  public static class Builder {
    String UUID;
    String oAuthToken;
    DeploymentProvider deploymentProvider;
    String repoName;
    Path pathToLocalPagesWebsiteFolder;
    String codeBuildProject;
    String websiteIdentifier;
    String bucketName;
    String cloudFrontDistributionId;
    String deploymentId;
    String githubUserName;

    public Builder setUUID(final String UUID) {
      this.UUID = UUID;
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

    public Builder setRepoName(final String repoName) {
      this.repoName = repoName;
      return this;
    }

    public Builder setPathToLocalPagesWebsiteFolder(final Path pathToLocalPagesWebsiteFolder) {
      this.pathToLocalPagesWebsiteFolder = pathToLocalPagesWebsiteFolder;
      return this;
    }

    public Builder setCodeBuildProject(final String codeBuildProject) {
      this.codeBuildProject = codeBuildProject;
      return this;
    }

    public Builder setWebsiteIdentifier(final String websiteIdentifier) {
      this.websiteIdentifier = websiteIdentifier;
      return this;
    }

    public Builder setBucketName(final String bucketName) {
      this.bucketName = bucketName;
      return this;
    }

    public Builder setCloudFrontDistributionId(final String cloudFrontDistributionId) {
      this.cloudFrontDistributionId = cloudFrontDistributionId;
      return this;
    }

    public Builder setDeploymentId(final String deploymentId) {
      this.deploymentId = deploymentId;
      return this;
    }

    public Builder setGithubUserName(final String githubUserName) {
      this.githubUserName = githubUserName;
      return this;
    }

    public PortfolioGenerationTask build() {
      return new PortfolioGenerationTask(this);
    }
  }
}
