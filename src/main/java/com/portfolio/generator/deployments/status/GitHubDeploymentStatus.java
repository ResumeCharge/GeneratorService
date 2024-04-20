package com.portfolio.generator.deployments.status;

import org.apache.commons.lang3.Validate;

public enum GitHubDeploymentStatus {
  DEPLOYING("building"),
  DEPLOYED("built"),
  ERROR("errored");
  private final String deploymentStatus;

  GitHubDeploymentStatus(final String deploymentStatus) {
    this.deploymentStatus = deploymentStatus;
  }

  public static GitHubDeploymentStatus getGitHubDeploymentStatusFromString(final String deploymentStatus) {
    Validate.notBlank(deploymentStatus);
    if (deploymentStatus.equalsIgnoreCase(DEPLOYING.deploymentStatus)) {
      return DEPLOYING;
    } else if (deploymentStatus.equalsIgnoreCase(DEPLOYED.deploymentStatus)) {
      return DEPLOYED;
    } else if (deploymentStatus.equalsIgnoreCase(ERROR.deploymentStatus)) {
      return ERROR;
    } else {
      throw new IllegalArgumentException(String.format("DeploymentStatus %s does not match any known GitHub deployment status", deploymentStatus));
    }
  }
}
