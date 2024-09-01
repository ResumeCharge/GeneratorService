package com.portfolio.generator.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.generator.models.ActionType;
import com.portfolio.generator.models.ActionsModel;
import com.portfolio.generator.models.ProcessorOptionsModel;
import com.portfolio.generator.models.ResumeModel;
import com.portfolio.generator.models.resumeModels.InstructionsModel;
import com.portfolio.generator.models.staticsite.PortfolioGenerationTask;
import com.portfolio.generator.models.staticsite.StaticSiteRequestModel;
import com.portfolio.generator.processors.IActionProcessor;
import com.portfolio.generator.processors.IOptionsProcessor;
import com.portfolio.generator.processors.ITemplateProcessor;
import com.portfolio.generator.services.staticsites.github.IGitHubService;
import com.portfolio.generator.utilities.exceptions.ActionProcessingFailedException;
import com.portfolio.generator.utilities.exceptions.PortfolioGenerationFailedException;
import com.portfolio.generator.utilities.exceptions.TemplateProcessingFailedException;
import com.portfolio.generator.utilities.helpers.*;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.portfolio.generator.services.PortfolioConstants.portfolioLocationsMap;

@Component
public class PortfolioGeneratorService implements IPortfolioGeneratorService {
  private final Logger logger = LoggerFactory.getLogger(PortfolioGeneratorService.class);
  private static final String PORTFOLIO_NOT_FOUND_ERROR = "%s is not a valid portfolio type for request %s";
  private static final String DEPLOYMENT_PROVIDER_NULL = "DeploymentProvider was null for request %s";
  private static final String START_ACTION_PROCESSING_MSG =
      "Starting action process for request [%s]";
  private static final String FINISH_ACTION_PROCESSING_MSG =
      "Finished action process for request [%s]";
  private static final String STATIC_SITE_REQUEST_MANAGER_CREATED =
      "Static site request manager initialized with UUID [%s]";
  private static final String DEPLOY_STATIC_SITE_MESSAGE =
      "Starting static site deployment for request [%s]";
  private final ITemplateProcessor templateProcessor;
  private final IActionProcessor actionProcessor;
  private final IOptionsProcessor optionsProcessor;
  private final IGitHubService gitHubService;
  private final StaticSiteRequestManagerHelper resumeManagerHelper;
  private final DeploymentStatusHelper deploymentStatusHelper;
  private final IResourceHelper resourceHelper;
  @Value("${RESOURCES_OUTPUT_ROOT}")
  private String resourceOutputRoot;

  public PortfolioGeneratorService(
      final ITemplateProcessor templateProcessor,
      final IActionProcessor actionProcessor,
      final IOptionsProcessor optionsProcessor,
      final IGitHubService gitHubService,
      final DeploymentStatusHelper deploymentStatusHelper,
      IResourceHelper resourceHelper
  ) {
    this.templateProcessor = templateProcessor;
    this.actionProcessor = actionProcessor;
    this.optionsProcessor = optionsProcessor;
    this.gitHubService = gitHubService;
    this.deploymentStatusHelper = deploymentStatusHelper;
    this.resourceHelper = resourceHelper;
    this.resumeManagerHelper = new StaticSiteRequestManagerHelper();
  }

  @Override
  public void generatePortfolio(final StaticSiteRequestModel request)
      throws PortfolioGenerationFailedException {
    try {
      validateRequest(request);
      //Set a unique UUID
      request.resume.setUUID(UUID.randomUUID().toString());
      logger.info(String.format(
          STATIC_SITE_REQUEST_MANAGER_CREATED,
          request.resume.getUUID()
      ));
      deploymentStatusHelper.updateDeploymentProgress(
          new DeploymentStatus(DeploymentStatusType.PROCESSING, 10L,
              request.deploymentId
          ));
      final String websiteTemplateName = request.websiteDetails.getTemplateName();
      final List<ActionsModel> actions = getActionsFromFile(websiteTemplateName);
      logger.info(String.format(
          START_ACTION_PROCESSING_MSG,
          request.resume.getUUID()
      ));
      processActions(actions, request);
      logger.info(String.format(
          FINISH_ACTION_PROCESSING_MSG,
          request.resume.getUUID()
      ));
    } catch (final Exception e) {
      logger.error("Portfolio generation failed", e);
      throw new PortfolioGenerationFailedException("Unable to create portfolio", e);
    } finally {
      cleanGeneratedOutput(request);
    }
  }

  /*All this logic should get moved to the action processor probably */
  void processActions(
      final List<ActionsModel> actions, final StaticSiteRequestModel staticSiteRequest
  )
      throws TemplateProcessingFailedException, ActionProcessingFailedException, GitAPIException,
      URISyntaxException, IOException, PortfolioGenerationFailedException, InterruptedException {
    deploymentStatusHelper.updateDeploymentProgress(
        new DeploymentStatus(
            25L,
            staticSiteRequest.deploymentId
        ));
    for (final ActionsModel action : actions) {
      if (!hasValidOptions(action, staticSiteRequest)) {
        continue;
      }
      if (action.getActionType().equals(ActionType.PROCESS_TEMPLATE)) {
        processTemplate(action, staticSiteRequest);
      } else if (action.getActionType().equals(ActionType.PROCESS_TEMPLATE_FROM_ARRAY)) {
        processTemplateWithArray(action, staticSiteRequest);
      } else if (action.getActionType().equals(ActionType.DEPLOY_STATIC_SITE)) {
        deployStaticSite(staticSiteRequest);
      } else {
        actionProcessor.processAction(action, staticSiteRequest);
      }
    }
  }

  List<ActionsModel> getActionsFromFile(final String websiteTemplateName) throws IOException {
    final ObjectMapper mapper = new ObjectMapper();
    final String actions = resourceHelper.getResourceAsString(String.format("templates/%s/generator-config.json", websiteTemplateName));
    final InstructionsModel instructions =
        mapper.readValue(actions, InstructionsModel.class);
    return Arrays.asList(instructions.getActions());
  }

  private void processTemplate(
      final ActionsModel action, final StaticSiteRequestModel staticSiteRequestManager
  )
      throws TemplateProcessingFailedException {
    final ResumeModel resume = staticSiteRequestManager.resume;
    final String templateFileInputLocation =
        getFilePath(action.getInputLocation(), resume.getUUID());
    final String templateFileOutputLocation =
        getFilePath(action.getOutputLocation(), resume.getUUID());
    templateProcessor.processTemplate(
        staticSiteRequestManager, "staticSiteRequestManager", templateFileInputLocation,
        templateFileOutputLocation
    );
  }

  private void processTemplateWithArray(
      final ActionsModel action, final StaticSiteRequestModel staticSiteRequestManager
  )
      throws TemplateProcessingFailedException {
    final ResumeModel resume = staticSiteRequestManager.resume;
    //get the array from the helper
    final List<Object> dataList =
        this.resumeManagerHelper.getArrayFromResumeManager(
            staticSiteRequestManager,
            action.getDataKey()
        );
    //loop through the array, calling process template for each action
    //Why is this code so bad???
    int index = 0;
    for (final Object data : dataList) {
      final String templateFileInputLocation =
          getFilePath(action.getInputLocation(), resume.getUUID());
      final String templateFileOutputLocation =
          getFilePath(action.getOutputLocation(), resume.getUUID(), action.getDataKey() + "-" + index++);
      templateProcessor.processTemplate(
          data, action.getDataKey(), templateFileInputLocation, templateFileOutputLocation);
    }
  }

  /**
   * Sent the request to deploy the static site
   **/
  private void deployStaticSite(final StaticSiteRequestModel staticSiteRequest)
      throws GitAPIException, URISyntaxException, IOException, PortfolioGenerationFailedException, InterruptedException {
    deploymentStatusHelper.updateDeploymentProgress(
        new DeploymentStatus(
            50L,
            staticSiteRequest.deploymentId
        ));
    final ResumeModel resume = staticSiteRequest.resume;
    final Path pathToLocalFolder = Paths.get(getFilePath("out/generated-templates/%s", resume.getUUID()));
    logger.info(String.format(DEPLOY_STATIC_SITE_MESSAGE, resume.getUUID()));
    final PortfolioGenerationTask portfolioGenerationTask = new
        PortfolioGenerationTask.Builder()
        .setDeploymentProvider(staticSiteRequest.deploymentProvider)
        .setoAuthToken(staticSiteRequest.oAuthToken)
        .setUUID(staticSiteRequest.resume.getUUID())
        .setPathToLocalPagesWebsiteFolder(pathToLocalFolder)
        .setRepoName(staticSiteRequest.repoName)
        .setCodeBuildProject(staticSiteRequest.codeBuildProject)
        .setWebsiteIdentifier(staticSiteRequest.websiteDetails.getWebsiteIdentifier())
        .setBucketName(staticSiteRequest.s3BucketName)
        .setCloudFrontDistributionId(staticSiteRequest.cloudFrontDistributionId)
        .setDeploymentId(staticSiteRequest.deploymentId)
        .setGithubUserName(staticSiteRequest.githubUserName)
        .build();
    switch (staticSiteRequest.deploymentProvider) {
      case GITHUB:
        gitHubService.deployNewGitHubPagesWebsite(portfolioGenerationTask);
        deploymentStatusHelper.updateDeploymentProgress(
            new DeploymentStatus(DeploymentStatusType.SENT_TO_GITHUB, 75L,
                staticSiteRequest.deploymentId
            ));
        break;
      default:
        throw new PortfolioGenerationFailedException(
            String.format("Invalid deployment provider %s for request %s",
                staticSiteRequest.deploymentId, staticSiteRequest),
            new IllegalArgumentException());
    }
  }

  private void validateRequest(final StaticSiteRequestModel request) throws PortfolioGenerationFailedException {
    final String websiteTemplateName = request.websiteDetails.getTemplateName();
    if (!portfolioLocationsMap.containsKey(websiteTemplateName)) {
      throw new PortfolioGenerationFailedException(String.format(PORTFOLIO_NOT_FOUND_ERROR, websiteTemplateName, request), new IllegalArgumentException());
    }
    if (request.deploymentProvider == null) {
      throw new PortfolioGenerationFailedException(String.format(DEPLOYMENT_PROVIDER_NULL, request));
    }
  }

  private String getFilePath(final String location, final String... args) {
    return String.format(resourceOutputRoot + "/" + location, args);
  }

  /**
   * returns true if the options array is empty or all the options are valid
   **/
  boolean hasValidOptions(
      final ActionsModel action, final StaticSiteRequestModel staticSiteRequestManager
  ) {
    if (action.getOptions() == null) {
      return true;
    }
    for (final ProcessorOptionsModel processorOptionsModel : action.getOptions()) {
      if (!optionsProcessor.isValid(processorOptionsModel, staticSiteRequestManager)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Once the portfolio is sent to Github we have no more use for the generated output, so delete it
   **/
  private void cleanGeneratedOutput(final StaticSiteRequestModel staticSiteRequestManager) {
    if(staticSiteRequestManager.resume == null){
      return;
    }
    final String outputLocation = String.format("%s/out/generated-templates/%s", resourceOutputRoot, staticSiteRequestManager.resume.getUUID());
    final Path outputPath = Paths.get(outputLocation);
    if(!Files.exists(outputPath)){
      return;
    }
    try {
      FileUtils.deleteDirectory(outputPath.toFile());
    } catch (final IOException e) {
      logger.warn("Failed to delete output directory " + outputLocation, e);
    }
  }
}
