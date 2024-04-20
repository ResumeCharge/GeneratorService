package com.portfolio.generator.models.resumeModels;

public class ExtraLinkModel {
  private String linkName;
  private String linkValue;

  public ExtraLinkModel() {
  }

  public ExtraLinkModel(String linkName, String linkValue) {
    this.linkName = linkName;
    this.linkValue = linkValue;
  }

  public String getLinkName() {
    return linkName;
  }

  public void setLinkName(String linkName) {
    this.linkName = linkName;
  }

  public String getLinkValue() {
    return linkValue;
  }

  public void setLinkValue(String linkValue) {
    this.linkValue = linkValue;
  }
}
