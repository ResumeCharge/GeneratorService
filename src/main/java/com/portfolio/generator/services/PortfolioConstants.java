package com.portfolio.generator.services;

import java.util.HashMap;
import java.util.Map;

public final class PortfolioConstants {

  public static final Map<String, String> portfolioLocationsMap = new HashMap<>();

  static {
    portfolioLocationsMap.put(
        "alembic", "src/main/resources/templates/alembic/generator-config.json");
    portfolioLocationsMap.put(
        "minimal-mistakes", "src/main/resources/templates/minimal-mistakes/generator-config.json");
    portfolioLocationsMap.put(
        "springfield", "src/main/resources/templates/springfield/generator-config.json");
    portfolioLocationsMap.put("hyde", "src/main/resources/templates/hyde/generator-config.json");
    portfolioLocationsMap.put(
        "beautiful-jekyll", "src/main/resources/templates/beautiful-jekyll/generator-config.json");
    portfolioLocationsMap.put(
        "midgard", "src/main/resources/templates/midgard/generator-config.json");
  }
}
