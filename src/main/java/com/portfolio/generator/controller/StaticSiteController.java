package com.portfolio.generator.controller;


import com.portfolio.generator.models.GenerateStaticSiteRequestModel;
import com.portfolio.generator.models.staticsite.DeploymentProvider;
import com.portfolio.generator.models.staticsite.StaticSiteRequestModel;
import com.portfolio.generator.services.IPortfolioGeneratorService;
import com.portfolio.generator.utilities.exceptions.PortfolioGenerationFailedException;
import com.portfolio.generator.utilities.helpers.IDeploymentStatusHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Main entry point for communication with other microservices.
 * Accepts a single POST request at the static-sites/deploy
 * endpoint
 */
@RestController
@RequestMapping("static-sites")
public class StaticSiteController {
  private static final Logger logger = LoggerFactory.getLogger(StaticSiteController.class);
  private final IPortfolioGeneratorService portfolioGeneratorService;

  public StaticSiteController(
      final IPortfolioGeneratorService portfolioGeneratorService,
      final IDeploymentStatusHelper deploymentStatusHelper
  ) {
    this.portfolioGeneratorService = portfolioGeneratorService;
  }

  @PostMapping("/deploy")
  public void createPortfolio(
      @RequestBody final GenerateStaticSiteRequestModel requestModel
  ) throws PortfolioGenerationFailedException {
    logger.info("GENERATE PORTFOLIO!");
    final StaticSiteRequestModel staticSiteRequestModel = new StaticSiteRequestModel.Builder()
        .setResume(requestModel.getResume())
        .setDeploymentId(requestModel.getDeploymentId())
        .setUserId(requestModel.getUserId())
        .setCreatedAt(requestModel.getResume().getCreatedAt())
        .setDeploymentProvider(DeploymentProvider.getDeploymentProviderFromString(requestModel.getDeploymentProvider()))
        .setWebsiteDetails(requestModel.getWebsiteDetails())
        .build();
    portfolioGeneratorService.generatePortfolio(staticSiteRequestModel);
  }
}