package com.portfolio.generator.utilities.helpers.s3;

import com.amazonaws.services.s3.AmazonS3;

public class CopyFolderRequest {
  public final String bucketName;
  public final String fromFolder;
  public final String toFolder;
  public final AmazonS3 s3Client;

  private CopyFolderRequest(final Builder builder) {
    this.bucketName = builder.bucketName;
    this.fromFolder = builder.fromFolder;
    this.toFolder = builder.toFolder;
    this.s3Client = builder.s3Client;
  }

  public static class Builder {
    String bucketName;
    String fromFolder;
    String toFolder;
    AmazonS3 s3Client;

    public Builder withBucketName(final String bucketName) {
      this.bucketName = bucketName;
      return this;
    }

    public Builder withFromFolder(final String fromFolder) {
      this.fromFolder = fromFolder;
      return this;
    }

    public Builder withToFolder(final String toFolder) {
      this.toFolder = toFolder;
      return this;
    }

    public Builder withS3Client(final AmazonS3 s3Client) {
      this.s3Client = s3Client;
      return this;
    }

    public CopyFolderRequest build() {
      return new CopyFolderRequest(this);
    }
  }
}