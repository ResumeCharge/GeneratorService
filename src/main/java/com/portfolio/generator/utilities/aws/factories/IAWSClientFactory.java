package com.portfolio.generator.utilities.aws.factories;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudfront.AmazonCloudFront;
import com.amazonaws.services.codebuild.AWSCodeBuild;
import com.amazonaws.services.codecommit.AWSCodeCommit;
import com.amazonaws.services.s3.AmazonS3;

/**
 * Creates new AWS clients for accessing AWS API.
 **/
public interface IAWSClientFactory {
  /**
   * Creates a new AWS CodeBuildClient
   **/
  AWSCodeBuild getCodeBuildClient(final AWSCredentialsProvider awsCredentialsProvider,
                                  final Regions region);


  /**
   * Creates a new AWS CodeCommitClient
   **/
  AWSCodeCommit getCodeCommitClient(final AWSCredentialsProvider awsCredentialsProvider,
                                    final Regions region);

  /**
   * Creates a new AWS S3 Client
   **/
  AmazonS3 getS3Client(final AWSCredentialsProvider awsCredentialsProvider,
                       final Regions region);

  /**
   * Creates a new AWS CloudFront Client
   **/
  AmazonCloudFront getCloudFrontClient(final AWSCredentialsProvider awsCredentialsProvider,
                                       final Regions region);
}
