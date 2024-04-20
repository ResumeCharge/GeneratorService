package com.portfolio.generator.models.resumeModels;

public class EducationModel {
  private String degree;
  private String university;
  private String startDate;
  private String endDate;
  private String details;

  public EducationModel() {
  }

  public EducationModel(
      String degree, String university, String startDate, String endDate, String details
  ) {
    this.degree = degree;
    this.university = university;
    this.startDate = startDate;
    this.endDate = endDate;
    this.details = details;
  }

  public String getDegree() {
    return degree;
  }

  public void setDegree(String degree) {
    this.degree = degree;
  }

  public String getUniversity() {
    return university;
  }

  public void setUniversity(String university) {
    this.university = university;
  }

  public String getStartDate() {
    return startDate;
  }

  public void setStartDate(String startDate) {
    this.startDate = startDate;
  }

  public String getEndDate() {
    return endDate;
  }

  public void setEndDate(String endDate) {
    this.endDate = endDate;
  }

  public String getDetails() {
    return details;
  }

  public void setDetails(String details) {
    this.details = details;
  }
}
