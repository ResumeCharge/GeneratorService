package com.portfolio.generator.utilities.aws.factories;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import org.springframework.stereotype.Component;

@Component
public class CredentialsProviderFactory implements ICredentialsProviderFactory {
  @Override
  public ProfileCredentialsProvider getProfileCredentialsProvider(final String profileName) {
    return new ProfileCredentialsProvider(profileName);
  }
}
