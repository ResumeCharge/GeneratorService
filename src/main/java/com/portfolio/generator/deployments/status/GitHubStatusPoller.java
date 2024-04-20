package com.portfolio.generator.deployments.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.generator.concurrency.helpers.ISleeper;
import com.portfolio.generator.deployments.jobs.PendingDeploymentSweeperAction;
import com.portfolio.generator.utilities.exceptions.PortfolioGenerationFailedException;
import com.portfolio.generator.utilities.factories.IDateTimeFactory;
import com.portfolio.generator.utilities.http.IHttpUtils;
import org.apache.commons.lang3.Validate;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class GitHubStatusPoller {
  private static final String GITHUB_REPOS_ENDPOINT = "https://api.github.com/repos";
  private static final ZoneId TIMEZONE_ID = ZoneId.of("Z");
  private final IDateTimeFactory dateTimeFactory;
  private final ISleeper sleeper;
  private final CloseableHttpClient client;
  private final IHttpUtils httpUtils;
  private final ObjectMapper objectMapper;

  public GitHubStatusPoller(final IDateTimeFactory dateTimeFactory,
                            final ISleeper sleeper,
                            final CloseableHttpClient client,
                            final IHttpUtils httpUtils,
                            final ObjectMapper objectMapper) {
    this.dateTimeFactory = dateTimeFactory;
    this.sleeper = sleeper;
    this.client = client;
    this.httpUtils = httpUtils;
    this.objectMapper = objectMapper;
  }

  public String pollDeploymentStatusUntilFinishedAndReturnDeployedUrl(final PendingDeploymentSweeperAction deploymentSweeperAction)
      throws IOException, InterruptedException, PortfolioGenerationFailedException {
    final LocalDateTime startTime = dateTimeFactory.getLocalDateTimeNow(TIMEZONE_ID);
    while (true) {
      final LocalDateTime now = dateTimeFactory.getLocalDateTimeNow(TIMEZONE_ID);
      if (startTime.plus(Duration.ofMinutes(15)).isBefore(now)) {
        throw new RuntimeException(String.format("GitHub status checks running too long for deploymentId %s",
            deploymentSweeperAction.deploymentId));
      }
      Validate.notBlank(deploymentSweeperAction.gitHubUserName);
      Validate.notBlank(deploymentSweeperAction.oAuthToken);
      final String repoName = String.format("%s.github.io", deploymentSweeperAction.gitHubUserName);
      final String endpoint =
          String.format(GITHUB_REPOS_ENDPOINT + "/%s/%s/pages", deploymentSweeperAction.gitHubUserName, repoName);
      final HttpGet request = new HttpGet(endpoint);
      request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + deploymentSweeperAction.oAuthToken);
      try (final CloseableHttpResponse response = client.execute(request)) {
        final HttpEntity entity = response.getEntity();
        final String responseBody = httpUtils.convertEntityToString(entity);
        final GitHubStatusResponse gitHubStatusResponse = objectMapper.readValue(responseBody, GitHubStatusResponse.class);
        Validate.notNull(gitHubStatusResponse);
        final String status = gitHubStatusResponse.getStatus();
        //Status will likely be null during the veryf first build
        if(status == null){
          sleeper.sleep(Duration.ofSeconds(60).toMillis());
          continue;
        }
        final GitHubDeploymentStatus gitHubDeploymentStatus =
            GitHubDeploymentStatus.getGitHubDeploymentStatusFromString(status);
        if (gitHubDeploymentStatus == GitHubDeploymentStatus.DEPLOYED) {
          final String deployedUrl = gitHubStatusResponse.getHtml_url();
          Validate.notBlank(deployedUrl);
          return deployedUrl;
        }
        if (gitHubDeploymentStatus == GitHubDeploymentStatus.ERROR) {
          throw new PortfolioGenerationFailedException(String.format("GitHub portfolio generation failed for deploymentId %s", deploymentSweeperAction.deploymentId));
        }
        sleeper.sleep(Duration.ofSeconds(60).toMillis());
      }
    }
  }
}
