package com.portfolio.generator.utilities.aws.factories;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.AmazonS3URI;
import org.springframework.stereotype.Component;

@Component
public class S3ObjectFactory implements IS3ObjectFactory {
  @Override
  public AmazonS3 getAmazonS3Client() {
    return AmazonS3ClientBuilder.standard().withRegion(Regions.US_WEST_2).build();
  }

  @Override
  public AmazonS3URI getAmazonS3URI(final String uri) {
    return new AmazonS3URI(uri);
  }
}
