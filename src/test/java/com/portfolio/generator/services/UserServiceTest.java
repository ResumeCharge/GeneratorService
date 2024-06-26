package com.portfolio.generator.services;

import com.portfolio.generator.utilities.http.IHttpUtils;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
  @Mock
  private IHttpUtils httpUtils;

  @Test
  void getUserOAuthToken() throws IOException {
    final CloseableHttpClient closeableHttpClient = mock(CloseableHttpClient.class);
    final CloseableHttpResponse closeableHttpResponse = mock(CloseableHttpResponse.class);
    final HttpEntity httpEntity = mock(HttpEntity.class);
    final StatusLine statusLine = mock(StatusLine.class);
    when(closeableHttpClient.execute(any())).thenReturn(closeableHttpResponse);
    when(closeableHttpResponse.getEntity()).thenReturn(httpEntity);
    when(closeableHttpResponse.getStatusLine()).thenReturn(statusLine);
    when(statusLine.getStatusCode()).thenReturn(200);
    when(httpUtils.convertEntityToString(any(HttpEntity.class)))
        .thenReturn("hello world");
    final UserService userService = new UserService(closeableHttpClient, httpUtils);
    final String result = userService.getUserOAuthToken("123");
    assertThat(result).isEqualTo("hello world");
  }

  @Test
  void getUserGitHubUserName() throws IOException {
    final CloseableHttpClient closeableHttpClient = mock(CloseableHttpClient.class);
    final CloseableHttpResponse closeableHttpResponse = mock(CloseableHttpResponse.class);
    final HttpEntity httpEntity = mock(HttpEntity.class);
    final StatusLine statusLine = mock(StatusLine.class);
    when(closeableHttpClient.execute(any())).thenReturn(closeableHttpResponse);
    when(closeableHttpResponse.getEntity()).thenReturn(httpEntity);
    when(closeableHttpResponse.getStatusLine()).thenReturn(statusLine);
    when(statusLine.getStatusCode()).thenReturn(200);
    when(httpUtils.convertEntityToString(any(HttpEntity.class)))
        .thenReturn("hello world");
    final UserService userService = new UserService(closeableHttpClient, httpUtils);
    final String result = userService.getUserGitHubUserName("123");
    assertThat(result).isEqualTo("hello world");
  }
}