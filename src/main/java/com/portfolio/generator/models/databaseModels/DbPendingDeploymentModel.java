package com.portfolio.generator.models.databaseModels;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.portfolio.generator.models.transformers.deserializers.ObjectIdDeserializer;

public class DbPendingDeploymentModel {
  @JsonDeserialize(using = ObjectIdDeserializer.class)
  @JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
  private String _id;

  public String get_id() {
    return _id;
  }

  public void set_id(final String _id) {
    this._id = _id;
  }
}
