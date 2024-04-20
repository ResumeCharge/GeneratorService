package com.portfolio.generator.services;

import java.io.IOException;

public interface IUserService {
  String getUserOAuthToken(String userId) throws IOException;

  String getUserGitHubUserName(final String userId) throws IOException;
}
