package com.portfolio.generator.utilities.helpers;

public enum DeploymentStatusType {
  PENDING("PENDING"),
  PROCESSING("PROCESSING"),
  SUCCESSFUL("SUCCESSFUL"),
  RETRYING("RETRYING"),
  FAILED("FAILED"),
  CANCELLATION_REQUESTED("CANCELLATION_REQUESTED"),
  CANCELLED("CANCELLED"),
  PENDING_RETRY("PENDING_RETRY"),
  SENT_TO_GITHUB("SENT_TO_GITHUB"),
  SENT_TO_AWS("SENT_TO_AWS");

  final String name;

  DeploymentStatusType(final String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
