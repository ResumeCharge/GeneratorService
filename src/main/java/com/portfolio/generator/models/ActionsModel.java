package com.portfolio.generator.models;

import java.util.List;


/**
 * Maps the actions defined in the generator-config.json files
 * for each template. New actions needed to be added here so the
 * generator knows how to handle them.
 */
public class ActionsModel {
  private ActionType actionType;
  private List<ProcessorOptionsModel> options;
  private String inputLocation;
  private String outputLocation;
  private String dataKey;
  private String contents;

  public ActionType getActionType() {
    return actionType;
  }

  public void setActionType(ActionType actionType) {
    this.actionType = actionType;
  }

  public List<ProcessorOptionsModel> getOptions() {
    return options;
  }

  public void setOptions(List<ProcessorOptionsModel> options) {
    this.options = options;
  }

  public String getInputLocation() {
    return inputLocation;
  }

  public void setInputLocation(String inputLocation) {
    this.inputLocation = inputLocation;
  }

  public String getOutputLocation() {
    return outputLocation;
  }

  public void setOutputLocation(String outputLocation) {
    this.outputLocation = outputLocation;
  }

  public String getDataKey() {
    return dataKey;
  }

  public void setDataKey(String dataKey) {
    this.dataKey = dataKey;
  }

  public String getContents() {
    return contents;
  }

  public void setContents(String contents) {
    this.contents = contents;
  }
}
