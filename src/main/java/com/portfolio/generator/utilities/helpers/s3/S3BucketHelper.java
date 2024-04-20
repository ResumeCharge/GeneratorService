package com.portfolio.generator.utilities.helpers.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.portfolio.generator.concurrency.factory.IExecutorServiceFactory;
import com.portfolio.generator.utilities.IO.IIOFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;


@Component
public class S3BucketHelper implements IS3BucketHelper {
  private static final int DELETE_OBJECTS_MAX_KEYS = 1000;
  private static final String BUCKET_NAME = "resume-charge-resources";
  private static final Logger log = LoggerFactory.getLogger(S3BucketHelper.class);
  private final IIOFactory ioFactory;
  private final IExecutorServiceFactory executorServiceFactory;

  public S3BucketHelper(final IIOFactory ioFactory,
                        final IExecutorServiceFactory executorServiceFactory) {
    this.ioFactory = ioFactory;
    this.executorServiceFactory = executorServiceFactory;
  }

  @Override
  public void downloadFromS3Bucket(final AmazonS3 s3Client, final String keyName, final Path outputLocation) {
    log.info(String.format("Downloading %s from S3 bucket %s...\n", keyName, BUCKET_NAME));
    try {
      final S3Object s3BucketObject = s3Client.getObject(BUCKET_NAME, keyName);
      final S3ObjectInputStream s3ObjectInputStream = s3BucketObject.getObjectContent();
      final FileOutputStream fileOutputStream = ioFactory.createFileOutputStreamUsingFileUtils(outputLocation.toFile());
      writeS3InputStreamToOutputStreamAndClose(s3ObjectInputStream, fileOutputStream);
    } catch (final Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  @Override
  public void copyFolder(final CopyFolderRequest copyFolderRequest) throws InterruptedException {
    final ExecutorService executorService = executorServiceFactory.getFixedSizedThreadPool(10);
    final CompletionService<CopyObjectResult> completionService = new ExecutorCompletionService<>(executorService);
    final AmazonS3 s3Client = copyFolderRequest.s3Client;
    final String bucketName = copyFolderRequest.bucketName;
    final String fromFolder = copyFolderRequest.fromFolder;
    final ListObjectsRequest listObjectsInFromFolder = new ListObjectsRequest()
        .withBucketName(bucketName)
        .withPrefix(fromFolder + "/");
    final ObjectListing objects = s3Client.listObjects(listObjectsInFromFolder);
    final String[] keys = objects.getObjectSummaries().stream()
        .filter(Objects::nonNull)
        .map(S3ObjectSummary::getKey)
        .toArray(String[]::new);
    final List<CopyObjectCallable> callables = getCallablesForCopyObjectsRequest(keys, copyFolderRequest);
    final int totalTasks = callables.size();
    int completedTasks = 0;
    for (final CopyObjectCallable callable : callables) {
      completionService.submit(callable);
    }
    while (completedTasks != totalTasks) {
      completionService.take();
      completedTasks++;
    }
  }

  @Override
  public void deleteFolder(final AmazonS3 s3Client,
                           final String folder,
                           final String bucketName) throws InterruptedException {
    final ExecutorService executorService = executorServiceFactory.getFixedSizedThreadPool(10);
    final CompletionService<DeleteObjectsResult> completionService = new ExecutorCompletionService<>(executorService);
    final ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
        .withBucketName(bucketName)
        .withPrefix(folder + "/");
    final ObjectListing objects = s3Client.listObjects(listObjectsRequest);
    final String[] keys = objects.getObjectSummaries().stream()
        .filter(Objects::nonNull)
        .map(S3ObjectSummary::getKey)
        .toArray(String[]::new);
    final List<DeleteObjectsCallable> callables = getCallablesForDeleteObjectsRequest(keys, bucketName, s3Client);
    final int totalTasks = callables.size();
    int completedTasks = 0;
    for (final DeleteObjectsCallable callable : callables) {
      completionService.submit(callable);
    }
    while (completedTasks != totalTasks) {
      completionService.take();
      completedTasks++;
    }
  }

  private List<DeleteObjectsCallable> getCallablesForDeleteObjectsRequest(final String[] keys,
                                                                          final String bucketName,
                                                                          final AmazonS3 s3Client) {
    int keysInRequest = 0;
    final int totalKeys = keys.length;
    final List<DeleteObjectsCallable> callables = new ArrayList<>();
    final List<String> keyBatch = new ArrayList<>();
    for (final String key : keys) {
      keyBatch.add(key);
      keysInRequest++;
      if (keysInRequest == totalKeys || keysInRequest == DELETE_OBJECTS_MAX_KEYS) {
        final DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucketName)
            .withKeys(keyBatch.toArray(String[]::new));
        final DeleteObjectsCallable deleteObjectsCallable = new DeleteObjectsCallable(s3Client, deleteObjectsRequest);
        callables.add(deleteObjectsCallable);
        keysInRequest = 0;
        keyBatch.clear();
      }
    }
    return callables;
  }

  private List<CopyObjectCallable> getCallablesForCopyObjectsRequest(final String[] keys,
                                                                     final CopyFolderRequest copyFolderRequest) {
    final List<CopyObjectCallable> callables = new ArrayList<>();
    for (final String sourceKey : keys) {
      final String destinationKey = getNewObjectKey(sourceKey, copyFolderRequest.toFolder);
      final CopyObjectRequest copyObjectRequest = new CopyObjectRequest()
          .withSourceBucketName(copyFolderRequest.bucketName)
          .withSourceKey(sourceKey)
          .withDestinationKey(destinationKey)
          .withDestinationBucketName(copyFolderRequest.bucketName);
      final CopyObjectCallable callable = new CopyObjectCallable(copyFolderRequest.s3Client, copyObjectRequest);
      callables.add(callable);
    }
    return callables;
  }

  private String getNewObjectKey(final String key,
                                 final String toFolder) {
    final int fromFolderDelimiterPosition = key.indexOf('/');
    final String keyWithFromFolderRemoved = key.substring(fromFolderDelimiterPosition + 1);
    return toFolder + "/" + keyWithFromFolderRemoved;
  }


  private void writeS3InputStreamToOutputStreamAndClose(
      final S3ObjectInputStream s3ObjectInputStream,
      final FileOutputStream fileOutputStream
  )
      throws IOException {
    final byte[] read_buf = new byte[1024];
    int read_len = 0;
    while ((read_len = s3ObjectInputStream.read(read_buf)) > 0) {
      fileOutputStream.write(read_buf, 0, read_len);
    }
    s3ObjectInputStream.close();
    fileOutputStream.close();
  }

  @Override
  public boolean doesFolderExist(final AmazonS3 s3Client, final String folder, final String bucketName) {
    final ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
        .withBucketName(bucketName)
        .withPrefix(folder + "/");
    final ObjectListing objects = s3Client.listObjects(listObjectsRequest);
    return !objects.getObjectSummaries().isEmpty();
  }

  private class CopyObjectCallable implements Callable<CopyObjectResult> {
    private final AmazonS3 amazonS3Client;
    private final CopyObjectRequest copyObjectRequest;

    private CopyObjectCallable(final AmazonS3 amazonS3Client, final CopyObjectRequest copyObjectRequest) {
      this.amazonS3Client = amazonS3Client;
      this.copyObjectRequest = copyObjectRequest;
    }

    @Override
    public CopyObjectResult call() throws Exception {
      return amazonS3Client.copyObject(copyObjectRequest);
    }
  }

  private class DeleteObjectsCallable implements Callable<DeleteObjectsResult> {
    private final AmazonS3 amazonS3Client;
    private final DeleteObjectsRequest deleteObjectsRequest;

    private DeleteObjectsCallable(final AmazonS3 amazonS3Client, final DeleteObjectsRequest deleteObjectsRequest) {
      this.amazonS3Client = amazonS3Client;
      this.deleteObjectsRequest = deleteObjectsRequest;
    }

    @Override
    public DeleteObjectsResult call() throws Exception {
      return amazonS3Client.deleteObjects(deleteObjectsRequest);
    }
  }
}
