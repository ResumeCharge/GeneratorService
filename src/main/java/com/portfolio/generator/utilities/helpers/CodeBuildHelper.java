package com.portfolio.generator.utilities.helpers;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.codebuild.AWSCodeBuild;
import com.amazonaws.services.codebuild.model.*;
import com.portfolio.generator.deployments.jobs.PendingDeploymentSweeperJob;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CodeBuildHelper implements ICodeBuildHelper {
  private static final Logger logger = LoggerFactory.getLogger(PendingDeploymentSweeperJob.class);

  @Override
  public StatusType getLastBuildStatus(final AWSCodeBuild codeBuildClient, final String projectName) {
    Validate.notBlank(projectName, "projectName cannot be null or blank");
    final Optional<String> lastBuildId = getLastBuildId(codeBuildClient, projectName);
    if (lastBuildId.isEmpty()) {
      logger.warn(String.format("No buildId found for project %s, assuming this is the first run", projectName));
      return StatusType.SUCCEEDED;
    }
    final Optional<Build> lastBuild = getBuild(codeBuildClient, lastBuildId.get());
    if (lastBuild.isEmpty()) {
      final String err =
          String.format("Unable to retrieve last build with id: %s, assuming something is very wrong", lastBuildId.get());
      logger.error(err);
      throw new SdkClientException(err);
    }
    return StatusType.valueOf(lastBuild.get().getBuildStatus());
  }

  @Override
  public Optional<Build> getBuild(final AWSCodeBuild awsCodeBuildClient, final String buildId) {
    final BatchGetBuildsRequest batchGetBuildsRequest = new BatchGetBuildsRequest().withIds(buildId);
    final BatchGetBuildsResult batchGetBuildsResult = awsCodeBuildClient.batchGetBuilds(batchGetBuildsRequest);
    return batchGetBuildsResult.getBuilds().stream().findFirst();
  }

  private Optional<String> getLastBuildId(final AWSCodeBuild codeBuildClient, final String projectName) {
    final ListBuildsForProjectRequest listBuildsForProjectRequest =
        new ListBuildsForProjectRequest().withProjectName(projectName).withSortOrder(SortOrderType.DESCENDING);
    try {
      final ListBuildsForProjectResult listBuildsForProjectResponse =
          codeBuildClient.listBuildsForProject(listBuildsForProjectRequest);
      return listBuildsForProjectResponse.getIds().stream().findFirst();
    } catch (final Exception e) {
      logger.error("Caught exception trying to get last build status for project: " + projectName, e);
    }
    return Optional.empty();
  }
}
