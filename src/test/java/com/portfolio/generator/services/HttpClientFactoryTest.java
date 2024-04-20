package com.portfolio.generator.services;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.Test;


class HttpClientFactoryTest {
  @Test
  void test() {
    final IHttpClientFactory httpClientFactory = new HttpClientFactory();
    final CloseableHttpClient client = httpClientFactory.newCloseableHttpClient();
    assertThat(client).isNotNull();
  }
}