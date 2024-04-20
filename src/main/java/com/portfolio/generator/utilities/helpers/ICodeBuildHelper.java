package com.portfolio.generator.utilities.helpers;

import com.amazonaws.services.codebuild.AWSCodeBuild;
import com.amazonaws.services.codebuild.model.Build;
import com.amazonaws.services.codebuild.model.StatusType;

import java.util.Optional;

public interface ICodeBuildHelper {
  /**
   * Fetches the status of the last build submitted to the specified codebuild project.
   **/
  StatusType getLastBuildStatus(final AWSCodeBuild codeBuildClient, final String projectName);

  Optional<Build> getBuild(final AWSCodeBuild awsCodeBuildClient, final String buildId);
}
