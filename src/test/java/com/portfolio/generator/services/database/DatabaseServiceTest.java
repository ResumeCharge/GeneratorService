package com.portfolio.generator.services.database;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoException;
import com.mongodb.client.*;
import com.mongodb.client.model.UpdateOptions;
import com.portfolio.generator.models.databaseModels.DbDeploymentModel;
import com.portfolio.generator.models.databaseModels.DbResumeModel;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatabaseServiceTest {
  private final String OBJECT_ID = "649e12b7197a46d4c80cbc69";
  private IDatabaseService databaseService;
  @Mock
  private IMongoDbConnectionManager mongoDbConnectionManager;
  @Mock
  private ObjectMapper objectMapper;
  @Mock
  private MongoDatabase mongoDatabase;
  @Mock
  private MongoClient mongoClient;
  @Mock
  private MongoCollection mongoCollection;

  @BeforeEach
  void setUp() {
    when(mongoDbConnectionManager.getMongoClient()).thenReturn(mongoClient);
    when(mongoClient.getDatabase(anyString())).thenReturn(mongoDatabase);
    databaseService = new DatabaseService(mongoDbConnectionManager, objectMapper);
  }

  @Test
  void getResume() throws JsonProcessingException {
    final FindIterable findIterable = mock(FindIterable.class);
    final Document document = mock(Document.class);
    final DbResumeModel dbResumeModel = new DbResumeModel();
    when(mongoDatabase.getCollection(anyString())).thenReturn(mongoCollection);
    when(mongoCollection.find(any(Bson.class))).thenReturn(findIterable);
    when(document.toJson()).thenReturn("");
    when(findIterable.first()).thenReturn(document);
    when(objectMapper.readValue(anyString(), eq(DbResumeModel.class))).thenReturn(dbResumeModel);
    final DbResumeModel resumeModel = databaseService.getResume(OBJECT_ID);
    assertThat(resumeModel).isNotNull();

  }

  @Test
  void getResume_ResumeNotFound() {
    assertThrows(MongoException.class, () -> {
      final FindIterable findIterable = mock(FindIterable.class);
      when(mongoDatabase.getCollection(anyString())).thenReturn(mongoCollection);
      when(mongoCollection.find(any(Bson.class))).thenReturn(findIterable);
      when(findIterable.first()).thenReturn(null);
      databaseService.getResume(OBJECT_ID);
    });
  }

  @Test
  void getResume_JacksonException() throws JsonProcessingException {
    final FindIterable findIterable = mock(FindIterable.class);
    final Document document = mock(Document.class);
    when(mongoDatabase.getCollection(anyString())).thenReturn(mongoCollection);
    when(mongoCollection.find(any(Bson.class))).thenReturn(findIterable);
    when(document.toJson()).thenReturn("");
    when(findIterable.first()).thenReturn(document);
    when(objectMapper.readValue(anyString(), eq(DbResumeModel.class))).thenThrow(new JsonMappingException(""));
    final DbResumeModel resumeModel = databaseService.getResume(OBJECT_ID);
    assertThat(resumeModel).isNull();
  }

  @Test
  void getDeployments() throws JsonProcessingException {
    final FindIterable findIterable = mock(FindIterable.class);
    final Document document = mock(Document.class);
    final DbDeploymentModel dbDeploymentModel = new DbDeploymentModel();
    final MongoCursor mongoCursor = mock(MongoCursor.class);
    when(mongoDatabase.getCollection(anyString())).thenReturn(mongoCollection);
    when(mongoCollection.find()).thenReturn(findIterable);
    when(document.toJson()).thenReturn("");
    when(findIterable.iterator()).thenReturn(mongoCursor);
    when(mongoCursor.hasNext())
        .thenReturn(true)
        .thenReturn(true)
        .thenReturn(false);
    when(mongoCursor.next())
        .thenReturn(document)
        .thenReturn(document)
        .thenReturn(null);
    when(objectMapper.readValue(anyString(), eq(DbDeploymentModel.class))).thenReturn(dbDeploymentModel);
    final List<DbDeploymentModel> deploymentModels = databaseService.getDeployments();
    assertThat(deploymentModels.size()).isEqualTo(2);
  }

  @Test
  void updateDeploymentUrl() {
    when(mongoDatabase.getCollection(anyString())).thenReturn(mongoCollection);
    databaseService.updateDeploymentUrl(OBJECT_ID, "deployed-url");
    verify(mongoCollection).updateOne(any(Bson.class), any(Bson.class), any(UpdateOptions.class));
  }

  @Test
  void updateCloudFrontInvalidationId() {
    when(mongoDatabase.getCollection(anyString())).thenReturn(mongoCollection);
    databaseService.updateCloudFrontInvalidationId(OBJECT_ID, "invalidation-id");
    verify(mongoCollection).updateOne(any(Bson.class), any(Bson.class), any(UpdateOptions.class));
  }

  @Test
  void getDeployment_JacksonException() throws JsonProcessingException {
    final FindIterable findIterable = mock(FindIterable.class);
    final Document document = mock(Document.class);
    final MongoCursor mongoCursor = mock(MongoCursor.class);
    when(mongoDatabase.getCollection(anyString())).thenReturn(mongoCollection);
    when(mongoCollection.find()).thenReturn(findIterable);
    when(document.toJson()).thenReturn("");
    when(findIterable.iterator()).thenReturn(mongoCursor);
    when(mongoCursor.hasNext())
        .thenReturn(true)
        .thenReturn(false);
    when(mongoCursor.next())
        .thenReturn(document);
    when(objectMapper.readValue(anyString(), eq(DbDeploymentModel.class))).thenThrow(new JsonMappingException(""));
    final List<DbDeploymentModel> deploymentModels = databaseService.getDeployments();
    assertThat(deploymentModels.isEmpty()).isTrue();
  }
}