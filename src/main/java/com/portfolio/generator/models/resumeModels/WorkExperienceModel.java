package com.portfolio.generator.models.resumeModels;

public class WorkExperienceModel {
  private String roleName;
  private String company;
  private String location;
  private String startDate;
  private String endDate;
  private String details;

  public WorkExperienceModel() {
  }

  public WorkExperienceModel(
      String roleName, String company, String location, String startDate, String endDate,
      String details
  ) {
    this.roleName = roleName;
    this.company = company;
    this.location = location;
    this.startDate = startDate;
    this.endDate = endDate;
    this.details = details;
  }

  public String getRoleName() {
    return roleName;
  }

  public void setRoleName(String roleName) {
    this.roleName = roleName;
  }

  public String getCompany() {
    return company;
  }

  public void setCompany(String company) {
    this.company = company;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
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
