package com.portfolio.generator.services.database;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class MongoDbConnectionManager implements IMongoDbConnectionManager {
  private final Logger logger = LoggerFactory.getLogger(MongoDbConnectionManager.class);
  @Value("${MONGO_DB_URI}")
  private String MONGO_URI;
  private MongoClient mongoClient;


  public MongoClient getMongoClient() {
    Validate.notBlank(MONGO_URI);
    logger.info(String.format("Building connection to mongoDB: %s", MONGO_URI));
    if (mongoClient != null) {
      return mongoClient;
    }
    final MongoClientSettings settings = MongoClientSettings.builder()
        .applyConnectionString(new ConnectionString(MONGO_URI))
        .build();
    mongoClient = MongoClients.create(settings);
    return mongoClient;
  }
}
