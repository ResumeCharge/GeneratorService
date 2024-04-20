package com.portfolio.generator.models.databaseModels;

public enum DeploymentStatus {
  PENDING,
  PROCESSING,
  SUCCESSFUL,
  PENDING_RETRY,
  SENT_TO_GITHUB,
  SENT_TO_AWS,
  FAILED,
  CANCELLED
}
