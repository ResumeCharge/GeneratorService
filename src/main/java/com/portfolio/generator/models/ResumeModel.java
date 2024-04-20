package com.portfolio.generator.models;

import com.portfolio.generator.models.resumeModels.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Defines the fields we expect to have in the ResumeModel.
 * This class is a field in the GenerateStaticSiteRequestModel class.
 */
public class ResumeModel {
  private String userId;
  private String nickname;
  private long createdAt;
  private long lastUpdateAt;
  private PersonalDetailsModel personalDetails;
  private List<ExtraLinkModel> extraLinkList;
  private CareerSummaryModel careerSummary;
  private List<EducationModel> educationList;
  private List<WorkExperienceModel> workExperienceList;
  private List<ProjectModel> projectsList;
  private SkillsModel skills;
  private List<AwardAndAccolade> awardsAndAccoladesList;

  private AboutMeModel aboutMe;
  private Map<String, String> extraDetails = new HashMap<>();
  private String UUID;

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getNickname() {
    return nickname;
  }

  public void setNickname(String nickname) {
    this.nickname = nickname;
  }

  public long getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(long createdAt) {
    this.createdAt = createdAt;
  }

  public long getLastUpdateAt() {
    return lastUpdateAt;
  }

  public void setLastUpdateAt(long lastUpdateAt) {
    this.lastUpdateAt = lastUpdateAt;
  }

  public PersonalDetailsModel getPersonalDetails() {
    return personalDetails;
  }

  public void setPersonalDetails(PersonalDetailsModel personalDetails) {
    this.personalDetails = personalDetails;
  }

  public List<ExtraLinkModel> getExtraLinkList() {
    return extraLinkList;
  }

  public void setExtraLinkList(List<ExtraLinkModel> extraLinkList) {
    this.extraLinkList = extraLinkList;
  }

  public CareerSummaryModel getCareerSummary() {
    return careerSummary;
  }

  public void setCareerSummary(CareerSummaryModel careerSummary) {
    this.careerSummary = careerSummary;
  }

  public List<EducationModel> getEducationList() {
    return educationList;
  }

  public void setEducationList(List<EducationModel> educationList) {
    this.educationList = educationList;
  }

  public List<WorkExperienceModel> getWorkExperienceList() {
    return workExperienceList;
  }

  public void setWorkExperienceList(List<WorkExperienceModel> workExperienceList) {
    this.workExperienceList = workExperienceList;
  }

  public List<ProjectModel> getProjectsList() {
    return projectsList;
  }

  public void setProjectsList(List<ProjectModel> projectsList) {
    this.projectsList = projectsList;
  }

  public SkillsModel getSkills() {
    return skills;
  }

  public void setSkills(SkillsModel skills) {
    this.skills = skills;
  }

  public List<AwardAndAccolade> getAwardsAndAccoladesList() {
    return awardsAndAccoladesList;
  }

  public void setAwardsAndAccoladesList(List<AwardAndAccolade> awardsAndAccoladesList) {
    this.awardsAndAccoladesList = awardsAndAccoladesList;
  }

  public AboutMeModel getAboutMe() {
    return aboutMe;
  }

  public void setAboutMe(AboutMeModel aboutMe) {
    this.aboutMe = aboutMe;
  }

  public Map<String, String> getExtraDetails() {
    return extraDetails;
  }

  public void setExtraDetails(Map<String, String> extraDetails) {
    this.extraDetails = extraDetails;
  }

  public String getUUID() {
    return UUID;
  }

  public void setUUID(String UUID) {
    this.UUID = UUID;
  }


}
