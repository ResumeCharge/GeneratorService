package com.portfolio.generator.utilities.helpers;

public class DeploymentStatus {
  private final String deploymentId;
  private DeploymentStatusType status;
  private long progress;

  public DeploymentStatus(DeploymentStatusType status, long progress, String deploymentId) {
    this.status = status;
    this.progress = progress;
    this.deploymentId = deploymentId;
  }

  public DeploymentStatus(long progress, String deploymentId) {
    this.progress = progress;
    this.deploymentId = deploymentId;
  }

  public DeploymentStatus(DeploymentStatusType status, String deploymentId) {
    this.status = status;
    this.deploymentId = deploymentId;
  }

  public DeploymentStatusType getStatus() {
    return status;
  }

  public long getProgress() {
    return progress;
  }

  public String getDeploymentId() {
    return deploymentId;
  }
}
