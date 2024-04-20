package com.portfolio.generator.models;

public class GithubUserModel {
  private String username;
  private String oAuthToken;

  public GithubUserModel() {
  }

  public GithubUserModel(String username, String oAuthToken) {
    this.username = username;
    this.oAuthToken = oAuthToken;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getOAuthToken() {
    return oAuthToken;
  }

  public void setOAuthToken(String oAuthToken) {
    this.oAuthToken = oAuthToken;
  }
}
