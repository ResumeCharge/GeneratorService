package com.portfolio.generator.models.resumeModels;

import static org.assertj.core.api.Assertions.assertThat;

import com.portfolio.generator.models.ActionsModel;
import java.util.Collections;
import org.junit.jupiter.api.Test;

public class ResumeModelsTest {

  @Test
  void test() {
    /*AwardAndAccolade*/
    final AwardAndAccolade awardAndAccolade = new AwardAndAccolade();
    awardAndAccolade.setName("name");
    awardAndAccolade.setDescription("description");
    awardAndAccolade.setOrganization("organization");
    awardAndAccolade.setYearsObtained(Collections.singletonList(1));
    assertThat(awardAndAccolade.getName()).isEqualTo("name");
    assertThat(awardAndAccolade.getDescription()).isEqualTo("description");
    assertThat(awardAndAccolade.getOrganization()).isEqualTo("organization");
    assertThat(awardAndAccolade.getYearsObtained().size()).isEqualTo(1);
    /*CareerSummary*/
    final CareerSummaryModel careerSummary = new CareerSummaryModel();
    careerSummary.setSummary("summary");
    assertThat(careerSummary.getSummary()).isEqualTo("summary");
    /*Education*/
    final EducationModel educationModel = new EducationModel();
    educationModel.setDegree("degree");
    educationModel.setUniversity("university");
    educationModel.setStartDate("startdate");
    educationModel.setEndDate("enddate");
    educationModel.setDetails("details");
    assertThat(educationModel.getDegree()).isEqualTo("degree");
    assertThat(educationModel.getUniversity()).isEqualTo("university");
    assertThat(educationModel.getStartDate()).isEqualTo("startdate");
    assertThat(educationModel.getEndDate()).isEqualTo("enddate");
    assertThat(educationModel.getDetails()).isEqualTo("details");
    /*ExtraLink*/
    final ExtraLinkModel extraLink = new ExtraLinkModel();
    extraLink.setLinkName("name");
    extraLink.setLinkValue("value");
    assertThat(extraLink.getLinkValue()).isEqualTo("value");
    assertThat(extraLink.getLinkName()).isEqualTo("name");
    /*Instructions*/
    final InstructionsModel instructionsModel = new InstructionsModel();
    instructionsModel.setActions(new ActionsModel[] {new ActionsModel()});
    assertThat(instructionsModel.getActions().length).isEqualTo(1);
    /*Language*/
    final LanguageModel languageModel = new LanguageModel();
    languageModel.setName("name");
    languageModel.setLevel("level");
    assertThat(languageModel.getLevel()).isEqualTo("level");
    assertThat(languageModel.getName()).isEqualTo("name");
    /*PersonalDetails*/
    final PersonalDetailsModel personalDetailsModel = new PersonalDetailsModel();
    personalDetailsModel.setFirstName("fname");
    personalDetailsModel.setLastName("lname");
    personalDetailsModel.setPositionTitle("position");
    personalDetailsModel.setLocation("location");
    personalDetailsModel.setTagline("tagline");
    personalDetailsModel.setAvatar("avatar");
    personalDetailsModel.setEmail("email");
    personalDetailsModel.setPhone("phone");
    personalDetailsModel.setWebsite("website");
    personalDetailsModel.setLinkedin("linkedin");
    personalDetailsModel.setGithub("github");
    personalDetailsModel.setLanguages(Collections.singletonList(new LanguageModel()));
    assertThat(personalDetailsModel.getFirstName()).isEqualTo("fname");
    assertThat(personalDetailsModel.getLastName()).isEqualTo("lname");
    assertThat(personalDetailsModel.getPositionTitle()).isEqualTo("position");
    assertThat(personalDetailsModel.getLocation()).isEqualTo("location");
    assertThat(personalDetailsModel.getTagline()).isEqualTo("tagline");
    assertThat(personalDetailsModel.getAvatar()).isEqualTo("avatar");
    assertThat(personalDetailsModel.getEmail()).isEqualTo("email");
    assertThat(personalDetailsModel.getPhone()).isEqualTo("phone");
    assertThat(personalDetailsModel.getWebsite()).isEqualTo("website");
    assertThat(personalDetailsModel.getLinkedin()).isEqualTo("linkedin");
    assertThat(personalDetailsModel.getGithub()).isEqualTo("github");
    assertThat(personalDetailsModel.getLanguages().size()).isEqualTo(1);
    /*Project*/
    final ProjectModel project = new ProjectModel();
    project.setDetails("details");
    project.setTitle("title");
    assertThat(project.getTitle()).isEqualTo("title");
    assertThat(project.getDetails()).isEqualTo("details");
    /*WorkExperience*/
    final WorkExperienceModel workExperience = new WorkExperienceModel();
    workExperience.setRoleName("role");
    workExperience.setCompany("company");
    workExperience.setLocation("location");
    workExperience.setStartDate("startdate");
    workExperience.setEndDate("enddate");
    workExperience.setDetails("details");

    assertThat(workExperience.getRoleName()).isEqualTo("role");
    assertThat(workExperience.getCompany()).isEqualTo("company");
    assertThat(workExperience.getLocation()).isEqualTo("location");
    assertThat(workExperience.getStartDate()).isEqualTo("startdate");
    assertThat(workExperience.getEndDate()).isEqualTo("enddate");
    assertThat(workExperience.getDetails()).isEqualTo("details");
  }
}
