package com.portfolio.generator.utilities.exceptions;

public class ActionProcessingFailedException extends Exception {
  public ActionProcessingFailedException(String errorMessage, Throwable err) {
    super(errorMessage, err);
  }

  public ActionProcessingFailedException(String errorMessage) {
    super(errorMessage);
  }
}
