package com.portfolio.generator.models.resumeModels;

import java.util.List;

public class AwardAndAccolade {
  private String name;
  private String organization;
  private String description;
  private List<Integer> yearsObtained;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getOrganization() {
    return organization;
  }

  public void setOrganization(String organization) {
    this.organization = organization;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<Integer> getYearsObtained() {
    return yearsObtained;
  }

  public void setYearsObtained(List<Integer> yearsObtained) {
    this.yearsObtained = yearsObtained;
  }
}
