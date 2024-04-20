package com.portfolio.generator.utilities.aws.factories;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3URI;

/**
 * Factory for creating S3 objects, largely created to ease testing
 * **/
public interface IS3ObjectFactory {
  AmazonS3 getAmazonS3Client();
  AmazonS3URI getAmazonS3URI(final String uri);
}
