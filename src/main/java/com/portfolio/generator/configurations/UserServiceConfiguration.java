package com.portfolio.generator.configurations;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserServiceConfiguration {
  @Bean
  public CloseableHttpClient closeableHttpClient() {
    return HttpClientBuilder.create().build();
  }
}
