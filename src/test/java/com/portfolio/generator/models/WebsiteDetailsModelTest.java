package com.portfolio.generator.models;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import org.junit.jupiter.api.Test;

class WebsiteDetailsModelTest {

  @Test
  void test() {
    final WebsiteDetailsModel websiteDetails = new WebsiteDetailsModel();
    websiteDetails.setTitle("title");
    websiteDetails.setDescription("description");
    websiteDetails.setTemplateName("template");
    websiteDetails.setExtraConfigurationOptions(new HashMap<>());
    websiteDetails.setResumeFile("resume");
    websiteDetails.setProfilePictureFile("profilePicture");

    assertThat(websiteDetails.getTitle()).isEqualTo("title");
    assertThat(websiteDetails.getDescription()).isEqualTo("description");
    assertThat(websiteDetails.getTemplateName()).isEqualTo("template");
    assertThat(websiteDetails.getResumeFile()).isEqualTo("resume");
    assertThat(websiteDetails.getProfilePictureFile()).isEqualTo("profilePicture");
    assertThat(websiteDetails.getExtraConfigurationOptions().isEmpty()).isTrue();
    assertThat(websiteDetails.toString()).isNotNull();

  }

}