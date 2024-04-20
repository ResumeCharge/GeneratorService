package com.portfolio.generator.utilities.helpers;

import java.io.IOException;

public interface IDeploymentStatusHelper {
  void updateDeploymentProgress(DeploymentStatus deploymentStatus) throws IOException;
}
