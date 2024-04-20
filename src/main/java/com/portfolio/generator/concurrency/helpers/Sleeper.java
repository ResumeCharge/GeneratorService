package com.portfolio.generator.concurrency.helpers;

import org.springframework.stereotype.Component;

@Component
public class Sleeper implements ISleeper {
  @Override
  public void sleep(final long millis) throws InterruptedException {
    Thread.sleep(millis);
  }
}
