package com.portfolio.generator.managers;

import com.portfolio.generator.concurrency.factory.IExecutorServiceFactory;
import com.portfolio.generator.deployments.jobs.PendingDeploymentSweeperAction;
import com.portfolio.generator.models.staticsite.StaticSiteRequestModel;
import com.portfolio.generator.services.IPortfolioGeneratorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GithubPortfolioGenerationTaskManagerTest {
  private GithubPortfolioGenerationTaskManager githubPortfolioGenerationTaskManager;
  @Mock
  private IPortfolioGeneratorService portfolioGeneratorService;
  @Mock
  private IExecutorServiceFactory executorServiceFactory;

  @Mock
  private ExecutorService executorService;

  @BeforeEach
  void setUp() {
    githubPortfolioGenerationTaskManager = new GithubPortfolioGenerationTaskManager(portfolioGeneratorService, executorServiceFactory);
  }

  @Test
  void schedulePortfolioGenerationTask() throws Exception {
    when(executorServiceFactory.getFixedSizedThreadPool(anyInt())).thenReturn(executorService);
    final ArgumentCaptor<GithubPortfolioGenerationTaskManager.GithubTaskCallable> argumentCaptor =
        ArgumentCaptor.forClass(GithubPortfolioGenerationTaskManager.GithubTaskCallable.class);
    githubPortfolioGenerationTaskManager.schedulePortfolioGenerationTask(new PendingDeploymentSweeperAction.Builder().build());
    verify(executorService).submit(argumentCaptor.capture());
    final Callable<PendingDeploymentSweeperAction> callable = argumentCaptor.getValue();
    final FutureTask future = new FutureTask(callable);
    future.run();
    verify(portfolioGeneratorService).generatePortfolio(any(StaticSiteRequestModel.class));
    assertThat(future.isDone()).isTrue();
  }
}