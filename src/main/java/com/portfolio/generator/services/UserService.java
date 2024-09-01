package com.portfolio.generator.services;

import com.portfolio.generator.utilities.http.IHttpUtils;
import org.apache.commons.lang3.Validate;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
public class UserService implements IUserService {
  @Value("${USER_SERVICE_PORT}")
  private String userServicePort;
  private final CloseableHttpClient client;
  private final IHttpUtils httpUtils;

  public UserService(final CloseableHttpClient client, final IHttpUtils httpUtils) {
    this.client = client;
    this.httpUtils = httpUtils;
  }

  @Override
  public String getUserOAuthToken(final String userId) throws IOException {
    final String endpoint = getUserServicePath(Optional.of(String.format("/users/%s/token", userId)));
    return callUserEndpointAndReturnString(endpoint);
  }

  @Override
  public String getUserGitHubUserName(final String userId) throws IOException {
    final String endpoint = getUserServicePath(Optional.of(String.format("/users/%s/githubUsername", userId)));
    return callUserEndpointAndReturnString(endpoint);
  }

  private String callUserEndpointAndReturnString(final String url)
          throws IOException {
    final HttpGet request = new HttpGet(url);
    request.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString());
    try (final CloseableHttpResponse response = client.execute(request)) {
      final HttpEntity entity = response.getEntity();
      final int status = response.getStatusLine().getStatusCode();
      if (status == 200) {
        final String result = httpUtils.convertEntityToString(entity);
        Validate.notBlank(result);
        return result;
      }
      throw new IOException();
    }
  }

  @Override
  public Boolean healthCheckUserService() {
    try {
      callUserEndpointAndReturnString(getUserServicePath(Optional.empty()));
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private String getUserServicePath(final Optional<String> endpoint) {
    String userServiceEndpoint = String.format("http://localhost:%s/api", userServicePort);
    if (endpoint.isPresent()) {
      userServiceEndpoint = userServiceEndpoint + endpoint.get();
    }
    return userServiceEndpoint;
  }

  public String getUserServicePort() {
    return userServicePort;
  }

  public void setUserServicePort(String userServicePort) {
    this.userServicePort = userServicePort;
  }
}
