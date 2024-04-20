package com.portfolio.generator.utilities.aws.factories;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudfront.AmazonCloudFront;
import com.amazonaws.services.codebuild.AWSCodeBuild;
import com.amazonaws.services.codecommit.AWSCodeCommit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class AWSClientFactoryTest {
  @Mock
  private AWSCredentialsProvider awsCredentialsProvider;
  private AWSClientFactory awsClientFactory;

  @BeforeEach
  void setUp() {
    awsClientFactory = new AWSClientFactory();
  }

  @Test
  void getCodeBuildClient() {
    final AWSCodeBuild codeBuild = awsClientFactory.getCodeBuildClient(awsCredentialsProvider, Regions.US_EAST_1);
    assertThat(codeBuild).isNotNull();
  }

  @Test
  void getCodeCommitClient() {
    final AWSCodeCommit codeCommit = awsClientFactory.getCodeCommitClient(awsCredentialsProvider, Regions.US_EAST_1);
    assertThat(codeCommit).isNotNull();
  }

  @Test
  void getCloudFrontClient() {
    final AmazonCloudFront cloudFront = awsClientFactory.getCloudFrontClient(awsCredentialsProvider, Regions.US_EAST_1);
    assertThat(cloudFront).isNotNull();
  }
}