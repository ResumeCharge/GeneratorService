package com.portfolio.generator.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JGitFactoryTest {

  Git gitMock;

  @BeforeEach
  void setUp() {
    gitMock = mock(Git.class);
  }

  @Test
  void test() {
    final JGitFactory jGitFactory = new JGitFactory();
    final AddCommand addCommand = jGitFactory.getNewAddCommand(gitMock);
    final CredentialsProvider credentialsProvider =
        jGitFactory.getNewCredentialsProvider("username", "password");
    assertThat(addCommand).isNotNull();
    assertThat(credentialsProvider).isNotNull();

  }

}