package com.portfolio.generator.services.staticsites.github;

import com.portfolio.generator.models.staticsite.PortfolioGenerationTask;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.net.URISyntaxException;

public interface IGitHubService {
  void deployNewGitHubPagesWebsite(final PortfolioGenerationTask request)
      throws GitAPIException, URISyntaxException, IOException;
}
