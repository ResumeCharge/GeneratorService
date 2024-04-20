package com.portfolio.generator.utilities.helpers;

import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Component;

@Component
public class EnvironmentVariableHelper {
  public String getEnvrionmentVariable() {
    final String token = System.getProperty("USER_TOKEN_TEST");
    Validate.notBlank(token, "Error when trying to get USER_TOKEN_TEST, was blank");
    return token;
  }

  public String getClientId() {
    final String clientId = System.getProperty("CLIENT_ID");
    Validate.notBlank(clientId, "Error when trying to get CLIENT_ID, was blank");
    return clientId;
  }

  public String getClientSecret() {
    final String clientSecret = System.getProperty("CLIENT_SECRET");
    Validate.notBlank(clientSecret, "Error when trying to get CLIENT_SECRET, was blank");
    return clientSecret;
  }
}
