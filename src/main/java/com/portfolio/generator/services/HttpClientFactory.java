package com.portfolio.generator.services;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class HttpClientFactory implements IHttpClientFactory { // too simple to fail -> no UnitTests
  public CloseableHttpClient newCloseableHttpClient() {
    return HttpClientBuilder.create().build();
  }
}