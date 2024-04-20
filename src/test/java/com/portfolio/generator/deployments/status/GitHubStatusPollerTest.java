package com.portfolio.generator.deployments.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.generator.concurrency.helpers.ISleeper;
import com.portfolio.generator.deployments.jobs.PendingDeploymentSweeperAction;
import com.portfolio.generator.utilities.exceptions.PortfolioGenerationFailedException;
import com.portfolio.generator.utilities.factories.IDateTimeFactory;
import com.portfolio.generator.utilities.http.IHttpUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GitHubStatusPollerTest {
  private static final ZoneId TIMEZONE_ID = ZoneId.of("Z");
  @Mock
  IDateTimeFactory dateTimeFactory;
  @Mock
  ISleeper sleeper;
  @Mock
  CloseableHttpClient closeableHttpClient;
  @Mock
  IHttpUtils httpUtils;
  @Mock
  ObjectMapper objectMapper;
  private GitHubStatusPoller gitHubStatusPoller;

  @BeforeEach
  void setUp() {
    gitHubStatusPoller = new GitHubStatusPoller(dateTimeFactory, sleeper, closeableHttpClient, httpUtils, objectMapper);
  }

  @Test
  void pollDeploymentStatusUntilFinishedAndReturnDeployedUrl() throws PortfolioGenerationFailedException, IOException, InterruptedException {
    final CloseableHttpResponse closeableHttpResponse = mock(CloseableHttpResponse.class);
    final HttpEntity httpEntity = mock(HttpEntity.class);
    final PendingDeploymentSweeperAction deploymentSweeperAction = new PendingDeploymentSweeperAction.Builder().setGitHubUserName("github-username").setoAuthToken("oauth-token").setDeploymentId("deployment-id").build();
    final LocalDateTime localDateTime = LocalDateTime.now(TIMEZONE_ID);
    final GitHubStatusResponse gitHubStatusResponse = new GitHubStatusResponse();
    gitHubStatusResponse.setStatus("built");
    gitHubStatusResponse.setHtml_url("portfolio-gen.github.io");
    when(dateTimeFactory.getLocalDateTimeNow(any(ZoneId.class))).thenReturn(localDateTime);
    when(closeableHttpClient.execute(any())).thenReturn(closeableHttpResponse);
    when(closeableHttpResponse.getEntity()).thenReturn(httpEntity);
    when(objectMapper.readValue(anyString(), eq(GitHubStatusResponse.class))).thenReturn(gitHubStatusResponse);
    when(httpUtils.convertEntityToString(any(HttpEntity.class))).thenReturn("response");
    final String url = gitHubStatusPoller.pollDeploymentStatusUntilFinishedAndReturnDeployedUrl(deploymentSweeperAction);
    assertThat(url).isEqualTo("portfolio-gen.github.io");
    verify(sleeper, never()).sleep(anyLong());
  }

  @Test
  void pollDeploymentStatus_maxTimeReached() {
    final PendingDeploymentSweeperAction deploymentSweeperAction = new PendingDeploymentSweeperAction.Builder().setGitHubUserName("github-username").setoAuthToken("oauth-token").setDeploymentId("deployment-id").build();
    final LocalDateTime localDateTime = LocalDateTime.now(TIMEZONE_ID);
    when(dateTimeFactory.getLocalDateTimeNow(any(ZoneId.class))).thenReturn(localDateTime).thenReturn(localDateTime.plus(Duration.ofHours(1)));
    assertThrows(RuntimeException.class, () -> gitHubStatusPoller.pollDeploymentStatusUntilFinishedAndReturnDeployedUrl(deploymentSweeperAction));
  }

  @Test
  void pollDeploymentStatus_gitHubErrorStatus() throws PortfolioGenerationFailedException, IOException, InterruptedException {
    final CloseableHttpResponse closeableHttpResponse = mock(CloseableHttpResponse.class);
    final HttpEntity httpEntity = mock(HttpEntity.class);
    final PendingDeploymentSweeperAction deploymentSweeperAction = new PendingDeploymentSweeperAction.Builder().setGitHubUserName("github-username").setoAuthToken("oauth-token").setDeploymentId("deployment-id").build();
    final LocalDateTime localDateTime = LocalDateTime.now(TIMEZONE_ID);
    final GitHubStatusResponse gitHubStatusResponse = new GitHubStatusResponse();
    gitHubStatusResponse.setStatus("errored");
    gitHubStatusResponse.setHtml_url("portfolio-gen.github.io");
    when(dateTimeFactory.getLocalDateTimeNow(any(ZoneId.class))).thenReturn(localDateTime);
    when(closeableHttpClient.execute(any())).thenReturn(closeableHttpResponse);
    when(closeableHttpResponse.getEntity()).thenReturn(httpEntity);
    when(objectMapper.readValue(anyString(), eq(GitHubStatusResponse.class))).thenReturn(gitHubStatusResponse);
    when(httpUtils.convertEntityToString(any(HttpEntity.class))).thenReturn("response");
    assertThrows(PortfolioGenerationFailedException.class, () -> gitHubStatusPoller.pollDeploymentStatusUntilFinishedAndReturnDeployedUrl(deploymentSweeperAction));
  }

  @Test
  void pollDeploymentStatus_buildingThenBuilt() throws IOException, PortfolioGenerationFailedException, InterruptedException {
    final CloseableHttpResponse closeableHttpResponse = mock(CloseableHttpResponse.class);
    final HttpEntity httpEntity = mock(HttpEntity.class);
    final PendingDeploymentSweeperAction deploymentSweeperAction = new PendingDeploymentSweeperAction.Builder().setGitHubUserName("github-username").setoAuthToken("oauth-token").setDeploymentId("deployment-id").build();
    final LocalDateTime localDateTime = LocalDateTime.now(TIMEZONE_ID);
    final GitHubStatusResponse gitHubStatusResponseBuilt = new GitHubStatusResponse();
    gitHubStatusResponseBuilt.setStatus("built");
    gitHubStatusResponseBuilt.setHtml_url("portfolio-gen.github.io");
    final GitHubStatusResponse gitHubStatusResponseBuilding = new GitHubStatusResponse();
    gitHubStatusResponseBuilding.setStatus("building");
    when(dateTimeFactory.getLocalDateTimeNow(any(ZoneId.class))).thenReturn(localDateTime);
    when(closeableHttpClient.execute(any())).thenReturn(closeableHttpResponse);
    when(closeableHttpResponse.getEntity()).thenReturn(httpEntity);
    when(objectMapper.readValue(anyString(), eq(GitHubStatusResponse.class))).thenReturn(gitHubStatusResponseBuilding).thenReturn(gitHubStatusResponseBuilt);
    when(httpUtils.convertEntityToString(any(HttpEntity.class))).thenReturn("response");
    final String url = gitHubStatusPoller.pollDeploymentStatusUntilFinishedAndReturnDeployedUrl(deploymentSweeperAction);
    assertThat(url).isEqualTo("portfolio-gen.github.io");
    verify(sleeper).sleep(anyLong());
  }
}