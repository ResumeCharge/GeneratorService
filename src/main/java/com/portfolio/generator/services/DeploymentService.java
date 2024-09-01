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
  @Value("${DEPLOYMENT_SERVICE_PORT:3001}")
  private String deploymentServicePort;
  @Value("${DEPLOYMENT_SERVICE_HOST:localhost}")
  private String deploymentServiceHost;
  private final CloseableHttpClient client;

  public DeploymentService(final CloseableHttpClient client) {
    this.client = client;
  }

  @Override
  public Boolean healthCheckDeploymentService() {
    final HttpGet request = new HttpGet(String.format("http://%s:%s/api", deploymentServiceHost, deploymentServicePort));
    request.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString());
    try (final CloseableHttpResponse response = client.execute(request)) {
      final int status = response.getStatusLine().getStatusCode();
      return status == 200;
    } catch (Exception e) {
      return false;
    }
  }

  public String getDeploymentServicePort() {
    return deploymentServicePort;
  }

  public void setDeploymentServicePort(String deploymentServicePort) {
    this.deploymentServicePort = deploymentServicePort;
  }

  public String getDeploymentServiceHost() {
    return deploymentServiceHost;
  }

  public void setDeploymentServiceHost(String deploymentServiceHost) {
    this.deploymentServiceHost = deploymentServiceHost;
  }
}
