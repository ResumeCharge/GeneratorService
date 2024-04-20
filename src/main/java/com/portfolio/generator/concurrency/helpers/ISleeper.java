package com.portfolio.generator.concurrency.helpers;

public interface ISleeper {
  void sleep(final long millis) throws InterruptedException;
}
