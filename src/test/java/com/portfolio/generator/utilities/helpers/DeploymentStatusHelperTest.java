package com.portfolio.generator.utilities.helpers;

import com.portfolio.generator.managers.GithubPortfolioGenerationTaskManager;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeploymentStatusHelperTest {
  private DeploymentStatusHelper deploymentStatusHelper;
  @Mock
  private CloseableHttpClient closeableHttpClient;

  @BeforeEach
  public void setUp() {
    deploymentStatusHelper =
        new DeploymentStatusHelper(closeableHttpClient);
  }

  @Test
  void updateDeploymentProgress() throws IOException {
    final ArgumentCaptor<HttpPatch> argumentCaptor =
            ArgumentCaptor.forClass(HttpPatch.class);
    final DeploymentStatus deploymentStatus =
        new DeploymentStatus(DeploymentStatusType.PROCESSING, 50L, "123");
    final CloseableHttpResponse closeableHttpResponse = mock(CloseableHttpResponse.class);
    final StatusLine statusLine = mock(StatusLine.class);
    when(closeableHttpClient.execute(argumentCaptor.capture())).thenReturn(closeableHttpResponse);
    when(closeableHttpResponse.getStatusLine()).thenReturn(statusLine);
    when(statusLine.getStatusCode()).thenReturn(200);
    deploymentStatusHelper.setDeploymentServicePort("3001");
    deploymentStatusHelper.setDeploymentServiceHost("localhost");
    deploymentStatusHelper.updateDeploymentProgress(deploymentStatus);
    final HttpPatch request = argumentCaptor.getValue();
    assertThat(request).isNotNull();
    assertThat(request.getURI().toString()).isEqualTo("http://localhost:3001/api/deployments/123");
  }

  @Test
  void updateDeploymentProgressMissingStatus() throws IOException {
    final DeploymentStatus deploymentStatus =
        new DeploymentStatus(DeploymentStatusType.PROCESSING, "123");
    final CloseableHttpResponse closeableHttpResponse = mock(CloseableHttpResponse.class);
    final StatusLine statusLine = mock(StatusLine.class);
    when(closeableHttpClient.execute(any())).thenReturn(closeableHttpResponse);
    when(closeableHttpResponse.getStatusLine()).thenReturn(statusLine);
    when(statusLine.getStatusCode()).thenReturn(200);
    deploymentStatusHelper.updateDeploymentProgress(deploymentStatus);
  }

  @Test
  void updateDeploymentProgressMissingProgress() throws IOException {
    final DeploymentStatus deploymentStatus =
        new DeploymentStatus(50L, "123");
    final CloseableHttpResponse closeableHttpResponse = mock(CloseableHttpResponse.class);
    final StatusLine statusLine = mock(StatusLine.class);
    when(closeableHttpClient.execute(any())).thenReturn(closeableHttpResponse);
    when(closeableHttpResponse.getStatusLine()).thenReturn(statusLine);
    when(statusLine.getStatusCode()).thenReturn(200);
    deploymentStatusHelper.updateDeploymentProgress(deploymentStatus);
  }
}