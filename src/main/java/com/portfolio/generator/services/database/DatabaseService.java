package com.portfolio.generator.services.database;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.portfolio.generator.models.databaseModels.DbDeploymentModel;
import com.portfolio.generator.models.databaseModels.DbPendingDeploymentModel;
import com.portfolio.generator.models.databaseModels.DbResumeModel;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.eq;

@Component
public class DatabaseService implements IDatabaseService {
  private static final Logger logger = LoggerFactory.getLogger(DatabaseService.class);
  private static final String DATABASE = "development";
  private static final String DEPLOYMENTS_COLLECTION = "deployments";
  private static final String RESUMES_COLLECTION = "resumes";
  private static final String PENDING_DEPLOYMENT_COLLECTIONS = "pendingdeployments";
  private final MongoDatabase mongoDatabase;
  private final ObjectMapper objectMapper;

  public DatabaseService(
      final IMongoDbConnectionManager mongoDbConnectionManager,
      final ObjectMapper objectMapper
  ) {
    this.objectMapper = objectMapper;
    final MongoClient mongoClient = mongoDbConnectionManager.getMongoClient();
    mongoDatabase = mongoClient.getDatabase(DATABASE);
  }

  @Override
  public DbResumeModel getResume(final String resumeId) {
    final MongoCollection<Document> collection =
        mongoDatabase.getCollection(RESUMES_COLLECTION);
    final FindIterable<Document> resumeIterable = collection.find(eq("_id", new ObjectId(resumeId)));
    final Document resumeDocument = resumeIterable.first();
    if (resumeDocument == null) {
      throw new MongoException("No resume found with id: " + resumeId);
    }
    return getModelFromJson(resumeDocument.toJson(), DbResumeModel.class);
  }

  @Override
  public List<DbDeploymentModel> getDeployments() {
    return getFromDb(DEPLOYMENTS_COLLECTION, DbDeploymentModel.class);
  }

  @Override
  public List<DbDeploymentModel> getDeployments(final List<String> deploymentIds) {
    final List<ObjectId> objectIds = deploymentIds.stream().map(ObjectId::new).collect(Collectors.toList());
    return getFromDb(DEPLOYMENTS_COLLECTION, DbDeploymentModel.class, objectIds);
  }

  @Override
  public void deletePendingDeployment(final String pendingDeploymentId) throws MongoException {
    final MongoCollection<Document> collection =
        mongoDatabase.getCollection(PENDING_DEPLOYMENT_COLLECTIONS);
    final ObjectId objectId = new ObjectId(pendingDeploymentId);
    collection.deleteOne(eq("_id", objectId));
  }

  @Override
  public List<DbPendingDeploymentModel> getPendingDeployments() throws MongoException {
    return getFromDb(PENDING_DEPLOYMENT_COLLECTIONS, DbPendingDeploymentModel.class);
  }

  @Override
  public void updateDeploymentRetryCount(final String deploymentId, final int retryCount) {
    final MongoCollection<Document> collection =
        mongoDatabase.getCollection(DEPLOYMENTS_COLLECTION);
    final ObjectId objectId = new ObjectId(deploymentId);
    final Bson updates = Updates.combine(
        Updates.set("retryCount", retryCount));
    final UpdateOptions options = new UpdateOptions().upsert(false);
    collection.updateOne(eq("_id", objectId), updates, options);
  }

  private <T> List<T> getFromDb(final String collectionName,
                                final Class<T> clazz) {
    final MongoCollection<Document> collection =
        mongoDatabase.getCollection(collectionName);
    final List<T> documents = new ArrayList<>();
    final FindIterable<Document> allDocuments = collection.find();
    for (final Document document : allDocuments) {
      final T documentModel =
          getModelFromJson(document.toJson(), clazz);
      if (documentModel != null) {
        documents.add(documentModel);
      }
    }
    return documents;
  }

  @Override
  public DbDeploymentModel getDeployment(final String deploymentId) {
    final MongoCollection<Document> collection =
        mongoDatabase.getCollection(DEPLOYMENTS_COLLECTION);
    final FindIterable<Document> deploymentIterable = collection.find(eq("_id", new ObjectId(deploymentId)));
    final Document deploymentDocument = deploymentIterable.first();
    if (deploymentDocument == null) {
      throw new MongoException("No deployment found with id: " + deploymentId);
    }
    return getModelFromJson(deploymentDocument.toJson(), DbDeploymentModel.class);
  }


  @Override
  public void updateDeploymentUrl(final String deploymentId, final String deployedUrl) {
    final MongoCollection<Document> collection =
        mongoDatabase.getCollection(DEPLOYMENTS_COLLECTION);
    final ObjectId objectId = new ObjectId(deploymentId);
    final Bson updates = Updates.combine(
        Updates.set("deployedUrl", deployedUrl));
    final UpdateOptions options = new UpdateOptions().upsert(false);
    collection.updateOne(eq("_id", objectId), updates, options);
  }

  @Override
  public void updateCloudFrontInvalidationId(final String deploymentId, final String cloudFrontInvalidationId) {
    final MongoCollection<Document> collection =
        mongoDatabase.getCollection(DEPLOYMENTS_COLLECTION);
    final ObjectId objectId = new ObjectId(deploymentId);
    final Bson updates = Updates.combine(
        Updates.set("cloudFrontInvalidationId", cloudFrontInvalidationId));
    final UpdateOptions options = new UpdateOptions().upsert(false);
    collection.updateOne(eq("_id", objectId), updates, options);
  }

  private <T> List<T> getFromDb(final String collectionName,
                                final Class<T> clazz,
                                final List<ObjectId> filters) {
    final MongoCollection<Document> collection =
        mongoDatabase.getCollection(collectionName);
    final List<T> deployments = new ArrayList<>();
    final FindIterable<Document> allDeployments = collection.find(Filters.in("_id", filters));
    for (final Document deploymentDocument : allDeployments) {
      final T deploymentModel =
          getModelFromJson(deploymentDocument.toJson(), clazz);
      if (deploymentModel != null) {
        deployments.add(deploymentModel);
      }
    }
    return deployments;
  }

  private <T> T getModelFromJson(final String json,
                                 final Class<T> clazz) {
    try {
      return objectMapper.readValue(json, clazz);
    } catch (final JacksonException e) {
      logger.error(String.format("Error converting JSON to %s", clazz.getName()), e);
    }
    return null;
  }

}
