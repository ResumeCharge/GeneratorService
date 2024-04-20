package com.portfolio.generator.models.databaseModels;

import com.portfolio.generator.models.ResumeModel;
import com.portfolio.generator.models.resumeModels.*;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.stream.Collectors;

public class DbResumeModel {
  private ObjectId _id;
  private int __v;
  private AboutMe aboutMe;
  private List<AwardAndAccolade> awardsAndAccoladesList;
  private CareerSummary careerSummary;
  private long createdAt;
  private List<Education> educationList;
  private List<ExtraLink> extraLinkList;
  private long lastUpdatedAt;
  private String nickname;
  private PersonalDetails personalDetails;
  private List<Project> projectsList;
  private Skills skills;
  private String userId;
  private List<WorkExperience> workExperienceList;

  public ObjectId get_id() {
    return _id;
  }

  public void set_id(final ObjectId _id) {
    this._id = _id;
  }

  public int get__v() {
    return __v;
  }

  public void set__v(final int __v) {
    this.__v = __v;
  }

  public AboutMe getAboutMe() {
    return aboutMe;
  }

  public void setAboutMe(AboutMe aboutMe) {
    this.aboutMe = aboutMe;
  }

  public List<AwardAndAccolade> getAwardsAndAccoladesList() {
    return awardsAndAccoladesList;
  }

  public void setAwardsAndAccoladesList(final List<AwardAndAccolade> awardsAndAccoladesList) {
    this.awardsAndAccoladesList = awardsAndAccoladesList;
  }

  public CareerSummary getCareerSummary() {
    return careerSummary;
  }

  public void setCareerSummary(final CareerSummary careerSummary) {
    this.careerSummary = careerSummary;
  }

  public long getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(final long createdAt) {
    this.createdAt = createdAt;
  }

  public List<Education> getEducationList() {
    return educationList;
  }

  public void setEducationList(final List<Education> educationList) {
    this.educationList = educationList;
  }

  public List<ExtraLink> getExtraLinkList() {
    return extraLinkList;
  }

  public void setExtraLinkList(final List<ExtraLink> extraLinkList) {
    this.extraLinkList = extraLinkList;
  }

  public long getLastUpdatedAt() {
    return lastUpdatedAt;
  }

  public void setLastUpdatedAt(final long lastUpdatedAt) {
    this.lastUpdatedAt = lastUpdatedAt;
  }

  public String getNickname() {
    return nickname;
  }

  public void setNickname(final String nickname) {
    this.nickname = nickname;
  }

  public PersonalDetails getPersonalDetails() {
    return personalDetails;
  }

  public void setPersonalDetails(final PersonalDetails personalDetails) {
    this.personalDetails = personalDetails;
  }

  public List<Project> getProjectsList() {
    return projectsList;
  }

  public void setProjectsList(final List<Project> projectsList) {
    this.projectsList = projectsList;
  }

  public Skills getSkills() {
    return skills;
  }

  public void setSkills(final Skills skills) {
    this.skills = skills;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(final String userId) {
    this.userId = userId;
  }

  public List<WorkExperience> getWorkExperienceList() {
    return workExperienceList;
  }

  public void setWorkExperienceList(final List<WorkExperience> workExperienceList) {
    this.workExperienceList = workExperienceList;
  }

  public ResumeModel toResumeModel() {
    final ResumeModel resumeModel = new ResumeModel();
    resumeModel.setUserId(this.userId);
    resumeModel.setNickname(this.nickname);
    resumeModel.setCreatedAt(this.createdAt);
    resumeModel.setLastUpdateAt(this.lastUpdatedAt);
    resumeModel.setProjectsList(
        this.projectsList.stream().map(Project::toProjectModel).collect(Collectors.toList()));
    resumeModel.setPersonalDetails(this.personalDetails.toPersonalDetailsModel());
    resumeModel.setExtraLinkList(
        this.extraLinkList.stream().map(ExtraLink::toExtraLinkModel).collect(Collectors.toList()));
    resumeModel.setCareerSummary(this.careerSummary.toCareerSummaryModel());
    resumeModel.setEducationList(
        this.educationList.stream().map(Education::toEducationModel).collect(Collectors.toList()));
    resumeModel.setWorkExperienceList(
        this.workExperienceList.stream().map(WorkExperience::toWorkExperienceModel)
            .collect(Collectors.toList()));
    resumeModel.setSkills(this.skills.toSkillsModel());
    resumeModel.setAwardsAndAccoladesList(
        this.awardsAndAccoladesList.stream().map(AwardAndAccolade::toAwardAndAccoladeModel)
            .collect(Collectors.toList()));
    resumeModel.setAboutMe(this.aboutMe.toAboutMeModel());
    return resumeModel;
  }

  public static class CareerSummary {
    private String summary;

    public String getSummary() {
      return summary;
    }

    public void setSummary(final String summary) {
      this.summary = summary;
    }

    public CareerSummaryModel toCareerSummaryModel() {
      final CareerSummaryModel careerSummaryModel = new CareerSummaryModel();
      careerSummaryModel.setSummary(this.summary);
      return careerSummaryModel;
    }
  }

  public static class Education {
    private String details;
    private String endDate;
    private String startDate;
    private String university;
    private String degree;

    public String getDetails() {
      return details;
    }

    public void setDetails(final String details) {
      this.details = details;
    }

    public String getEndDate() {
      return endDate;
    }

    public void setEndDate(final String endDate) {
      this.endDate = endDate;
    }

    public String getStartDate() {
      return startDate;
    }

    public void setStartDate(final String startDate) {
      this.startDate = startDate;
    }

    public String getUniversity() {
      return university;
    }

    public void setUniversity(final String university) {
      this.university = university;
    }

    public String getDegree() {
      return degree;
    }

    public void setDegree(final String degree) {
      this.degree = degree;
    }

    public EducationModel toEducationModel() {
      final EducationModel educationModel = new EducationModel();
      educationModel.setDetails(this.details);
      educationModel.setStartDate(this.startDate);
      educationModel.setEndDate(this.endDate);
      educationModel.setDegree(this.degree);
      educationModel.setUniversity(this.university);
      return educationModel;
    }
  }

  public static class ExtraLink {
    private String linkValue;
    private String linkName;

    public String getLinkValue() {
      return linkValue;
    }

    public void setLinkValue(final String linkValue) {
      this.linkValue = linkValue;
    }

    public String getLinkName() {
      return linkName;
    }

    public void setLinkName(final String linkName) {
      this.linkName = linkName;
    }

    public ExtraLinkModel toExtraLinkModel() {
      final ExtraLinkModel extraLinkModel = new ExtraLinkModel();
      extraLinkModel.setLinkValue(this.linkValue);
      extraLinkModel.setLinkName(this.linkName);
      return extraLinkModel;
    }
  }

  public static class PersonalDetails {
    private List<Language> languages;
    private String github;
    private String linkedin;
    private String website;
    private String phone;
    private String email;
    private String avatar;
    private String lastName;
    private String firstName;

    public List<Language> getLanguages() {
      return languages;
    }

    public void setLanguages(final List<Language> languages) {
      this.languages = languages;
    }

    public String getGithub() {
      return github;
    }

    public void setGithub(final String github) {
      this.github = github;
    }

    public String getLinkedin() {
      return linkedin;
    }

    public void setLinkedin(final String linkedin) {
      this.linkedin = linkedin;
    }

    public String getWebsite() {
      return website;
    }

    public void setWebsite(final String website) {
      this.website = website;
    }

    public String getPhone() {
      return phone;
    }

    public void setPhone(final String phone) {
      this.phone = phone;
    }

    public String getEmail() {
      return email;
    }

    public void setEmail(final String email) {
      this.email = email;
    }

    public String getAvatar() {
      return avatar;
    }

    public void setAvatar(final String avatar) {
      this.avatar = avatar;
    }

    public String getLastName() {
      return lastName;
    }

    public void setLastName(final String lastName) {
      this.lastName = lastName;
    }

    public String getFirstName() {
      return firstName;
    }

    public void setFirstName(final String firstName) {
      this.firstName = firstName;
    }

    public PersonalDetailsModel toPersonalDetailsModel() {
      final PersonalDetailsModel personalDetailsModel = new PersonalDetailsModel();
      personalDetailsModel.setLanguages(
          this.languages.stream().map(language -> new LanguageModel(language.name, language.level))
              .collect(Collectors.toList()));
      personalDetailsModel.setLinkedin(this.linkedin);
      personalDetailsModel.setWebsite(this.website);
      personalDetailsModel.setPhone(this.phone);
      personalDetailsModel.setEmail(this.email);
      personalDetailsModel.setAvatar(this.avatar);
      personalDetailsModel.setLastName(this.lastName);
      personalDetailsModel.setFirstName(this.firstName);
      personalDetailsModel.setGithub(this.github);
      return personalDetailsModel;
    }

    public class Language {
      private final String name;
      private final String level;

      public Language(final String name, final String level) {
        this.name = name;
        this.level = level;
      }

      public String getName() {
        return name;
      }

      public String getLevel() {
        return level;
      }
    }
  }

  public static class Project {
    private String details;
    private String title;

    public String getDetails() {
      return details;
    }

    public void setDetails(final String details) {
      this.details = details;
    }

    public String getTitle() {
      return title;
    }

    public void setTitle(final String title) {
      this.title = title;
    }

    public ProjectModel toProjectModel() {
      final ProjectModel projectModel = new ProjectModel();
      projectModel.setTitle(this.title);
      projectModel.setDetails(this.details);
      return projectModel;
    }
  }

  public static class WorkExperience {
    private String details;
    private String endDate;
    private String startDate;
    private String location;
    private String company;
    private String roleName;

    public String getDetails() {
      return details;
    }

    public void setDetails(final String details) {
      this.details = details;
    }

    public String getEndDate() {
      return endDate;
    }

    public void setEndDate(final String endDate) {
      this.endDate = endDate;
    }

    public String getStartDate() {
      return startDate;
    }

    public void setStartDate(final String startDate) {
      this.startDate = startDate;
    }

    public String getLocation() {
      return location;
    }

    public void setLocation(final String location) {
      this.location = location;
    }

    public String getCompany() {
      return company;
    }

    public void setCompany(final String company) {
      this.company = company;
    }

    public String getRoleName() {
      return roleName;
    }

    public void setRoleName(final String roleName) {
      this.roleName = roleName;
    }

    public WorkExperienceModel toWorkExperienceModel() {
      final WorkExperienceModel workExperienceModel = new WorkExperienceModel();
      workExperienceModel.setDetails(this.details);
      workExperienceModel.setStartDate(this.startDate);
      workExperienceModel.setEndDate(this.endDate);
      workExperienceModel.setLocation(this.location);
      workExperienceModel.setRoleName(this.roleName);
      workExperienceModel.setCompany(this.company);
      return workExperienceModel;
    }
  }

  public static class Skills {
    private String skills;

    public SkillsModel toSkillsModel() {
      final SkillsModel skillsModel = new SkillsModel();
      skillsModel.setSkills(this.skills);
      return skillsModel;
    }

    public String getSkills() {
      return skills;
    }

    public void setSkills(String skills) {
      this.skills = skills;
    }
  }

  public static class AboutMe {
    private String aboutMe;

    public AboutMeModel toAboutMeModel() {
      final AboutMeModel aboutMeModel = new AboutMeModel();
      aboutMeModel.setAboutMe(this.aboutMe);
      return aboutMeModel;
    }

    public String getAboutMe() {
      return aboutMe;
    }

    public void setAboutMe(String aboutMe) {
      this.aboutMe = aboutMe;
    }
  }

  public static class AwardAndAccolade {
    private String name;
    private String organization;
    private String description;
    private List<Integer> yearsObtained;

    public String getName() {
      return name;
    }

    public void setName(final String name) {
      this.name = name;
    }

    public String getOrganization() {
      return organization;
    }

    public void setOrganization(final String organization) {
      this.organization = organization;
    }

    public String getDescription() {
      return description;
    }

    public void setDescription(final String description) {
      this.description = description;
    }

    public List<Integer> getYearsObtained() {
      return yearsObtained;
    }

    public void setYearsObtained(final List<Integer> yearsObtained) {
      this.yearsObtained = yearsObtained;
    }

    public com.portfolio.generator.models.resumeModels.AwardAndAccolade toAwardAndAccoladeModel() {
      final com.portfolio.generator.models.resumeModels.AwardAndAccolade awardAndAccolade =
          new com.portfolio.generator.models.resumeModels.AwardAndAccolade();
      awardAndAccolade.setYearsObtained(this.yearsObtained);
      awardAndAccolade.setOrganization(this.organization);
      awardAndAccolade.setName(this.name);
      awardAndAccolade.setDescription(this.description);
      return awardAndAccolade;
    }
  }
}
