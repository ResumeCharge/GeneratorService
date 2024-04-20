package com.portfolio.generator.services;

import com.portfolio.generator.models.staticsite.StaticSiteRequestModel;
import com.portfolio.generator.utilities.exceptions.PortfolioGenerationFailedException;

public interface IPortfolioGeneratorService {
  void generatePortfolio(StaticSiteRequestModel request)
      throws PortfolioGenerationFailedException;
}
