package com.portfolio.generator.services;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

/**
 * Helper class used as a wrapper around the JGit methods.
 * Largely used to facilitate testing, as mocking the created Command
 * objects in GithubService is really hard.
 */
@Component
public class JGitFactory {
  public AddCommand getNewAddCommand(final Git git) {
    return new AddCommand(git.getRepository());
  }

  public Git getGitForLocalFolder(final Path pathToLocalFolder) throws GitAPIException {
    return Git.init().setDirectory(pathToLocalFolder.toFile()).call();
  }

  public CheckoutCommand getNewCheckoutCommand(final Git git, final boolean createBranch, final String branch) {
    return git.checkout().setCreateBranch(createBranch).setName(branch);
  }

  public CredentialsProvider getNewCredentialsProvider(final String username, final String password) {
    return new UsernamePasswordCredentialsProvider(username, password);
  }
}
