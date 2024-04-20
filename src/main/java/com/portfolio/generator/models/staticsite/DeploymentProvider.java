package com.portfolio.generator.models.staticsite;

import org.apache.commons.lang3.Validate;

public enum DeploymentProvider {
  GITHUB;

  public static DeploymentProvider getDeploymentProviderFromString(final String deploymentProvider) {
    Validate.notBlank(deploymentProvider);
    if (deploymentProvider.equalsIgnoreCase("github")) {
      return DeploymentProvider.GITHUB;
    } else {
      throw new IllegalArgumentException(String.format("Unknown deployment provider %s", deploymentProvider));
    }
  }
}
