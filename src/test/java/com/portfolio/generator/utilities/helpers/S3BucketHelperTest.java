package com.portfolio.generator.utilities.helpers;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.google.common.util.concurrent.MoreExecutors;
import com.portfolio.generator.concurrency.factory.IExecutorServiceFactory;
import com.portfolio.generator.utilities.IO.IIOFactory;
import com.portfolio.generator.utilities.helpers.s3.CopyFolderRequest;
import com.portfolio.generator.utilities.helpers.s3.IS3BucketHelper;
import com.portfolio.generator.utilities.helpers.s3.S3BucketHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class S3BucketHelperTest {
  @Mock
  private IExecutorServiceFactory executorServiceFactory;
  @Mock
  private IIOFactory ioFactory;
  private IS3BucketHelper s3BucketHelper;
  private AmazonS3 s3Client;
  private S3Object s3Object;

  @BeforeEach
  public void setUp() {
    s3Client = mock(AmazonS3.class);
    s3Object = mock(S3Object.class);
    s3BucketHelper = new S3BucketHelper(ioFactory, executorServiceFactory);
  }

  @Test
  public void testDownloadResumeFromS3Bucket() throws IOException {
    final S3ObjectInputStream s3ObjectInputStream = mock(S3ObjectInputStream.class);
    final FileOutputStream fileOutputStream = mock(FileOutputStream.class);
    final Path p = mock(Path.class);
    when(p.toFile()).thenReturn(mock(File.class));
    when(ioFactory.createFileOutputStreamUsingFileUtils(any(File.class))).thenReturn(fileOutputStream);
    when(s3Object.getObjectContent()).thenReturn(s3ObjectInputStream);
    when(s3Client.getObject(anyString(), anyString())).thenReturn(s3Object);
    s3BucketHelper.downloadFromS3Bucket(s3Client, "", p);
  }

  @Test
  public void testDownloadResumeFromS3Bucket_Exception() {
    final Path p = mock(Path.class);
    when(s3Client.getObject(anyString(), anyString())).thenThrow(new SdkClientException("failure"));
    s3BucketHelper.downloadFromS3Bucket(s3Client, "", p);
  }

  @Test
  public void testCopyFolder() throws InterruptedException {
    final ArgumentCaptor<CopyObjectRequest> argumentCaptor = ArgumentCaptor.forClass(CopyObjectRequest.class);
    final CopyFolderRequest copyFolderRequest = new CopyFolderRequest.Builder()
        .withFromFolder("folder-1")
        .withToFolder("folder-2")
        .withBucketName("bucket")
        .withS3Client(s3Client)
        .build();
    final ObjectListing objectListing = mock(ObjectListing.class);
    when(s3Client.listObjects(any(ListObjectsRequest.class))).thenReturn(objectListing);
    when(objectListing.getObjectSummaries()).thenReturn(
        List.of(
            getS3ObjectSummary("folder-1/key-1"),
            getS3ObjectSummary("folder-1/key-2")
        )
    );
    when(executorServiceFactory.getFixedSizedThreadPool(anyInt()))
        .thenReturn(MoreExecutors.newDirectExecutorService());
    when(s3Client.copyObject(argumentCaptor.capture())).thenReturn(new CopyObjectResult());
    s3BucketHelper.copyFolder(copyFolderRequest);
    final List<CopyObjectRequest> copyObjectRequests = argumentCaptor.getAllValues();
    assertThat(copyObjectRequests.size()).isEqualTo(2);
    assertThat(copyObjectRequests.get(0).getSourceKey()).isEqualTo("folder-1/key-1");
    assertThat(copyObjectRequests.get(0).getSourceBucketName()).isEqualTo("bucket");
    assertThat(copyObjectRequests.get(0).getDestinationKey()).isEqualTo("folder-2/key-1");
    assertThat(copyObjectRequests.get(0).getDestinationBucketName()).isEqualTo("bucket");

    assertThat(copyObjectRequests.get(1).getSourceKey()).isEqualTo("folder-1/key-2");
    assertThat(copyObjectRequests.get(1).getSourceBucketName()).isEqualTo("bucket");
    assertThat(copyObjectRequests.get(1).getDestinationKey()).isEqualTo("folder-2/key-2");
    assertThat(copyObjectRequests.get(1).getDestinationBucketName()).isEqualTo("bucket");
  }

  @Test
  public void testDeleteFolder() {
    final ObjectListing objectListing = mock(ObjectListing.class);
    when(s3Client.listObjects(any(ListObjectsRequest.class))).thenReturn(objectListing);
    when(objectListing.getObjectSummaries()).thenReturn(
        List.of(
            getS3ObjectSummary("folder-1/key-1"),
            getS3ObjectSummary("folder-1/key-2")
        )
    );
    final boolean doesFolderExist = s3BucketHelper.doesFolderExist(s3Client, "folder-1", "bucket");
    assertThat(doesFolderExist).isTrue();
  }

  @Test
  public void testDeleteFolder_DoesNotExist() {
    final ObjectListing objectListing = mock(ObjectListing.class);
    when(s3Client.listObjects(any(ListObjectsRequest.class))).thenReturn(objectListing);
    when(objectListing.getObjectSummaries()).thenReturn(Collections.emptyList());
    final boolean doesFolderExist = s3BucketHelper.doesFolderExist(s3Client, "folder-1", "bucket");
    assertThat(doesFolderExist).isFalse();
  }

  @Test
  public void testDoesFolderExist() throws InterruptedException {
    final ArgumentCaptor<DeleteObjectsRequest> argumentCaptor = ArgumentCaptor.forClass(DeleteObjectsRequest.class);
    final ObjectListing objectListing = mock(ObjectListing.class);
    when(s3Client.listObjects(any(ListObjectsRequest.class))).thenReturn(objectListing);
    when(objectListing.getObjectSummaries()).thenReturn(
        List.of(
            getS3ObjectSummary("folder-1/key-1"),
            getS3ObjectSummary("folder-1/key-2")
        )
    );
    when(executorServiceFactory.getFixedSizedThreadPool(anyInt()))
        .thenReturn(MoreExecutors.newDirectExecutorService());
    s3BucketHelper.deleteFolder(s3Client, "folder-1", "bucket");
    verify(s3Client).deleteObjects(argumentCaptor.capture());
    final DeleteObjectsRequest deleteObjectsRequest = argumentCaptor.getValue();

    assertThat(deleteObjectsRequest.getBucketName()).isEqualTo("bucket");
    assertThat(deleteObjectsRequest.getKeys()).hasSize(2);
  }

  private S3ObjectSummary getS3ObjectSummary(final String key) {
    final S3ObjectSummary objectSummary = new S3ObjectSummary();
    objectSummary.setKey(key);
    return objectSummary;
  }
}