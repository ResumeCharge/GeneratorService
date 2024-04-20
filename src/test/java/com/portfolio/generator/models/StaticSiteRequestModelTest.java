package com.portfolio.generator.models;

import com.portfolio.generator.models.staticsite.StaticSiteRequestModel;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StaticSiteRequestModelTest {

  @Test
  void test() {
    final StaticSiteRequestModel request = new StaticSiteRequestModel
        .Builder()
        .setResume(new ResumeModel())
        .setWebsiteDetails(new WebsiteDetailsModel())
        .setDeploymentId("1")
        .setUserId("1")
        .setoAuthToken("1")
        .setCreatedAt(1L)
        .build();
    assertThat(request.resume).isNotNull();
    assertThat(request.websiteDetails).isNotNull();
    assertThat(request.createdAt).isEqualTo(1L);
    assertThat(request.deploymentId).isEqualTo("1");
    assertThat(request.userId).isEqualTo("1");
    assertThat(request.oAuthToken).isEqualTo("1");
  }
}