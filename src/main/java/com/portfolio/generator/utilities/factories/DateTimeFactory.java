package com.portfolio.generator.utilities.factories;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class DateTimeFactory implements IDateTimeFactory {
  @Override
  public LocalDateTime getLocalDateTimeNow(final ZoneId zoneId) {
    return LocalDateTime.now(zoneId);
  }
}
