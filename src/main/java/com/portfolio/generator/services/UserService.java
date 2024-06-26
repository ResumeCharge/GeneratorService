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

@Component
public class UserService implements IUserService {
  private static final String CLIENT_ID_HEADER = "client_id";
  private static final String CLIENT_SECRET_HEADER = "client_secret";
  private static final String USER_SERVICE_TOKEN_ENDPOINT = "http://localhost/api/users/%s/token";
  private static final String USER_SERVICE_USERNAME_ENDPOINT = "http://localhost/api/users/%s/githubUsername";
  private final CloseableHttpClient client;
  private final IHttpUtils httpUtils;

  @Value("${CLIENT_SECRET}")
  private String clientSecret;

  @Value("${CLIENT_ID}")
  private String clientId;

  public UserService(final CloseableHttpClient client, final IHttpUtils httpUtils) {
    this.client = client;
    this.httpUtils = httpUtils;
  }

  @Override
  public String getUserOAuthToken(final String userId) throws IOException {
    final String endpoint = String.format(USER_SERVICE_TOKEN_ENDPOINT, userId);
    return callUserEndpointAndReturnString(endpoint);
  }

  @Override
  public String getUserGitHubUserName(final String userId) throws IOException {
    final String endpoint = String.format(USER_SERVICE_USERNAME_ENDPOINT, userId);
    return callUserEndpointAndReturnString(endpoint);
  }

  private String callUserEndpointAndReturnString(final String endPoint)
      throws IOException {
    final HttpGet request = new HttpGet(endPoint);
    request.addHeader(CLIENT_ID_HEADER, clientId);
    request.addHeader(CLIENT_SECRET_HEADER, clientSecret);
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

  public String getClientSecret() {
    return clientSecret;
  }

  public void setClientSecret(final String clientSecret) {
    this.clientSecret = clientSecret;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(final String clientId) {
    this.clientId = clientId;
  }
}
