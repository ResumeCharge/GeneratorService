package com.portfolio.generator.models;

import java.io.File;

public class ActionResultModel {
  private final boolean isSuccessful;
  private final File outputFile;

  private ActionResultModel(Builder actionResultModelBuilder) {
    this.isSuccessful = actionResultModelBuilder.isSuccessful;
    this.outputFile = actionResultModelBuilder.outputFile;
  }

  public boolean getIsSuccessful() {
    return isSuccessful;
  }

  public File getOutputFile() {
    return outputFile;
  }

  public static class Builder {
    private boolean isSuccessful;
    private File outputFile;

    public Builder() {
    }

    public Builder setIsSuccessful(boolean isSuccessful) {
      this.isSuccessful = isSuccessful;
      return this;
    }

    public Builder setOutputFile(File outputFile) {
      this.outputFile = outputFile;
      return this;
    }

    public ActionResultModel build() {
      return new ActionResultModel(this);
    }
  }

}
