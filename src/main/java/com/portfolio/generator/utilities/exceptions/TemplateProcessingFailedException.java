package com.portfolio.generator.utilities.exceptions;


public class TemplateProcessingFailedException extends Exception {
  public TemplateProcessingFailedException(String errorMessage, Throwable err) {
    super(errorMessage, err);
  }
}


