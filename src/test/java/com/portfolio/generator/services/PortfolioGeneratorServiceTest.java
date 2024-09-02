package com.portfolio.generator.services;

import com.portfolio.generator.models.*;
import com.portfolio.generator.models.resumeModels.WorkExperienceModel;
import com.portfolio.generator.models.staticsite.DeploymentProvider;
import com.portfolio.generator.models.staticsite.StaticSiteRequestModel;
import com.portfolio.generator.processors.IActionProcessor;
import com.portfolio.generator.processors.IOptionsProcessor;
import com.portfolio.generator.processors.ITemplateProcessor;
import com.portfolio.generator.services.staticsites.github.IGitHubService;
import com.portfolio.generator.utilities.IO.IOFactory;
import com.portfolio.generator.utilities.exceptions.ActionProcessingFailedException;
import com.portfolio.generator.utilities.exceptions.PortfolioGenerationFailedException;
import com.portfolio.generator.utilities.exceptions.TemplateProcessingFailedException;
import com.portfolio.generator.utilities.helpers.*;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortfolioGeneratorServiceTest {
  private static final Logger log = LoggerFactory.getLogger(PortfolioGeneratorServiceTest.class);
  private static final Path testDirectory =
          Paths.get("out", "test-artifacts", "generated-templates");
  @Mock
  private ITemplateProcessor templateProcessor;
  @Mock
  private IActionProcessor actionProcessor;
  @Mock
  private IOptionsProcessor optionsProcessor;
  @Mock
  private IGitHubService gitHubService;
  @Mock
  private DeploymentStatusHelper deploymentStatusHelper;
  @Mock
  private IOFactory ioFactory;
  private PortfolioGeneratorService portfolioGeneratorService;

  @BeforeAll
  public static void initialize() {
    try {
      if (Files.notExists(testDirectory)) {
        FileUtils.forceMkdir(testDirectory.toFile());
      }
    } catch (final IOException e) {
      log.debug("Unable to create testDirectory for ActionProcessorTest. Failing....");
      System.exit(1);
    }
  }

  @AfterAll
  public static void cleanUp() {
    try {
      FileUtils.deleteDirectory(testDirectory.getParent().toFile());
    } catch (final Exception e) {
      System.out.println("Failed to delete test output directory");
    }
  }

  @BeforeEach
  void setUp() {
    final IOFactory ioFactory = new IOFactory();
    final ResourceLoader resourceLoader = new DefaultResourceLoader();
    IResourceHelper resourceHelper = new ResourceHelper(resourceLoader, ioFactory);
    this.portfolioGeneratorService =
            new PortfolioGeneratorService(
                    templateProcessor,
                    actionProcessor,
                    optionsProcessor,
                    gitHubService,
                    deploymentStatusHelper,
                    ioFactory);
  }

  @Test
  void getActionsFromFile() throws IOException {
    portfolioGeneratorService.setStaticAssetsLocation("./assets");
    final String portfolioType = "alembic";
    final List<ActionsModel> actionsModel =
            portfolioGeneratorService.getActionsFromFile(portfolioType);
    assertThat(actionsModel.size(), is(8));
  }

  @Test
  void testGeneratePortfolioException() {
    final Exception exception = assertThrows(PortfolioGenerationFailedException.class, () -> {
      portfolioGeneratorService.generatePortfolio(new StaticSiteRequestModel.Builder().build());
    });
    final String expectedMessage = "Unable to create portfolio";
    final String actualMessage = exception.getMessage();
    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  void testProcessActionsFromArray()
          throws ActionProcessingFailedException, GitAPIException, TemplateProcessingFailedException,
          URISyntaxException, IOException, PortfolioGenerationFailedException, InterruptedException {

    final ActionsModel action = new ActionsModel();
    action.setActionType(ActionType.PROCESS_TEMPLATE_FROM_ARRAY);
    action.setInputLocation("123");
    action.setOutputLocation("123");
    final List<ProcessorOptionsModel> options = new ArrayList<>();
    final ProcessorOptionsModel option = new ProcessorOptionsModel();
    option.setOptionType(OptionType.REQUIRED);
    option.setOptionValue("WORK_EXPERIENCE");
    action.setDataKey("WORK_EXPERIENCE");
    options.add(option);
    action.setOptions(options);
    final List<ActionsModel> actionsList = new ArrayList<>();
    actionsList.add(action);
    final WorkExperienceModel workExperienceModel = new WorkExperienceModel(
            "Junior Software Engineer",
            "Telepin",
            "Ottawa, ON",
            "January 2019",
            "April 2019",
            "Did some stuff with money"
    );
    final List<WorkExperienceModel> workExperienceModelList = new ArrayList<>();
    workExperienceModelList.add(workExperienceModel);
    final ResumeModel resume = new ResumeModel();
    resume.setWorkExperienceList(workExperienceModelList);
    final StaticSiteRequestModel request = new StaticSiteRequestModel
            .Builder()
            .setResume(resume)
            .build();
    when(optionsProcessor.isValid(eq(option), any(StaticSiteRequestModel.class))).thenReturn(true);
    portfolioGeneratorService.processActions(actionsList, request);
  }

  @Test
  void testGeneratePortfolioGithub()
          throws PortfolioGenerationFailedException, IOException, GitAPIException, URISyntaxException, InterruptedException {
    final ResumeModel resumeModel = new ResumeModel();
    resumeModel.setUUID("UUID");
    final WebsiteDetailsModel websiteDetailsModel = new WebsiteDetailsModel();
    websiteDetailsModel.setTemplateName("alembic");
    final StaticSiteRequestModel request = new StaticSiteRequestModel.Builder()
            .setDeploymentProvider(DeploymentProvider.GITHUB)
            .setUserId("userId")
            .setResume(resumeModel)
            .setoAuthToken("token")
            .setDeploymentId("deploymentId")
            .setWebsiteDetails(websiteDetailsModel)
            .build();
    portfolioGeneratorService.setStaticAssetsLocation("./assets");
    portfolioGeneratorService.generatePortfolio(request);
    final ArgumentCaptor<DeploymentStatus> argumentCaptor = ArgumentCaptor.forClass(DeploymentStatus.class);
    verify(deploymentStatusHelper, times(4)).updateDeploymentProgress(argumentCaptor.capture());
    assertDeploymentStatusUpdatesCalled(argumentCaptor.getAllValues(), true);
  }

  @Test
  void testGeneratePortfolioInvalidWebsiteTemplate() {
    final ResumeModel resumeModel = new ResumeModel();
    final WebsiteDetailsModel websiteDetailsModel = new WebsiteDetailsModel();
    websiteDetailsModel.setTemplateName("fake-template");
    final StaticSiteRequestModel request = new StaticSiteRequestModel.Builder()
            .setWebsiteDetails(websiteDetailsModel)
            .setResume(resumeModel)
            .setDeploymentProvider(DeploymentProvider.GITHUB)
            .build();
    final Exception exception = assertThrows(PortfolioGenerationFailedException.class,
            () -> portfolioGeneratorService.generatePortfolio(request));
    assertTrue(exception.getCause().getMessage().contains("fake-template is not a valid portfolio type"));
  }

  @Test
  void testGeneratePortfolioNullDeploymentProvider() {
    final ResumeModel resumeModel = new ResumeModel();
    final WebsiteDetailsModel websiteDetailsModel = new WebsiteDetailsModel();
    websiteDetailsModel.setTemplateName("alembic");
    final StaticSiteRequestModel request = new StaticSiteRequestModel.Builder()
            .setWebsiteDetails(websiteDetailsModel)
            .setResume(resumeModel)
            .build();
    final Exception exception = assertThrows(PortfolioGenerationFailedException.class,
            () -> portfolioGeneratorService.generatePortfolio(request));
    assertTrue(exception.getCause().getMessage().contains("DeploymentProvider was null"));
  }

  private void assertDeploymentStatusUpdatesCalled(final List<DeploymentStatus> deploymentStatuses, final boolean isGithHub) {
    assertThat(deploymentStatuses.get(0).getStatus(), is(DeploymentStatusType.PROCESSING));
    assertThat(deploymentStatuses.get(0).getProgress(), is(10L));

    assertThat(deploymentStatuses.get(1).getProgress(), is(25L));

    assertThat(deploymentStatuses.get(2).getProgress(), is(50L));

    if (isGithHub) {
      assertThat(deploymentStatuses.get(3).getStatus(), is(DeploymentStatusType.SENT_TO_GITHUB));
      assertThat(deploymentStatuses.get(3).getProgress(), is(75L));
    } else {
      assertThat(deploymentStatuses.get(3).getStatus(), is(DeploymentStatusType.SENT_TO_AWS));
      assertThat(deploymentStatuses.get(3).getProgress(), is(75L));
    }
  }
}