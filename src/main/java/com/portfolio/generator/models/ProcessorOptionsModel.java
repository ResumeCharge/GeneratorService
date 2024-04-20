package com.portfolio.generator.models;

public class ProcessorOptionsModel {
  private OptionType optionType;
  private String optionValue;
  private String optionKey;

  public OptionType getOptionType() {
    return optionType;
  }

  public void setOptionType(OptionType optionType) {
    this.optionType = optionType;
  }

  public String getOptionValue() {
    return optionValue;
  }

  public void setOptionValue(String optionValue) {
    this.optionValue = optionValue;
  }

  public String getOptionKey() {
    return optionKey;
  }

  public void setOptionKey(String optionKey) {
    this.optionKey = optionKey;
  }
}
