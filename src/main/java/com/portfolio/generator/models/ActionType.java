package com.portfolio.generator.models;

/**
 * Defines the type of actions supported by
 * the generator service.
 */
public enum ActionType {
  DELETE_FILE,
  COPY_DIRECTORY,
  CREATE_DIRECTORY,
  PROCESS_TEMPLATE,
  PROCESS_TEMPLATE_FROM_ARRAY,
  DEPLOY_STATIC_SITE,
  COPY_RESUME_FROM_STATIC_ASSETS,
  COPY_PROFILE_PICTURE_FROM_STATIC_ASSETS
}
