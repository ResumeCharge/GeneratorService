package com.portfolio.generator.models.staticsite;

import com.portfolio.generator.models.ResumeModel;
import com.portfolio.generator.models.WebsiteDetailsModel;

/**
 * Holds the data from the incoming request.
 */
public class StaticSiteRequestModel {
  public final ResumeModel resume;
  public final WebsiteDetailsModel websiteDetails;
  public final String deploymentId;
  public final String userId;
  public final Long createdAt;
  public final String oAuthToken;
  public final DeploymentProvider deploymentProvider;
  public final String repoName;
  public final String codeBuildProject;
  public final String s3BucketName;
  public final String cloudFrontDistributionId;
  public final String githubUserName;

  private StaticSiteRequestModel(final Builder builder) {
    this.resume = builder.resume;
    this.websiteDetails = builder.websiteDetails;
    this.deploymentId = builder.deploymentId;
    this.createdAt = builder.createdAt;
    this.userId = builder.userId;
    this.oAuthToken = builder.oAuthToken;
    this.deploymentProvider = builder.deploymentProvider;
    this.repoName = builder.repoName;
    this.codeBuildProject = builder.codeBuildProject;
    this.s3BucketName = builder.s3BucketName;
    this.cloudFrontDistributionId = builder.cloudFrontDistributionId;
    this.githubUserName = builder.githubUserName;
  }

  public static class Builder {
    ResumeModel resume;
    WebsiteDetailsModel websiteDetails;
    String deploymentId;
    String userId;
    Long createdAt;
    String oAuthToken;
    DeploymentProvider deploymentProvider;
    String repoName;
    String codeBuildProject;
    String s3BucketName;
    String cloudFrontDistributionId;
    String githubUserName;

    public Builder setResume(final ResumeModel resume) {
      this.resume = resume;
      return this;
    }

    public Builder setWebsiteDetails(final WebsiteDetailsModel websiteDetails) {
      this.websiteDetails = websiteDetails;
      return this;
    }

    public Builder setDeploymentId(final String deploymentId) {
      this.deploymentId = deploymentId;
      return this;
    }

    public Builder setUserId(final String userId) {
      this.userId = userId;
      return this;
    }

    public Builder setCreatedAt(final Long createdAt) {
      this.createdAt = createdAt;
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

    public Builder setCodeBuildProject(final String codeBuildProject) {
      this.codeBuildProject = codeBuildProject;
      return this;
    }

    public Builder setS3BucketName(final String s3BucketName) {
      this.s3BucketName = s3BucketName;
      return this;
    }

    public Builder setCloudFrontDistributionId(final String cloudFrontDistributionId) {
      this.cloudFrontDistributionId = cloudFrontDistributionId;
      return this;
    }

    public Builder setGithubUserName(final String githubUserName) {
      this.githubUserName = githubUserName;
      return this;
    }

    public StaticSiteRequestModel build() {
      return new StaticSiteRequestModel(this);
    }
  }
}
