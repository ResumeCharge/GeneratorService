package com.portfolio.generator.services;

import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeploymentServiceTest {

  @Test
  void healthCheckDeploymentService() throws IOException {
    final ArgumentCaptor<HttpGet> argumentCaptor =
            ArgumentCaptor.forClass(HttpGet.class);
    final CloseableHttpClient closeableHttpClient = mock(CloseableHttpClient.class);
    final CloseableHttpResponse closeableHttpResponse = mock(CloseableHttpResponse.class);
    final StatusLine statusLine = mock(StatusLine.class);
    when(closeableHttpClient.execute(argumentCaptor.capture())).thenReturn(closeableHttpResponse);
    when(closeableHttpResponse.getStatusLine()).thenReturn(statusLine);
    when(statusLine.getStatusCode()).thenReturn(200);
    final DeploymentService deploymentService = new DeploymentService(closeableHttpClient);
    deploymentService.setDeploymentServicePort("3001");
    deploymentService.setDeploymentServiceHost("localhost");
    final boolean result = deploymentService.healthCheckDeploymentService();
    assertTrue(result);
    final HttpGet request = argumentCaptor.getValue();
    assertThat(request).isNotNull();
    assertThat(request.getURI().toString()).isEqualTo("http://localhost:3001/api");
  }

  @Test
  void healthCheckDeploymentServiceExpectFailure() throws IOException {
    final CloseableHttpClient closeableHttpClient = mock(CloseableHttpClient.class);
    final CloseableHttpResponse closeableHttpResponse = mock(CloseableHttpResponse.class);
    final StatusLine statusLine = mock(StatusLine.class);
    when(closeableHttpClient.execute(any())).thenReturn(closeableHttpResponse);
    when(closeableHttpResponse.getStatusLine()).thenReturn(statusLine);
    when(statusLine.getStatusCode()).thenReturn(500);
    final DeploymentService deploymentService = new DeploymentService(closeableHttpClient);
    deploymentService.setDeploymentServicePort("3000");
    final boolean result = deploymentService.healthCheckDeploymentService();
    assertFalse(result);
  }
}