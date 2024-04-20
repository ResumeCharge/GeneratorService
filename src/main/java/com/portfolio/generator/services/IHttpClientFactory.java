package com.portfolio.generator.services;

import org.apache.http.impl.client.CloseableHttpClient;

/**
 * Factory class with a single method to return a CloseableHttpClient.
 * Created to allow easier mock of the CloseableHttpClient
 */
public interface IHttpClientFactory {
  /**
   * @return CloseableHttpClient
   */
  CloseableHttpClient newCloseableHttpClient();
}
