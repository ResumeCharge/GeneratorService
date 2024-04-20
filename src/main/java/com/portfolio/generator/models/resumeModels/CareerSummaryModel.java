package com.portfolio.generator.models.resumeModels;

public class CareerSummaryModel {
  private String summary;

  public CareerSummaryModel() {
  }

  public CareerSummaryModel(String summary) {
    this.summary = summary;
  }

  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }
}
