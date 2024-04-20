package com.portfolio.generator.utilities.factories;

import java.time.LocalDateTime;
import java.time.ZoneId;

public interface IDateTimeFactory {
  LocalDateTime getLocalDateTimeNow(final ZoneId zoneId);
}
