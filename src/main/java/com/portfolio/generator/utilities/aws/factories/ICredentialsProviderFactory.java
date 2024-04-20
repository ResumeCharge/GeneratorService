package com.portfolio.generator.utilities.aws.factories;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;

public interface ICredentialsProviderFactory {
  ProfileCredentialsProvider getProfileCredentialsProvider(final String profileName);
}
