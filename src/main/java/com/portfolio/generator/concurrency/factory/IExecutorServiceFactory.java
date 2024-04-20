package com.portfolio.generator.concurrency.factory;

import java.util.concurrent.ExecutorService;

public interface IExecutorServiceFactory {
  ExecutorService getFixedSizedThreadPool(final int threadPoolSize);
}
