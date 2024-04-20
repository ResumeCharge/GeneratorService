package com.portfolio.generator.utilities.aws.factories;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudfront.AmazonCloudFront;
import com.amazonaws.services.cloudfront.AmazonCloudFrontClientBuilder;
import com.amazonaws.services.codebuild.AWSCodeBuild;
import com.amazonaws.services.codebuild.AWSCodeBuildClient;
import com.amazonaws.services.codecommit.AWSCodeCommit;
import com.amazonaws.services.codecommit.AWSCodeCommitClient;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.stereotype.Component;

@Component
public class AWSClientFactory implements IAWSClientFactory {
  @Override
  public AWSCodeBuild getCodeBuildClient(final AWSCredentialsProvider awsCredentialsProvider,
                                         final Regions region) {
    return AWSCodeBuildClient
        .builder()
        .withCredentials(awsCredentialsProvider)
        .withRegion(region)
        .build();
  }

  @Override
  public AWSCodeCommit getCodeCommitClient(final AWSCredentialsProvider awsCredentialsProvider,
                                           final Regions region) {
    return AWSCodeCommitClient
        .builder()
        .withCredentials(awsCredentialsProvider)
        .withRegion(region)
        .build();
  }

  @Override
  public AmazonS3 getS3Client(final AWSCredentialsProvider awsCredentialsProvider, final Regions region) {
    return AmazonS3ClientBuilder.standard().withCredentials(awsCredentialsProvider).withRegion(region).build();
  }

  @Override
  public AmazonCloudFront getCloudFrontClient(final AWSCredentialsProvider awsCredentialsProvider, final Regions region) {
    return AmazonCloudFrontClientBuilder.standard().withCredentials(awsCredentialsProvider).withRegion(region).build();
  }
}
