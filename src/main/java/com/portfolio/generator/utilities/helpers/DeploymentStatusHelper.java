package com.portfolio.generator.utilities.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.generator.services.staticsites.github.GitHubService;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

@Component
public class DeploymentStatusHelper implements IDeploymentStatusHelper {
  private static final String UPDATE_DEPLOYMENT_STATUS_MSG =
      "Updating deployment status. Request: %s";
  private static final String UPDATE_DEPLOYMENT_STATUS_ERR_MSG =
      "Failed to update deployment status. Request: %s. Error: %s";
  private static final String UPDATE_DEPLOYMENT_STATUS_MISSING_FIELDS_ERR_MSG =
      "Failed to update deployment status. One of progress or status field on DeploymentStatus must be set.";
  private static final String DEPLOYMENT_SERVICE_URL = "http://localhost/api/deployments/%s";
  private static final String CLIENT_ID_HEADER = "client_id";
  private static final String CLIENT_SECRET_HEADER = "client_secret";
  private static final Logger logger = LoggerFactory.getLogger(GitHubService.class);
  private final CloseableHttpClient closeableHttpClient;

  @Value("${CLIENT_SECRET}")
  private String CLIENT_SECRET;

  @Value("${CLIENT_ID}")
  private String CLIENT_ID;

  public DeploymentStatusHelper(
      final CloseableHttpClient closeableHttpClient
  ) {
    this.closeableHttpClient = closeableHttpClient;
  }

  @Override
  public void updateDeploymentProgress(final DeploymentStatus deploymentStatus) throws IOException {
    try {
      final HttpPatch request = createHttpPatchRequest(deploymentStatus);
      logger.info(String.format(UPDATE_DEPLOYMENT_STATUS_MSG, request));
      try (final CloseableHttpResponse response = closeableHttpClient.execute(request)) {
        final int status = response.getStatusLine().getStatusCode();
        if (status != 200) {
          logger.info(
              String.format(UPDATE_DEPLOYMENT_STATUS_ERR_MSG, deploymentStatus.getDeploymentId(),
                  response.getStatusLine().getReasonPhrase()
              ));
        }
      }
    } catch (final Exception e) {
      logger.error("Exception trying to update deployment status", e);
    }
  }

  private HttpPatch createHttpPatchRequest(final DeploymentStatus deploymentStatus)
      throws UnsupportedEncodingException, JsonProcessingException {
    final Map<String, Object> body = getRequestBody(deploymentStatus);
    final HttpPatch request =
        new HttpPatch(String.format(DEPLOYMENT_SERVICE_URL, deploymentStatus.getDeploymentId()));
    // add request headers
    request.addHeader(CLIENT_ID_HEADER, CLIENT_ID);
    request.addHeader(CLIENT_SECRET_HEADER, CLIENT_SECRET);
    request.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString());
    final String bodyAsJson = new ObjectMapper().writeValueAsString(body);
    final StringEntity requestEntity = new StringEntity(bodyAsJson);
    request.setEntity(requestEntity);
    return request;
  }

  private Map<String, Object> getRequestBody(final DeploymentStatus deploymentStatus) {
    final Map<String, Object> body = new HashMap<>();
    final DeploymentStatusType deploymentStatusType = deploymentStatus.getStatus();
    final Long deploymentProgress = deploymentStatus.getProgress();
    if (deploymentStatusType == null && deploymentProgress == null) {
      logger.info(UPDATE_DEPLOYMENT_STATUS_MISSING_FIELDS_ERR_MSG);
    }
    if (deploymentStatusType != null) {
      body.put("status", deploymentStatus.getStatus().getName());
    }
    if (deploymentProgress != null) {
      body.put("progress", deploymentStatus.getProgress());
    }
    return body;
  }


  @PreDestroy
  public void destroy() throws IOException {
    closeableHttpClient.close();
  }
}
