package com.portfolio.generator.services.staticsites.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.generator.models.GithubUserModel;
import com.portfolio.generator.models.staticsite.PortfolioGenerationTask;
import com.portfolio.generator.services.IHttpClientFactory;
import com.portfolio.generator.services.JGitFactory;
import org.apache.commons.lang3.Validate;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Component
public class GitHubService implements IGitHubService {
  private static final String INITIALIZE_LOCAL_FOLDER_MSG =
      "{RequestId: %s} Initializing local folder as git repository";
  private static final String ADD_LOCAL_FILES_TO_COMMIT_MSG =
      "{RequestId: %s} Adding local files to commit";
  private static final String CREATE_COMMIT_MSG =
      "{RequestId: %s} Creating commit for local git repository";
  private static final String CHECK_IF_PAGES_REPO_EXISTS_MSG =
      "{RequestId: %s} Checking if a github pages repo already exists";
  private static final String RENAME_PAGES_REPO_MSG =
      "{RequestId: %s} Renaming existing github pages repo to: %s";
  private static final String SET_REMOTE_FOR_LOCAL_REPO_MSG =
      "{RequestId: %s} Setting remote endpoint for local repo";
  private static final String PUSH_LOCAL_TO_REMOTE_MSG =
      "{RequestId: %s} Pushing local repo to remote";
  private static final String GITHUB_REPOS_ENDPOINT = "https://api.github.com/user/repos";
  private static final String GITHUB_PAGES_ENDPOINT = "https://api.github.com/repos";
  private static final Logger logger = LoggerFactory.getLogger(GitHubService.class);
  private final JGitFactory jGitFactory;
  private final IHttpClientFactory httpClientFactory;

  public GitHubService(
      final JGitFactory jGitFactory,
      final IHttpClientFactory httpClientFactory
  ) {
    this.jGitFactory = jGitFactory;
    this.httpClientFactory = httpClientFactory;
  }

  /**
   * This should get refactored to a generic request object
   */
  @Override
  public void deployNewGitHubPagesWebsite(
      final PortfolioGenerationTask portfolioGenerationTask
  )
      throws GitAPIException, IOException {
    final String oAuthToken = portfolioGenerationTask.oAuthToken;
    final String UUID = portfolioGenerationTask.UUID;
    final Path pathToLocalPagesWebsiteFolder = portfolioGenerationTask.pathToLocalPagesWebsiteFolder;
    Validate.notBlank(oAuthToken);
    final String username = portfolioGenerationTask.githubUserName;
    Validate.notBlank(username);
    final String repoName = String.format("%s.github.io", username);
    final GithubUserModel githubUserModel = new GithubUserModel(username, oAuthToken);
    logger.info(String.format(INITIALIZE_LOCAL_FOLDER_MSG, UUID));
    final Git git = initializeLocalFolderAsGitRepository(pathToLocalPagesWebsiteFolder);
    logger.info(String.format(ADD_LOCAL_FILES_TO_COMMIT_MSG, UUID));
    stageLocalFilesForCommit(git);
    logger.info(String.format(CREATE_COMMIT_MSG, UUID));
    createNewCommitInLocalFolder(git);
    final String url = "";
    setRemoteForLocalGitRepository(git, url);
    logger.info(String.format(CHECK_IF_PAGES_REPO_EXISTS_MSG, UUID));
    if (doesRepoAlreadyExistForUser(repoName, githubUserModel)) {
      final String newRepoName = String.format("%s-resume-charge-%s", repoName, UUID);
      logger.info(String.format(RENAME_PAGES_REPO_MSG, UUID, newRepoName));
      renameRepo(
          repoName, newRepoName,
          githubUserModel
      );
    }
    final String newRepoName = String.format("%s.github.io", githubUserModel.getUsername());
    createNewRemoteRepo(newRepoName, githubUserModel);
    logger.info(String.format(SET_REMOTE_FOR_LOCAL_REPO_MSG, UUID));
    final String remoteURL =
        String.format("https://github.com/%s/%s.github.io.git", username, username);
    setRemoteForLocalGitRepository(git, remoteURL);
    logger.info(String.format(PUSH_LOCAL_TO_REMOTE_MSG, UUID));
    pushLocalFolderToRemote(git, githubUserModel.getOAuthToken());
    createGitHubPagesWebsite(githubUserModel);
  }

  Git initializeLocalFolderAsGitRepository(final Path pathToLocalFolder) throws GitAPIException {
    return jGitFactory.getGitForLocalFolder(pathToLocalFolder);
  }

  void stageLocalFilesForCommit(final Git git) throws GitAPIException {
    final AddCommand addFiles = jGitFactory.getNewAddCommand(git).addFilepattern(".");
    addFiles.call();
  }

  void createNewCommitInLocalFolder(final Git git) throws GitAPIException {
    git.commit().setMessage("Hello world!").call();
  }

  void renameRepo(
      final String originalRepoName, final String newRepoName, final GithubUserModel userModel
  )
      throws IOException {
    final HttpPatch request = new HttpPatch(
        String.format("https://api.github.com/repos/%s/%s", userModel.getUsername(),
            originalRepoName
        ));
    // add request headers
    request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + userModel.getOAuthToken());
    request.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString());
    final Map<String, String> body = new HashMap<>();
    body.put("name", newRepoName);
    final String bodyAsJson = new ObjectMapper().writeValueAsString(body);
    final StringEntity requestEntity = new StringEntity(bodyAsJson);
    request.setEntity(requestEntity);
    try (final CloseableHttpClient httpClient = httpClientFactory.newCloseableHttpClient()) {
      try (final CloseableHttpResponse response = httpClient.execute(request)) {
        final int status = response.getStatusLine().getStatusCode();
        if (status != 200) {
          throw new IOException(String.format("Error with status code: %d",status));
        }
      }
    }
  }

  boolean doesRepoAlreadyExistForUser(final String repoName, final GithubUserModel usermodel)
      throws IOException {
    final HttpGet request = new HttpGet(
        String.format("https://api.github.com/repos/%s/%s", usermodel.getUsername(), repoName));
    // add request headers
    request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + usermodel.getOAuthToken());
    try (final CloseableHttpClient httpClient = httpClientFactory.newCloseableHttpClient()) {
      try (final CloseableHttpResponse response = httpClient.execute(request)) {
        final HttpEntity entity = response.getEntity();
        final int status = response.getStatusLine().getStatusCode();
        if (status == 200) {
          return true;
        }
        if (status == 404) {
          return false;
        }
        if (status == 400) {
          throw new IOException("Invalid user token");
        }
        throw new IOException("Error with status code: " + status);
      }
    }

  }

  void setRemoteForLocalGitRepository(final Git git, final String url) throws IOException {
    final StoredConfig config = git.getRepository().getConfig();
    config.setString("remote", "origin", "url", url);
    config.save();
  }

  void pushLocalFolderToRemote(final Git git, final String userToken) throws GitAPIException {
    final CredentialsProvider credentialsProvider =
        jGitFactory.getNewCredentialsProvider(userToken, "");
    git.push().setCredentialsProvider(credentialsProvider).call();
  }

  void createNewRemoteRepo(final String repoName, final GithubUserModel userModel)
      throws IOException {
    final HttpPost request = new HttpPost(GITHUB_REPOS_ENDPOINT);
    // add request headers
    request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + userModel.getOAuthToken());
    request.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString());
    final Map<String, String> body = new HashMap<>();
    body.put("name", repoName);
    final String bodyAsJson = new ObjectMapper().writeValueAsString(body);
    final StringEntity requestEntity = new StringEntity(bodyAsJson);
    request.setEntity(requestEntity);
    try (final CloseableHttpClient httpClient = httpClientFactory.newCloseableHttpClient()) {
      try (final CloseableHttpResponse response = httpClient.execute(request)) {
        final int status = response.getStatusLine().getStatusCode();
        if (status == 400) {
          throw new IOException("Invalid user token");
        }
        if (status != 201) {
          throw new IOException("Error with status code: " + status);
        }
      }
    }
  }

  void createGitHubPagesWebsite(final GithubUserModel userModel)
          throws IOException {
    final String username = userModel.getUsername();
    final String pagesUrl = String.format("%s.github.io", username);
    final String endpoint = String.format("%s/%s/%s/pages", GITHUB_PAGES_ENDPOINT, username, pagesUrl);
    final HttpPost request = new HttpPost(endpoint);
    // add request headers
    request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + userModel.getOAuthToken());
    request.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString());
    final Map<String, Object> body = new HashMap<>();
    final Map<String, String> sourceBodyParam = new HashMap<>();
    sourceBodyParam.put("branch", "master");
    body.put("source", sourceBodyParam);
    final String bodyAsJson = new ObjectMapper().writeValueAsString(body);
    final StringEntity requestEntity = new StringEntity(bodyAsJson);
    request.setEntity(requestEntity);
    try (final CloseableHttpClient httpClient = httpClientFactory.newCloseableHttpClient()) {
      try (final CloseableHttpResponse response = httpClient.execute(request)) {
        final int status = response.getStatusLine().getStatusCode();
        if (status == 400) {
          throw new IOException("Invalid user token");
        }
        if (status != 201) {
          throw new IOException("Error with status code: " + status);
        }
      }
    }
  }
}
