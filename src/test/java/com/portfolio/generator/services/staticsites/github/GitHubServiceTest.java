package com.portfolio.generator.services.staticsites.github;

import com.portfolio.generator.models.GithubUserModel;
import com.portfolio.generator.models.staticsite.PortfolioGenerationTask;
import com.portfolio.generator.services.IHttpClientFactory;
import com.portfolio.generator.services.JGitFactory;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class GitHubServiceTest {
  @Mock
  private IHttpClientFactory httpClientFactoryMock;
  @Mock
  private CloseableHttpClient httpClientMock;
  @Mock
  private Git gitMock;
  @Mock
  private JGitFactory jGitFactoryMock;

  private GitHubService gitHubService;

  @BeforeEach
  void setUp() {
    gitHubService = new GitHubService(
        jGitFactoryMock,
        httpClientFactoryMock
    );
    when(httpClientFactoryMock.newCloseableHttpClient()).thenReturn(httpClientMock);
  }

  @Test
  void deployNewGitHubPagesWebsite() throws GitAPIException, IOException {
    final TestParameters<HttpGet> testParameters = new TestParameters
        .TestParametersBuilder<HttpGet>()
        .withEntity(false)
        .withStatusCode(200)
        .withHttpMethod(HttpGet.class)
        .build();
    setupHTTPMockWithMethod(testParameters);
    try (final MockedStatic<EntityUtils> utils = Mockito.mockStatic(EntityUtils.class);
         final MockedStatic<Git> git = Mockito.mockStatic(Git.class)) {
      final InitCommand initCommandMock = mock(InitCommand.class);
      final AddCommand addCommandMock = mock(AddCommand.class);
      final CommitCommand commitCommandMock = mock(CommitCommand.class);
      final Repository repositoryMock = mock(Repository.class);
      final StoredConfig storedConfigMock = mock(StoredConfig.class);
      final CredentialsProvider credentialsProviderMock = mock(CredentialsProvider.class);
      final PushCommand pushCommandMock = mock(PushCommand.class);
      final Path pathMock = mock(Path.class);
      utils.when(() -> EntityUtils.toString(any()))
          .thenReturn("{\"login\":\"username\"}");
      git.when(Git::init).thenReturn(initCommandMock);
      when(jGitFactoryMock.getNewAddCommand(gitMock)).thenReturn(addCommandMock);
      when(addCommandMock.addFilepattern(anyString())).thenReturn(addCommandMock);
      when(jGitFactoryMock.getGitForLocalFolder(any(Path.class))).thenReturn(gitMock);
      when(gitMock.commit()).thenReturn(commitCommandMock);
      when(commitCommandMock.setMessage(anyString())).thenReturn(commitCommandMock);
      commitCommandMock.call();
      addCommandMock.call();
      when(gitMock.getRepository()).thenReturn(repositoryMock);
      when(repositoryMock.getConfig()).thenReturn(storedConfigMock);
      storedConfigMock.setString(anyString(), anyString(), anyString(), anyString());
      storedConfigMock.save();
      setupHTTPMockWithMethod(new TestParameters
          .TestParametersBuilder<HttpPatch>()
          .withEntity(false)
          .withStatusCode(200)
          .withHttpMethod(HttpPatch.class)
          .build());
      setupHTTPMockWithMethod(new TestParameters
          .TestParametersBuilder<HttpPost>()
          .withEntity(false)
          .withStatusCode(201)
          .withHttpMethod(HttpPost.class)
          .build());
      when(jGitFactoryMock.getNewCredentialsProvider(anyString(), anyString())).thenReturn(
          credentialsProviderMock);
      when(gitMock.push()).thenReturn(pushCommandMock);
      when(pushCommandMock.setCredentialsProvider(credentialsProviderMock)).thenReturn(
          pushCommandMock);
      pushCommandMock.call();
      final PortfolioGenerationTask portfolioGenerationTask = new PortfolioGenerationTask.Builder()
          .setPathToLocalPagesWebsiteFolder(pathMock)
          .setoAuthToken("123")
          .setUUID("123")
          .setGithubUserName("username")
          .build();
      gitHubService.deployNewGitHubPagesWebsite(portfolioGenerationTask);
    }
  }

  @Test
  void deployNewGitHubPagesWebsiteRepoDoesNotExistAlready() throws GitAPIException, IOException {
    final TestParameters<HttpGet> testParameters = new TestParameters
        .TestParametersBuilder<HttpGet>()
        .withEntity(false)
        .withStatusCode(200, 404)
        .withHttpMethod(HttpGet.class)
        .build();
    setupHTTPMockWithMethod(testParameters);

    final InitCommand initCommandMock = mock(InitCommand.class);
    final AddCommand addCommandMock = mock(AddCommand.class);
    final CommitCommand commitCommandMock = mock(CommitCommand.class);
    final Repository repositoryMock = mock(Repository.class);
    final StoredConfig storedConfigMock = mock(StoredConfig.class);
    final CredentialsProvider credentialsProviderMock = mock(CredentialsProvider.class);
    final PushCommand pushCommandMock = mock(PushCommand.class);
    final Path pathMock = mock(Path.class);
    when(jGitFactoryMock.getNewAddCommand(gitMock)).thenReturn(addCommandMock);
    when(jGitFactoryMock.getGitForLocalFolder(any(Path.class))).thenReturn(gitMock);
    when(addCommandMock.addFilepattern(anyString())).thenReturn(addCommandMock);
    when(gitMock.commit()).thenReturn(commitCommandMock);
    when(commitCommandMock.setMessage(anyString())).thenReturn(commitCommandMock);
    commitCommandMock.call();
    addCommandMock.call();
    when(gitMock.getRepository()).thenReturn(repositoryMock);
    when(repositoryMock.getConfig()).thenReturn(storedConfigMock);
    storedConfigMock.setString(anyString(), anyString(), anyString(), anyString());
    storedConfigMock.save();
    setupHTTPMockWithMethod(new TestParameters
        .TestParametersBuilder<HttpPatch>()
        .withEntity(false)
        .withStatusCode(200)
        .withHttpMethod(HttpPatch.class)
        .build());
    setupHTTPMockWithMethod(new TestParameters
        .TestParametersBuilder<HttpPost>()
        .withEntity(false)
        .withStatusCode(201)
        .withHttpMethod(HttpPost.class)
        .build());
    when(jGitFactoryMock.getNewCredentialsProvider(anyString(), anyString())).thenReturn(
        credentialsProviderMock);
    when(gitMock.push()).thenReturn(pushCommandMock);
    when(pushCommandMock.setCredentialsProvider(credentialsProviderMock)).thenReturn(
        pushCommandMock);
    pushCommandMock.call();
    final PortfolioGenerationTask portfolioGenerationTask = new PortfolioGenerationTask.Builder()
        .setPathToLocalPagesWebsiteFolder(pathMock)
        .setoAuthToken("123")
        .setUUID("123")
        .setGithubUserName("username")
        .build();
    gitHubService.deployNewGitHubPagesWebsite(portfolioGenerationTask);
  }

  @Test
  public void testDoesRepoExistForUser() throws IOException {
    this.setupHttpMock(200);
    final GithubUserModel userModel = new GithubUserModel();
    userModel.setUsername("portfolio-gen");
    userModel.setOAuthToken("token");
    final boolean doesRepoExist =
        gitHubService.doesRepoAlreadyExistForUser("not-a-repo", userModel);
    assertThat(doesRepoExist).isTrue();
  }

  @Test
  public void testDoesRepoExistForUserStatus400() throws IOException {
    final Exception exception = assertThrows(IOException.class, () -> {
      final TestParameters<HttpGet> testParameters = new TestParameters
          .TestParametersBuilder<HttpGet>()
          .withEntity(true)
          .withStatusCode(400)
          .withHttpMethod(HttpGet.class)
          .build();
      setupHTTPMockWithMethod(testParameters);
      final GithubUserModel userModel = new GithubUserModel();
      userModel.setUsername("portfolio-gen");
      userModel.setOAuthToken("token");
      gitHubService.doesRepoAlreadyExistForUser("some-repo", userModel);
    });
    final String expectedMessage = "Invalid user token";
    final String actualMessage = exception.getMessage();
    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void testDoesRepoExistForUserStatusOther() throws IOException {
    final Exception exception = assertThrows(IOException.class, () -> {
      final TestParameters<HttpGet> testParameters = new TestParameters
          .TestParametersBuilder<HttpGet>()
          .withEntity(true)
          .withStatusCode(504)
          .withHttpMethod(HttpGet.class)
          .build();
      setupHTTPMockWithMethod(testParameters);
      final GithubUserModel userModel = new GithubUserModel();
      userModel.setUsername("portfolio-gen");
      userModel.setOAuthToken("token");
      gitHubService.doesRepoAlreadyExistForUser("some-repo", userModel);
    });
    final String expectedMessage = "Error with status code: 504";
    final String actualMessage = exception.getMessage();
    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void testDoesRepoExistForUserRepoDoesNotExist() throws IOException {
    this.setupHttpMock(404);
    final GithubUserModel userModel = new GithubUserModel();
    userModel.setUsername("portfolio-gen");
    userModel.setOAuthToken("token");
    final boolean doesRepoExist =
        gitHubService.doesRepoAlreadyExistForUser("not-a-repo", userModel);
    assertThat(doesRepoExist).isFalse();
  }

  @Test
  public void testDoesRepoExistForUserInvalidToken() {
    assertThrows(IOException.class, () -> {
      this.setupHttpMock(400);
      final GithubUserModel userModel = new GithubUserModel();
      userModel.setUsername("portfolio-gen");
      userModel.setOAuthToken("not-a-token");
      gitHubService.doesRepoAlreadyExistForUser("test-repo", userModel);
    });

  }

  @Test
  void renameRepo() throws IOException {
    final TestParameters<HttpPatch> testParameters = new TestParameters
        .TestParametersBuilder<HttpPatch>()
        .withEntity(false)
        .withStatusCode(200)
        .withHttpMethod(HttpPatch.class)
        .build();
    setupHTTPMockWithMethod(testParameters);
    final GithubUserModel userModel = new GithubUserModel();
    userModel.setUsername("portfolio-gen");
    userModel.setOAuthToken("token");
    gitHubService.renameRepo("some-repo", "test-repo-1", userModel);
  }

  @Test
  void renameRepoStatusInvalid() {
    final Exception exception = assertThrows(IOException.class, () -> {
      final TestParameters<HttpPatch> testParameters = new TestParameters
          .TestParametersBuilder<HttpPatch>()
          .withEntity(false)
          .withStatusCode(404)
          .withHttpMethod(HttpPatch.class)
          .build();
      setupHTTPMockWithMethod(testParameters);
      final GithubUserModel userModel = new GithubUserModel();
      userModel.setUsername("portfolio-gen");
      userModel.setOAuthToken("token");
      gitHubService.renameRepo("some-repo", "test-repo-1", userModel);
    });
    final String expectedMessage = "Error with status code: 404";
    final String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  void doesRepoAlreadyExistForUser() throws IOException {
    final TestParameters<HttpGet> testParameters = new TestParameters
        .TestParametersBuilder<HttpGet>()
        .withEntity(true)
        .withStatusCode(200)
        .withHttpMethod(HttpGet.class)
        .build();
    setupHTTPMockWithMethod(testParameters);
    final GithubUserModel userModel = new GithubUserModel();
    userModel.setUsername("portfolio-gen");
    userModel.setOAuthToken("token");
    final boolean doesRepoExist = gitHubService.doesRepoAlreadyExistForUser("some-repo", userModel);
    assertThat(doesRepoExist).isTrue();
  }

  @Test
  void createNewRemoteRepo() throws IOException {
    final TestParameters<HttpPost> testParameters = new TestParameters
        .TestParametersBuilder<HttpPost>()
        .withEntity(false)
        .withStatusCode(201)
        .withHttpMethod(HttpPost.class)
        .build();
    setupHTTPMockWithMethod(testParameters);
    final GithubUserModel userModel = new GithubUserModel();
    userModel.setUsername("portfolio-gen");
    userModel.setOAuthToken("token");
    gitHubService.createNewRemoteRepo("some-repo", userModel);
  }

  @Test
  void createNewRemoteRepoStatus400() {
    final Exception exception = assertThrows(IOException.class, () -> {
      final TestParameters<HttpPost> testParameters = new TestParameters
          .TestParametersBuilder<HttpPost>()
          .withEntity(false)
          .withStatusCode(400)
          .withHttpMethod(HttpPost.class)
          .build();
      setupHTTPMockWithMethod(testParameters);
      final GithubUserModel userModel = new GithubUserModel();
      userModel.setUsername("portfolio-gen");
      userModel.setOAuthToken("token");
      gitHubService.createNewRemoteRepo("some-repo", userModel);
    });
    final String expectedMessage = "Invalid user token";
    final String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  void createNewRemoteRepoStatusOther() throws IOException {
    final Exception exception = assertThrows(IOException.class, () -> {
      final TestParameters<HttpPost> testParameters = new TestParameters
          .TestParametersBuilder<HttpPost>()
          .withEntity(false)
          .withStatusCode(404)
          .withHttpMethod(HttpPost.class)
          .build();
      setupHTTPMockWithMethod(testParameters);
      final GithubUserModel userModel = new GithubUserModel();
      userModel.setUsername("portfolio-gen");
      userModel.setOAuthToken("token");
      gitHubService.createNewRemoteRepo("some-repo", userModel);
    });
    final String expectedMessage = "Error with status code: 404";
    final String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  private <T extends HttpRequestBase> void setupHTTPMockWithMethod(
      final TestParameters<T> testParameters
  ) throws IOException {
    final CloseableHttpResponse responseMock = mock(CloseableHttpResponse.class);
    final HttpEntity httpEntityMock = mock(HttpEntity.class);
    final StatusLine statusLineMock = mock(StatusLine.class);
    when(httpClientMock.execute(isA(testParameters.httpMethod))).thenReturn(
        responseMock);
    when(responseMock.getStatusLine()).thenReturn(statusLineMock);
    final OngoingStubbing<Integer> stubbing =
        when(statusLineMock.getStatusCode()).thenReturn(testParameters.getStatusCode()[0]);
    for (int i = 1; i < testParameters.getStatusCode().length; i++) {
      stubbing.thenReturn(testParameters.getStatusCode()[i]);
    }
    if (!testParameters.isWithEntity()) {
      return;
    }
    when(responseMock.getEntity()).thenReturn(httpEntityMock);
  }

  private void setupHttpMock(final int statusCode) throws IOException {
    final CloseableHttpResponse responseMock = mock(CloseableHttpResponse.class);
    final StatusLine statusLineMock = mock(StatusLine.class);
    when(httpClientMock.execute(isA(HttpGet.class))).thenReturn(responseMock);
    when(responseMock.getStatusLine()).thenReturn(statusLineMock);
    when(statusLineMock.getStatusCode()).thenReturn(statusCode);
  }

  private static class TestParameters<T extends HttpRequestBase> {
    private final Class<T> httpMethod;
    private final int[] statusCode;

    private final boolean withEntity;

    TestParameters(final TestParametersBuilder<T> builder) {
      this.httpMethod = builder.httpMethod;
      this.statusCode = builder.statusCode;
      this.withEntity = builder.withEntity;
    }

    public Class<T> getHttpMethod() {
      return httpMethod;
    }

    public int[] getStatusCode() {
      return statusCode;
    }

    private boolean isWithEntity() {
      return withEntity;
    }

    private static class TestParametersBuilder<T extends HttpRequestBase> {
      private Class<T> httpMethod;
      private int[] statusCode;
      private boolean withEntity;

      private TestParametersBuilder() {
      }

      public TestParametersBuilder<T> withHttpMethod(final Class<T> httpMethod) {
        this.httpMethod = httpMethod;
        return this;
      }

      public TestParametersBuilder<T> withStatusCode(final int... statusCode) {
        this.statusCode = statusCode;
        return this;
      }

      public TestParametersBuilder<T> withEntity(final boolean withEntity) {
        this.withEntity = withEntity;
        return this;
      }

      public TestParameters<T> build() {
        return new TestParameters<>(this);
      }
    }
  }
}