package com.portfolio.generator.services.database;

import com.mongodb.MongoException;
import com.portfolio.generator.models.databaseModels.DbDeploymentModel;
import com.portfolio.generator.models.databaseModels.DbPendingDeploymentModel;
import com.portfolio.generator.models.databaseModels.DbResumeModel;

import java.util.List;

public interface IDatabaseService {
  DbResumeModel getResume(final String resumeId) throws MongoException;

  DbDeploymentModel getDeployment(final String deploymentId) throws MongoException;

  List<DbDeploymentModel> getDeployments() throws MongoException;

  List<DbDeploymentModel> getDeployments(final List<String> deploymentIds) throws MongoException;

  void deletePendingDeployment(final String pendingDeploymentId) throws MongoException;

  List<DbPendingDeploymentModel> getPendingDeployments() throws MongoException;

  void updateDeploymentRetryCount(final String deploymentId, final int retryCount) throws MongoException;

  void updateDeploymentUrl(final String deploymentId, final String deployedUrl) throws MongoException;

  void updateCloudFrontInvalidationId(final String deploymentId, final String cloudFrontInvalidationId) throws MongoException;
}
