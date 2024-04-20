package com.portfolio.generator.managers;

import com.portfolio.generator.concurrency.factory.IExecutorServiceFactory;
import com.portfolio.generator.deployments.jobs.PendingDeploymentSweeperAction;
import com.portfolio.generator.models.staticsite.StaticSiteRequestModel;
import com.portfolio.generator.services.IPortfolioGeneratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Task manager for handling requests to deploy portfolios to Github
 **/
@Component
public class GithubPortfolioGenerationTaskManager implements IPortfolioGenerationTaskManager {
  private final Logger logger = LoggerFactory.getLogger(GithubPortfolioGenerationTaskManager.class);
  private final int MAX_THREADS = 100;
  private final IPortfolioGeneratorService portfolioGeneratorService;
  private final IExecutorServiceFactory executorServiceFactory;
  private ExecutorService executorService;

  public GithubPortfolioGenerationTaskManager(
      final IPortfolioGeneratorService portfolioGeneratorService,
      final IExecutorServiceFactory executorServiceFactory
  ) {
    this.portfolioGeneratorService = portfolioGeneratorService;
    this.executorServiceFactory = executorServiceFactory;
  }

  @Override
  public Future<PendingDeploymentSweeperAction> schedulePortfolioGenerationTask(
      final PendingDeploymentSweeperAction deploymentRequest
  ) {
    final StaticSiteRequestModel request = getStaticSiteRequestForPendingDeploymentTask(deploymentRequest);
    final GithubTaskCallable callable = new GithubTaskCallable(portfolioGeneratorService, deploymentRequest, request);
    if (executorService == null) {
      logger.info("Initializing executor service");
      executorService = executorServiceFactory.getFixedSizedThreadPool(MAX_THREADS);
    }
    logger.info("Received github portfolio generation request");
    return executorService.submit(callable);
  }

  private StaticSiteRequestModel getStaticSiteRequestForPendingDeploymentTask(final PendingDeploymentSweeperAction deploymentRequest) {
    return new StaticSiteRequestModel.Builder()
        .setResume(deploymentRequest.resume)
        .setDeploymentId(deploymentRequest.deploymentId)
        .setUserId(deploymentRequest.userId)
        .setWebsiteDetails(deploymentRequest.websiteDetails)
        .setoAuthToken(deploymentRequest.oAuthToken)
        .setDeploymentProvider(deploymentRequest.deploymentProvider)
        .setGithubUserName(deploymentRequest.gitHubUserName)
        .build();
  }

  static class GithubTaskCallable implements Callable<PendingDeploymentSweeperAction> {
    private final IPortfolioGeneratorService portfolioGeneratorService;
    private final PendingDeploymentSweeperAction deploymentRequest;
    private final StaticSiteRequestModel request;

    GithubTaskCallable(final IPortfolioGeneratorService portfolioGeneratorService,
                       final PendingDeploymentSweeperAction deploymentRequest,
                       final StaticSiteRequestModel request) {

      this.portfolioGeneratorService = portfolioGeneratorService;
      this.deploymentRequest = deploymentRequest;
      this.request = request;
    }

    @Override
    public PendingDeploymentSweeperAction call() throws Exception {
      portfolioGeneratorService.generatePortfolio(request);
      return deploymentRequest;
    }
  }
}
