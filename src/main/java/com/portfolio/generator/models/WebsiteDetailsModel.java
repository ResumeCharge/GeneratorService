package com.portfolio.generator.models;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.HashMap;
import java.util.Map;

public class WebsiteDetailsModel {
  private String title;
  private String description;
  private String templateName;
  private String resumeS3URI;
  private String profilePictureS3URI;
  private String websiteIdentifier;
  private String resumeName;
  private Map<String, String> extraConfigurationOptions = new HashMap<>();

  public String getTitle() {
    return title;
  }

  public void setTitle(final String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(final String description) {
    this.description = description;
  }

  public String getTemplateName() {
    return templateName;
  }

  public void setTemplateName(final String templateName) {
    this.templateName = templateName;
  }

  public String getResumeS3URI() {
    return resumeS3URI;
  }

  public void setResumeS3URI(final String resumeS3URI) {
    this.resumeS3URI = resumeS3URI;
  }

  public String getProfilePictureS3URI() {
    return profilePictureS3URI;
  }

  public void setProfilePictureS3URI(final String profilePictureS3URI) {
    this.profilePictureS3URI = profilePictureS3URI;
  }

  public Map<String, String> getExtraConfigurationOptions() {
    return extraConfigurationOptions;
  }

  public void setExtraConfigurationOptions(
      final Map<String, String> extraConfigurationOptions
  ) {
    this.extraConfigurationOptions = extraConfigurationOptions;
  }

  public String getWebsiteIdentifier() {
    return websiteIdentifier;
  }

  public void setWebsiteIdentifier(final String websiteIdentifier) {
    this.websiteIdentifier = websiteIdentifier;
  }

  public String getResumeName() {
    return resumeName;
  }

  public void setResumeName(final String resumeName) {
    this.resumeName = resumeName;
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this);
  }
}
