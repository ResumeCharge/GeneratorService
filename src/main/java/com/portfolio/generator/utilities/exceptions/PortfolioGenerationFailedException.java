package com.portfolio.generator.utilities.exceptions;

public class PortfolioGenerationFailedException extends Exception {
  public PortfolioGenerationFailedException(final String errorMessage) {
    super(errorMessage);
  }

  public PortfolioGenerationFailedException(final String errorMessage, final Throwable err) {
    super(errorMessage, err);
  }
}
