package com.portfolio.generator.models;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.HashMap;
import java.util.Map;

public class WebsiteDetailsModel {
  private String title;
  private String description;
  private String templateName;
  private String resumeFile;
  private String profilePictureFile;
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

  public String getResumeFile() {
    return resumeFile;
  }

  public void setResumeFile(final String resumeFile) {
    this.resumeFile = resumeFile;
  }

  public String getProfilePictureFile() {
    return profilePictureFile;
  }

  public void setProfilePictureFile(final String profilePictureFile) {
    this.profilePictureFile = profilePictureFile;
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
