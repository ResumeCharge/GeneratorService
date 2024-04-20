package com.portfolio.generator.models;

import static org.assertj.core.api.Assertions.assertThat;

import com.portfolio.generator.models.resumeModels.*;

import java.util.Collections;
import java.util.HashMap;
import org.junit.jupiter.api.Test;

class ResumeModelTest {

  @Test
  void test() {
    final ResumeModel resume = new ResumeModel();
    resume.setUserId("userId");
    resume.setNickname("nickname");
    resume.setUUID("uuid");
    resume.setCreatedAt(1L);
    resume.setLastUpdateAt(1L);
    resume.setPersonalDetails(new PersonalDetailsModel());
    resume.setExtraDetails(new HashMap<>());
    resume.setExtraLinkList(Collections.singletonList(new ExtraLinkModel()));
    resume.setCareerSummary(new CareerSummaryModel());
    resume.setWorkExperienceList(Collections.singletonList(new WorkExperienceModel()));
    resume.setProjectsList(Collections.singletonList(new ProjectModel()));
    resume.setSkills(new SkillsModel());
    resume.setEducationList(Collections.singletonList(new EducationModel()));
    resume.setAwardsAndAccoladesList(Collections.singletonList(new AwardAndAccolade()));
    resume.setAboutMe(new AboutMeModel());

    assertThat(resume.getUserId()).isEqualTo("userId");
    assertThat(resume.getNickname()).isEqualTo("nickname");
    assertThat(resume.getCreatedAt()).isEqualTo(1L);
    assertThat(resume.getLastUpdateAt()).isEqualTo(1L);
    assertThat(resume.getPersonalDetails()).isNotNull();
    assertThat(resume.getExtraLinkList().size()).isEqualTo(1);
    assertThat(resume.getCareerSummary()).isNotNull();
    assertThat(resume.getEducationList().size()).isEqualTo(1);
    assertThat(resume.getWorkExperienceList().size()).isEqualTo(1);
    assertThat(resume.getProjectsList().size()).isEqualTo(1);
    assertThat(resume.getSkills()).isNotNull();
    assertThat(resume.getAwardsAndAccoladesList().size()).isEqualTo(1);
    assertThat(resume.getAboutMe()).isNotNull();
    assertThat(resume.getExtraDetails().isEmpty()).isTrue();
    assertThat(resume.getUUID()).isEqualTo("uuid");
  }

}