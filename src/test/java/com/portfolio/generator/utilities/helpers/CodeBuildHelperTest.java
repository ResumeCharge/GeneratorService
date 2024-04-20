package com.portfolio.generator.utilities.helpers;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.codebuild.AWSCodeBuildClient;
import com.amazonaws.services.codebuild.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class CodeBuildHelperTest {
  @Mock
  private AWSCodeBuildClient awsCodeBuildClient;

  private ICodeBuildHelper codeBuildHelper;

  @BeforeEach
  void setUp() {
    codeBuildHelper = new CodeBuildHelper();
  }

  @Test
  void getLastBuildStatus() {
    final BatchGetBuildsResult batchGetBuildsResult = getBuildsResult(
        new Build().withBuildStatus(StatusType.SUCCEEDED).withId("1"), new Build().withId("2").withBuildStatus(StatusType.FAILED)
    );
    when(awsCodeBuildClient.batchGetBuilds(any(BatchGetBuildsRequest.class))).thenReturn(batchGetBuildsResult);
    when(awsCodeBuildClient.listBuildsForProject(any(ListBuildsForProjectRequest.class))).thenReturn(getBuildsForProject("1", "2"));
    final StatusType buildStatus = codeBuildHelper.getLastBuildStatus(awsCodeBuildClient, "project");
    assertThat(buildStatus).isEqualTo(StatusType.SUCCEEDED);
  }

  @Test
  void getLastBuildStatus_NoBuilds() {
    when(awsCodeBuildClient.listBuildsForProject(any(ListBuildsForProjectRequest.class))).thenReturn(new ListBuildsForProjectResult());
    final StatusType buildStatus = codeBuildHelper.getLastBuildStatus(awsCodeBuildClient, "project");
    assertThat(buildStatus).isEqualTo(StatusType.SUCCEEDED);
  }

  @Test
  void getLastBuildStatus_BuildIdNotFound() {
    when(awsCodeBuildClient.batchGetBuilds(any(BatchGetBuildsRequest.class))).thenReturn(new BatchGetBuildsResult().withBuilds(Collections.emptyList()));
    when(awsCodeBuildClient.listBuildsForProject(any(ListBuildsForProjectRequest.class))).thenReturn(getBuildsForProject("1"));
    assertThrows(SdkClientException.class, () -> codeBuildHelper.getLastBuildStatus(awsCodeBuildClient, "project"));
  }

  @Test
  void getLastBuildStatus_ProjectNameBlank() {
    assertThrows(IllegalArgumentException.class, () -> codeBuildHelper.getLastBuildStatus(awsCodeBuildClient, ""));
  }

  private BatchGetBuildsResult getBuildsResult(final Build... builds) {
    return new BatchGetBuildsResult().withBuilds(builds);
  }

  private ListBuildsForProjectResult getBuildsForProject(final String... builds) {
    return new ListBuildsForProjectResult().withIds(builds);
  }
}