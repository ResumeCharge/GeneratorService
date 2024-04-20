package com.portfolio.generator.services.database;

import com.mongodb.client.MongoClient;

public interface IMongoDbConnectionManager {
  MongoClient getMongoClient();
}
