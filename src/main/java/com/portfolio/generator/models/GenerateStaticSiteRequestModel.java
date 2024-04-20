package com.portfolio.generator.models;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

/**
 * Defines the body required when calling the createPortfolio
 * method in the StaticSiteController
 */
public class GenerateStaticSiteRequestModel {
  private ResumeModel resume;
  private WebsiteDetailsModel websiteDetails;
  private String deploymentId;
  private String userId;
  private String oAuthToken;
  private String deploymentProvider;

  public ResumeModel getResume() {
    return resume;
  }

  public void setResume(final ResumeModel resume) {
    this.resume = resume;
  }

  public WebsiteDetailsModel getWebsiteDetails() {
    return websiteDetails;
  }

  public void setWebsiteDetails(final WebsiteDetailsModel websiteDetails) {
    this.websiteDetails = websiteDetails;
  }

  public String getDeploymentId() {
    return deploymentId;
  }

  public void setDeploymentId(final String deploymentId) {
    this.deploymentId = deploymentId;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(final String userId) {
    this.userId = userId;
  }

  public String getoAuthToken() {
    return oAuthToken;
  }

  public void setoAuthToken(final String oAuthToken) {
    this.oAuthToken = oAuthToken;
  }

  public String getDeploymentProvider() {
    return deploymentProvider;
  }

  public void setDeploymentProvider(final String deploymentProvider) {
    this.deploymentProvider = deploymentProvider;
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this);
  }
}