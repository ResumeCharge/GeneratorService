package com.portfolio.generator.services;

import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class DeploymentService implements IDeploymentService {
  @Value("${DEPLOYMENT_SERVICE_PORT}")
  private String DEPLOYMENT_SERVICE_PORT;
  private final CloseableHttpClient client;

  public DeploymentService(final CloseableHttpClient client) {
    this.client = client;
  }

  @Override
  public Boolean healthCheckDeploymentService() {
    final HttpGet request = new HttpGet(String.format("http://localhost:%s/api", DEPLOYMENT_SERVICE_PORT));
    request.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString());
    try (final CloseableHttpResponse response = client.execute(request)) {
      final int status = response.getStatusLine().getStatusCode();
      return status == 200;
    } catch (Exception e) {
      return false;
    }
  }
}
