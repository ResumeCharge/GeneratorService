package com.portfolio.generator.utilities.helpers.s3;

import com.amazonaws.services.s3.AmazonS3;

import java.nio.file.Path;

/**
 * Helper for downloading objects from the project S3 bucket
 **/
public interface IS3BucketHelper {
  /**
   * Download an item from the project S3 bucket to the specific output location
   **/
  void downloadFromS3Bucket(final AmazonS3 s3Client, final String keyName, final Path outputLocation);

  /**
   * Copies a folder to a new location in the bucket, does not modify the original folder
   **/
  void copyFolder(final CopyFolderRequest copyFolderRequest) throws InterruptedException;

  /**
   * Deletes a folder in the S3 bucket
   ***/
  void deleteFolder(final AmazonS3 s3Client, final String folder, final String bucketName) throws InterruptedException;

  boolean doesFolderExist(final AmazonS3 s3Client, final String folder, final String bucketName);
}
