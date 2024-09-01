package com.portfolio.generator.models.databaseModels;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.portfolio.generator.models.WebsiteDetailsModel;
import com.portfolio.generator.models.transformers.deserializers.ObjectIdDeserializer;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public class DbDeploymentModel {
  @JsonDeserialize(using = ObjectIdDeserializer.class)
  @JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
  private String _id;
  private String resumeId;
  private String templateId;
  private WebsiteDetails websiteDetails;
  private String userId;
  private DeploymentStatus status;
  private Long lastUpdatedAt;
  private Double createdAt;
  private int __v;
  private Integer progress;
  private Integer retryCount;
  private String deployedUrl;
  private String deploymentProvider;
  private Boolean cancellationRequested;
  private String cloudFrontInvalidationId;
  private String githubUserName;

  // getters and setters for all the private variables
  public String get_id() {
    return _id;
  }

  public void set_id(final String _id) {
    this._id = _id;
  }

  public String getResumeId() {
    return resumeId;
  }

  public void setResumeId(final String resumeId) {
    this.resumeId = resumeId;
  }

  public String getTemplateId() {
    return templateId;
  }

  public void setTemplateId(final String templateId) {
    this.templateId = templateId;
  }

  public WebsiteDetails getWebsiteDetails() {
    return websiteDetails;
  }

  public void setWebsiteDetails(final WebsiteDetails websiteDetails) {
    this.websiteDetails = websiteDetails;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(final String userId) {
    this.userId = userId;
  }

  public DeploymentStatus getStatus() {
    return status;
  }

  public void setStatus(final DeploymentStatus status) {
    this.status = status;
  }

  public Long getLastUpdatedAt() {
    return lastUpdatedAt;
  }

  public void setLastUpdatedAt(final Long lastUpdatedAt) {
    this.lastUpdatedAt = lastUpdatedAt;
  }

  public Double getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(final Double createdAt) {
    this.createdAt = createdAt;
  }

  public int get__v() {
    return __v;
  }

  public void set__v(final int __v) {
    this.__v = __v;
  }

  public Integer getProgress() {
    return progress;
  }

  public void setProgress(final Integer progress) {
    this.progress = progress;
  }

  public Integer getRetryCount() {
    return retryCount;
  }

  public void setRetryCount(final Integer retryCount) {
    this.retryCount = retryCount;
  }

  public String getDeployedUrl() {
    return deployedUrl;
  }

  public void setDeployedUrl(final String deployedUrl) {
    this.deployedUrl = deployedUrl;
  }

  public String getDeploymentProvider() {
    return deploymentProvider;
  }

  public void setDeploymentProvider(final String deploymentProvider) {
    this.deploymentProvider = deploymentProvider;
  }

  public Boolean getCancellationRequested() {
    return cancellationRequested;
  }

  public void setCancellationRequested(final Boolean cancellationRequested) {
    this.cancellationRequested = cancellationRequested;
  }

  public String getCloudFrontInvalidationId() {
    return cloudFrontInvalidationId;
  }

  public void setCloudFrontInvalidationId(final String cloudFrontInvalidationId) {
    this.cloudFrontInvalidationId = cloudFrontInvalidationId;
  }

  public String getGithubUserName() {
    return githubUserName;
  }

  public void setGithubUserName(String githubUserName) {
    this.githubUserName = githubUserName;
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this);
  }

  // inner class for the websiteDetails field
  public static class WebsiteDetails {
    private String profilePictureFile;
    private String resumeFile;
    private String description;
    private String title;
    private String templateName;
    private String resumeName;
    private String websiteIdentifier;

    // getters and setters for all the private variables


    public String getProfilePictureFile() {
      return profilePictureFile;
    }

    public void setProfilePictureFile(String profilePictureFile) {
      this.profilePictureFile = profilePictureFile;
    }

    public String getResumeFile() {
      return resumeFile;
    }

    public void setResumeFile(String resumeFile) {
      this.resumeFile = resumeFile;
    }

    public String getDescription() {
      return description;
    }

    public void setDescription(final String description) {
      this.description = description;
    }

    public String getTitle() {
      return title;
    }

    public void setTitle(final String title) {
      this.title = title;
    }

    public String getTemplateName() {
      return templateName;
    }

    public void setTemplateName(final String templateName) {
      this.templateName = templateName;
    }

    public String getResumeName() {
      return resumeName;
    }

    public void setResumeName(final String resumeName) {
      this.resumeName = resumeName;
    }

    public String getWebsiteIdentifier() {
      return websiteIdentifier;
    }

    public void setWebsiteIdentifier(final String websiteIdentifier) {
      this.websiteIdentifier = websiteIdentifier;
    }

    public WebsiteDetailsModel toWebsiteDetailsModel() {
      final WebsiteDetailsModel websiteDetailsModel = new WebsiteDetailsModel();
      websiteDetailsModel.setProfilePictureFile(this.profilePictureFile);
      websiteDetailsModel.setTitle(this.title);
      websiteDetailsModel.setDescription(this.description);
      websiteDetailsModel.setTemplateName(this.templateName);
      websiteDetailsModel.setResumeFile(this.resumeFile);
      websiteDetailsModel.setWebsiteIdentifier(this.websiteIdentifier);
      websiteDetailsModel.setResumeName(this.resumeName);
      return websiteDetailsModel;
    }

    @Override
    public String toString() {
      return ReflectionToStringBuilder.toString(this);
    }
  }
}

