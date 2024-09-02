package com.portfolio.generator.templates;

import com.portfolio.generator.models.ResumeModel;
import com.portfolio.generator.models.WebsiteDetailsModel;
import com.portfolio.generator.models.resumeModels.*;
import com.portfolio.generator.models.staticsite.DeploymentProvider;
import com.portfolio.generator.models.staticsite.StaticSiteRequestModel;
import com.portfolio.generator.processors.IActionProcessor;
import com.portfolio.generator.processors.IOptionsProcessor;
import com.portfolio.generator.processors.ITemplateProcessor;
import com.portfolio.generator.services.PortfolioGeneratorService;
import com.portfolio.generator.services.staticsites.github.IGitHubService;
import com.portfolio.generator.utilities.IO.IOFactory;
import com.portfolio.generator.utilities.exceptions.ActionProcessingFailedException;
import com.portfolio.generator.utilities.exceptions.PortfolioGenerationFailedException;
import com.portfolio.generator.utilities.exceptions.TemplateProcessingFailedException;
import com.portfolio.generator.utilities.helpers.DeploymentStatusHelper;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BeautifulJekyllTest {
  @Mock
  private ITemplateProcessor templateProcessorMock;
  @Mock
  private IActionProcessor actionProcessorMock;
  @Mock
  private IOptionsProcessor optionsProcessorMock;
  @Mock
  private IGitHubService gitHubServiceMock;
  @Mock
  private DeploymentStatusHelper deploymentStatusHelperMock;

  private PortfolioGeneratorService portfolioGeneratorService;

  @BeforeEach
  void setUp() {
    final IOFactory ioFactory = new IOFactory();

    portfolioGeneratorService =
            new PortfolioGeneratorService(templateProcessorMock, actionProcessorMock,
                    optionsProcessorMock, gitHubServiceMock,
                    deploymentStatusHelperMock, ioFactory
            );
  }

  @Test
  void testProcessTemplate()
      throws PortfolioGenerationFailedException, GitAPIException, URISyntaxException, IOException,
      ActionProcessingFailedException, TemplateProcessingFailedException {
    final String uuid = UUID.randomUUID().toString();
    final ResumeModel resume = new ResumeModel();
    resume.setUUID(uuid);
    final LanguageModel languageModel1 = new LanguageModel("English", "Fluent");
    final LanguageModel languageModel2 = new LanguageModel("French", "Fluent");
    final List<LanguageModel> languageModelList = new ArrayList<>();
    languageModelList.add(languageModel1);
    languageModelList.add(languageModel2);
    final PersonalDetailsModel personalDetails = new PersonalDetailsModel(
        "Adam",
        "Lawson",
        "Software Engineer",
        null,
        "A cool dev",
        "",
        "test@test.com",
        "16136065783",
        "adamlawson.dev",
        "alaws057",
        "adamlawson99",
        languageModelList
    );
    final ExtraLinkModel extraLinkModel = new ExtraLinkModel("bitbucket", "adambitbucket");
    final List<ExtraLinkModel> extraLinkModelList = new ArrayList<>();
    extraLinkModelList.add(extraLinkModel);
    final CareerSummaryModel careerSummaryModel = new CareerSummaryModel("I did cool stuff");
    final EducationModel educationModel = new EducationModel(
        "B.ASc Software Engineering",
        "University of Ottawa",
        "September 2017",
        "December 2021",
        "* Was really fun\n* Did goblin t'hings\n* **Hello World!**"
    );
    final List<EducationModel> educationModelList = new ArrayList<>();
    educationModelList.add(educationModel);
    final WorkExperienceModel workExperienceModel = new WorkExperienceModel(
        "Junior Software Engineer",
        "Telepin",
        "Ottawa, ON",
        "January 2019",
        "April 2019",
        "Did some stuff with money"
    );
    final WorkExperienceModel workExperienceModel2 = new WorkExperienceModel(
        "SDE-AWS",
        "Telepin",
        "Ottawa, ON",
        "January 2019",
        "April 2019",
        "Did some **stuff** with money"
    );
    final List<WorkExperienceModel> workExperienceModelList = new ArrayList<>();
    workExperienceModelList.add(workExperienceModel);
    workExperienceModelList.add(workExperienceModel2);
    final ProjectModel projectModel =
        new ProjectModel("Discord bot", "built a discord bot with python");
    final List<ProjectModel> projectModelList = new ArrayList<>();
    projectModelList.add(projectModel);
    final SkillsModel skills = new SkillsModel();
    skills.setSkills("MY AWESOME SKILLS!");

    resume.setPersonalDetails(personalDetails);
    resume.setExtraLinkList(extraLinkModelList);
    resume.setCareerSummary(careerSummaryModel);
    resume.setEducationList(educationModelList);
    resume.setWorkExperienceList(workExperienceModelList);
    resume.setProjectsList(projectModelList);

    //extra website details
    final Map<String, String> extraDetails = new HashMap<>();
    extraDetails.put("about", "* I **love** tech!\n * Tech is cool!");
    extraDetails.put(
        "resumeLink", "https://test-bucket-pgen.s3.us-west-2.amazonaws.com/resume.pdf");
    extraDetails.put("showContactInfo", "true");
    extraDetails.put("lookingForWork", "true");
    resume.setExtraDetails(extraDetails);

    resume.setUUID(uuid);
    final WebsiteDetailsModel websiteDetails = new WebsiteDetailsModel();
    websiteDetails.setDescription("My awesome website");
    websiteDetails.setTitle("My website title");
    websiteDetails.setTemplateName("beautiful-jekyll");
    when(optionsProcessorMock.isValid(any(), any())).thenReturn(true);
    final StaticSiteRequestModel request = new StaticSiteRequestModel.Builder()
        .setResume(resume)
        .setUserId("1234")
        .setWebsiteDetails(websiteDetails)
        .setDeploymentProvider(DeploymentProvider.GITHUB)
        .build();
    portfolioGeneratorService.setStaticAssetsLocation("./assets");
    portfolioGeneratorService.generatePortfolio(request);
    verify(gitHubServiceMock, times(1)).deployNewGitHubPagesWebsite(any());
    verify(deploymentStatusHelperMock, times(4)).updateDeploymentProgress(any());
    verify(actionProcessorMock, times(12)).processAction(any(), any());
    verify(templateProcessorMock, times(10)).processTemplate(any(), any(), any(), any());
  }
}
