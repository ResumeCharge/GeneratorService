package com.portfolio.generator.concurrency.factory;

import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class ExecutorServiceFactory implements IExecutorServiceFactory {
  @Override
  public ExecutorService getFixedSizedThreadPool(final int threadPoolSize) {
    return Executors.newFixedThreadPool(threadPoolSize);
  }
}
