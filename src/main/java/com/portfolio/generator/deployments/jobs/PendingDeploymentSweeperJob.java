package com.portfolio.generator.deployments.jobs;

import com.mongodb.MongoException;
import com.portfolio.generator.deployments.status.IPendingDeploymentDeployActionProcessor;
import com.portfolio.generator.deployments.status.StatusPollerResult;
import com.portfolio.generator.managers.GithubPortfolioGenerationTaskManager;
import com.portfolio.generator.models.ResumeModel;
import com.portfolio.generator.models.databaseModels.DbDeploymentModel;
import com.portfolio.generator.models.databaseModels.DbPendingDeploymentModel;
import com.portfolio.generator.models.databaseModels.DeploymentStatus;
import com.portfolio.generator.models.staticsite.DeploymentProvider;
import com.portfolio.generator.services.UserService;
import com.portfolio.generator.services.database.IDatabaseService;
import com.portfolio.generator.utilities.factories.IDateTimeFactory;
import com.portfolio.generator.utilities.helpers.DeploymentStatusType;
import com.portfolio.generator.utilities.helpers.IDeploymentStatusHelper;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Main entry point for scheduling pending deployments. Goes through the pendingDeployments tables and attempts to
 * schedule deployment for Github/AWS with their respective managers. Tasks are submitted to Github/AWS and a
 * future is returned which is used to determine task completeness. When a task is completed its corresponding
 * DB entry is removed, if a task fails or is cancelled nothing is done, allowing the task to be retried (unless
 * we have hit the max retries allowed).
 **/
@Component
public class PendingDeploymentSweeperJob implements IJob {
  private static final Logger logger = LoggerFactory.getLogger(PendingDeploymentSweeperJob.class);
  private static final int MAX_RETRIES = 3;
  private static final ZoneId TIMEZONE_ID = ZoneId.of("Z");
  private final Set<Task> tasks;
  private final GithubPortfolioGenerationTaskManager githubDeploymentTaskManager;
  private final IDatabaseService databaseService;
  private final UserService userService;
  private final IDateTimeFactory dateTimeFactory;
  private final IDeploymentStatusHelper deploymentStatusHelper;
  private final IPendingDeploymentDeployActionProcessor deploymentStatusPollerScheduler;
  @Value("${DEPLOYMENT_URL_PREFIX}")
  private String deploymentUrlPrefix;


  public PendingDeploymentSweeperJob(
      final GithubPortfolioGenerationTaskManager githubDeploymentTaskManager,
      final IDatabaseService databaseService,
      final UserService userService,
      final IDateTimeFactory dateTimeFactory,
      final IDeploymentStatusHelper deploymentStatusHelper,
      final IPendingDeploymentDeployActionProcessor deploymentStatusPollerScheduler) {
    this.githubDeploymentTaskManager = githubDeploymentTaskManager;
    this.databaseService = databaseService;
    this.userService = userService;
    this.dateTimeFactory = dateTimeFactory;
    this.deploymentStatusHelper = deploymentStatusHelper;
    this.deploymentStatusPollerScheduler = deploymentStatusPollerScheduler;
    tasks = new HashSet<>();
  }


  /**
   * Loop through the deployments in the table and schedule retries for failed deployments up
   * to a max of MAX_RETRIES retries.
   * Runs every 1 minutes
   **/
  @Scheduled(fixedRate = 5000)
  public void run() {
    try {
      processTaskList();
      final List<DbDeploymentModel> deployments = getPendingDeployments();
      processPendingDeployments(deployments);
    } catch (final MongoException e) {
      logger.error("Error fetching deployments from database", e);
    }
  }

  private void processPendingDeployments(final List<DbDeploymentModel> deployments) {
    final StringBuilder stringBuilder = new StringBuilder();
    for (final DbDeploymentModel deployment : deployments) {
      final Summary.Builder summaryBuilder = new Summary.Builder();
      final String deploymentId = deployment.get_id();
      final DeploymentStatus deploymentStatus = deployment.getStatus();
      final Integer retryCount = deployment.getRetryCount();
      summaryBuilder.withDeploymentId(deploymentId);
      final NextDeploymentAction nextDeploymentAction = getNextActionForDeployment(deployment);
      if (nextDeploymentAction == NextDeploymentAction.DEPLOY) {
        schedulePortfolioGeneration(deployment);
        summaryBuilder.withActionTaken("DEPLOY");
        summaryBuilder.withDeploymentStatus(DeploymentStatus.PENDING_RETRY);
        summaryBuilder.withRetryCount(retryCount + 1);
      } else if (nextDeploymentAction == NextDeploymentAction.CANCEL) {
        cancelPendingDeployment(deploymentId);
        summaryBuilder.withActionTaken("CANCEL");
        summaryBuilder.withDeploymentStatus(DeploymentStatus.CANCELLED);
        summaryBuilder.withRetryCount(retryCount);
      } else if (nextDeploymentAction == NextDeploymentAction.FAIL) {
        failPendingDeployment(deploymentId);
        summaryBuilder.withActionTaken("FAILED");
        summaryBuilder.withDeploymentStatus(DeploymentStatus.FAILED);
        summaryBuilder.withRetryCount(retryCount);
      } else if (nextDeploymentAction == NextDeploymentAction.CHECK_STATUS_EXTERNAL) {
        checkDeploymentStatusExternally(deployment);
        summaryBuilder.withActionTaken("CHECK_STATUS_EXTERNAL");
        summaryBuilder.withDeploymentStatus(deploymentStatus);
        summaryBuilder.withRetryCount(retryCount);
      } else {
        summaryBuilder.withActionTaken("NONE");
        summaryBuilder.withDeploymentStatus(deploymentStatus);
        summaryBuilder.withRetryCount(retryCount);
      }
      updateSummary(stringBuilder, summaryBuilder.build());
    }
    logger.info("Processed deployments, summary of current deployments: \n");
    logger.info(stringBuilder.toString());
  }

  private void schedulePortfolioGeneration(final DbDeploymentModel deployment
  ) {
    try {
      final PendingDeploymentSweeperAction request = createDeploymentBuildRequest(deployment);
      final Future<PendingDeploymentSweeperAction> buildPhaseFuture;
        if (Objects.requireNonNull(request.deploymentProvider) == DeploymentProvider.GITHUB) {
            buildPhaseFuture = githubDeploymentTaskManager.schedulePortfolioGenerationTask(request);
        } else {
            logger.error(String.format("DbDeploymentModel %s has unknown deployment provider: %s", deployment, request.deploymentProvider));
            throw new RuntimeException("Unable to schedule deployment task for unknown deployment provider");
        }
      databaseService.updateDeploymentRetryCount(deployment.get_id(), deployment.getRetryCount() + 1);
      final Task task = new Task.Builder()
          .withDeploymentSweeperAction(request)
          .withBuildPhaseFuture(buildPhaseFuture)
          .withSubmittedTime(dateTimeFactory.getLocalDateTimeNow(TIMEZONE_ID))
          .withDeploymentPhase(DeploymentPhase.BUILD)
          .build();
      tasks.add(task);
    } catch (final IOException | MongoException e) {
      logger.error("Failed to schedule portfolio generation task for deploymentId: " +
          deployment.get_id(), e);
      databaseService.updateDeploymentRetryCount(deployment.get_id(), deployment.getRetryCount() + 1);
    }
  }

  private void updateSummary(final StringBuilder stringBuilder, final Summary summary) {
    stringBuilder.append(" Action Taken: ").append(summary.actionTaken);
    stringBuilder.append(" DeploymentId: ").append(summary.deploymentId);
    stringBuilder.append(" DeploymentStatus: ").append(summary.deploymentStatus);
    stringBuilder.append(" retryCount: ").append(summary.retryCount);
    stringBuilder.append("\n");
  }

  private PendingDeploymentSweeperAction createDeploymentBuildRequest(
      final DbDeploymentModel deployment
  ) throws IOException, MongoException {
    final String userId = deployment.getUserId();
    Validate.notBlank(userId);
    final ResumeModel resumeModel =
        databaseService.getResume(deployment.getResumeId()).toResumeModel();
    final DeploymentProvider deploymentProvider = DeploymentProvider.getDeploymentProviderFromString(deployment.getDeploymentProvider());
    final PendingDeploymentSweeperAction.Builder deploymentRequest = new PendingDeploymentSweeperAction.Builder()
        .setResume(resumeModel)
        .setDeploymentId(deployment.get_id())
        .setPendingDeploymentId(deployment.get_id())
        .setUserId(deployment.getUserId())
        .setGitHubUserName(deployment.getGithubUserName())
        .setWebsiteDetails(deployment.getWebsiteDetails().toWebsiteDetailsModel())
        .setDeploymentProvider(deploymentProvider);
    if (deploymentProvider == DeploymentProvider.GITHUB) {
      final String oAuthToken = userService.getUserOAuthToken(userId);
      Validate.notBlank(oAuthToken);
      Validate.notBlank(deploymentRequest.gitHubUserName);
      deploymentRequest.setoAuthToken(oAuthToken);
    }
    return deploymentRequest.build();
  }

  private PendingDeploymentSweeperAction createCheckStatusExternalRequest(
      final DbDeploymentModel deployment
  ) throws IOException, MongoException {
    final String userId = deployment.getUserId();
    Validate.notBlank(userId);
    Validate.notNull(deployment.getDeploymentProvider());
    final PendingDeploymentSweeperAction.Builder deploymentRequest = new PendingDeploymentSweeperAction.Builder()
        .setDeploymentId(deployment.get_id())
        .setPendingDeploymentId(deployment.get_id())
        .setInvalidationId(deployment.getCloudFrontInvalidationId())
        .setDeploymentPhase(DeploymentPhase.DEPLOY)
        .setDeploymentProvider(DeploymentProvider.getDeploymentProviderFromString(deployment.getDeploymentProvider()));
    if (deployment.getDeploymentProvider().equalsIgnoreCase("github")) {
      final String oAuthToken = userService.getUserOAuthToken(userId);
      final String gitHubUserName = userService.getUserGitHubUserName(userId);
      Validate.notBlank(oAuthToken);
      Validate.notBlank(gitHubUserName);
      deploymentRequest.setoAuthToken(oAuthToken);
      deploymentRequest.setGitHubUserName(gitHubUserName);
    }
    return deploymentRequest.build();
  }


  private NextDeploymentAction getNextActionForDeployment(final DbDeploymentModel deployment) {
    final String deploymentId = deployment.get_id();
    final DeploymentStatus deploymentStatus = deployment.getStatus();
    final boolean taskAlreadySubmitted = taskAlreadySubmitted(deploymentId);
    if (taskAlreadySubmitted) {
      return NextDeploymentAction.NOTHING;
    }
    if (deploymentStatus == DeploymentStatus.SENT_TO_AWS
        || deploymentStatus == DeploymentStatus.SENT_TO_GITHUB) {
      return NextDeploymentAction.CHECK_STATUS_EXTERNAL;
    }
    if (deployment.getRetryCount() >= MAX_RETRIES) {
      logger.info(String.format("Deployment %s reached max retries, failing deployment", deploymentId));
      return NextDeploymentAction.FAIL;
    }
    //something bad happened if this is ever true
    if (deploymentStatus == DeploymentStatus.PROCESSING) {
      logger.info(String.format("Deployment %s is marked processing but no task exists, unable to proceed", deploymentId));
      return NextDeploymentAction.FAIL;
    }
    if (shouldCancelDeployment(deployment)) {
      logger.info(String.format("Deployment %s has cancellation requested, cancelling", deploymentId));
      return NextDeploymentAction.CANCEL;
    }
    if (shouldScheduleDeploymentForGeneration(deploymentStatus)) {
      return NextDeploymentAction.DEPLOY;
    }
    return NextDeploymentAction.NOTHING;
  }

  /**
   * Attempts to cancel a deployment
   **/
  private boolean cancelDeployment(final Task task) {
    final Future<?> future = getFutureForTask(task);
    final boolean success =
        updateDeploymentStatus(DeploymentStatusType.PENDING_RETRY, 0L, task.deploymentSweeperAction.deploymentId);
    return future.cancel(true) && success;
  }

  /**
   * Attempt to update the deployment status in the Db.
   *
   * @return true if we successfully updated the deployment staus, false otherwise
   **/
  private boolean updateDeploymentStatus(final DeploymentStatusType deploymentStatusType,
                                         final long progress,
                                         final String deploymentId) {
    try {
      deploymentStatusHelper.updateDeploymentProgress(
          new com.portfolio.generator.utilities.helpers.DeploymentStatus(deploymentStatusType, progress, deploymentId
          ));
      return true;
    } catch (final IOException e) {
      logger.error("Failed to update deployment status for deployment: " + deploymentId, e);
      return false;
    }
  }

  /**
   * Attempt to update the deployment status in the Db.
   *
   * @return true if we successfully updated the deployment staus, false otherwise
   **/
  private void failPendingDeployment(final String deploymentId) {
    try {
      databaseService.deletePendingDeployment(deploymentId);
      deploymentStatusHelper.updateDeploymentProgress(
          new com.portfolio.generator.utilities.helpers.DeploymentStatus(DeploymentStatusType.FAILED, 0L, deploymentId
          ));
    } catch (final IOException e) {
      logger.error("Exception failing deployment: " + deploymentId, e);
    }
  }

  /**
   * Attempt to update the deployment status in the Db.
   *
   * @return true if we successfully updated the deployment staus, false otherwise
   **/
  private void cancelPendingDeployment(final String deploymentId) {
    try {
      databaseService.deletePendingDeployment(deploymentId);
      deploymentStatusHelper.updateDeploymentProgress(
          new com.portfolio.generator.utilities.helpers.DeploymentStatus(DeploymentStatusType.CANCELLED, 0L, deploymentId
          ));
    } catch (final IOException e) {
      logger.error("Exception cancelling deployment: " + deploymentId, e);
    }
  }

  /**
   * Attempt to update the deployment status in the Db.
   *
   * @return true if we successfully updated the deployment staus, false otherwise
   **/
  private void checkDeploymentStatusExternally(final DbDeploymentModel deployment) {
    try {
      final PendingDeploymentSweeperAction deployAction = createCheckStatusExternalRequest(deployment);
      final Future<StatusPollerResult> future =
          deploymentStatusPollerScheduler.pollDeploymentStatusUntilFinished(deployAction);
      final Task checkDeploymentStatusTask = new Task.Builder()
          .withDeploymentSweeperAction(deployAction)
          .withSubmittedTime(dateTimeFactory.getLocalDateTimeNow(TIMEZONE_ID))
          .withDeploymentPhase(DeploymentPhase.DEPLOY)
          .withDeployPhaseFuture(future)
          .build();
      tasks.add(checkDeploymentStatusTask);
    } catch (final IOException e) {
      logger.error(String.format("Failed to check status externally for deploymentId %s", deployment.get_id()), e);
      databaseService.updateDeploymentRetryCount(deployment.get_id(), deployment.getRetryCount() + 1);
    }

  }

  /**
   * Goes through the submitted tasks list and performs any required any actions such as cancelling a task
   * or removing a pending deployment that has completed. Runs before we check the DB for any pending tasks.
   * <p>
   * If we fail to update the deployment status, leave the task in the list, we will try again later.
   **/
  private void processTaskList() {
    final Iterator<Task> taskIterator = tasks.iterator();
    while (taskIterator.hasNext()) {
      final Task task = taskIterator.next();
      final Future<?> future = getFutureForTask(task);
      final boolean taskRunningTooLong = hasTaskBeenRunningTooLong(task);
      if (future.isCancelled()) {
        handleCancelledTask(taskIterator, task);
        continue;
      }
      if (!future.isDone() && taskRunningTooLong) {
        handleTaskRunningTooLong(taskIterator, task);
        continue;
      }
      if (!future.isDone()) {
        continue;
      }
      if (task.deploymentPhase == DeploymentPhase.BUILD) {
        handleBuildPhaseComplete(taskIterator, task);
      } else if (task.deploymentPhase == DeploymentPhase.DEPLOY) {
        handleDeployPhaseCompleted(taskIterator, task);
      } else {
        throw new IllegalArgumentException(String.format("Unknown deployment phase %s", task.deploymentPhase));
      }

    }
  }

  /**
   * Complete the build phase of the task,
   **/
  private void handleBuildPhaseComplete(final Iterator<Task> taskIterator, final Task task) {
    final String deploymentId = task.deploymentSweeperAction.deploymentId;
    final PendingDeploymentSweeperAction deploymentRequest = task.deploymentSweeperAction;
    try {
      final Future<PendingDeploymentSweeperAction> future = task.buildPhaseFuture;
      // this will throw an exception if the task did not complete successfully
      future.get();
    } catch (final InterruptedException | ExecutionException e) {
      // if we get here it means the generation failed and we should mark it to be retried
      logger.error(String.format("Task for deploymentId %s failed exceptionally", deploymentId), e);
      final boolean updateStatusSuccessfully =
          updateDeploymentStatus(DeploymentStatusType.PENDING_RETRY, 0L, deploymentId);
      // if we fail to update the status, don't delete the task. Let us try again next time.
      if (updateStatusSuccessfully) {
        taskIterator.remove();
      }
      return;
    }
    try {
      completePortfolioGenerationBuildPhase(deploymentRequest);
      taskIterator.remove();
    } catch (final IOException | MongoException ex) {
      logger.warn(String.format("Failed to mark deployment %s as complete", deploymentId), ex);
    }
  }

  /**
   * Complete the deploy phase of the task,
   **/
  private void handleDeployPhaseCompleted(final Iterator<Task> taskIterator, final Task task) {
    final String deploymentId = task.deploymentSweeperAction.deploymentId;
    final StatusPollerResult statusPollerResult;
    try {
      final Future<StatusPollerResult> future = task.deployPhaseFuture;
      // this will throw an exception if the task did not complete successfully
      statusPollerResult = future.get();
    } catch (final InterruptedException | ExecutionException e) {
      // if we get here it means the generation failed and we should mark it to be retried
      logger.error(String.format("Task for deploymentId %s failed exceptionally", deploymentId), e);
      //CHANGE ME!
      final boolean updateStatusSuccessfully =
          updateDeploymentStatus(DeploymentStatusType.PENDING_RETRY, 0L, deploymentId);
      //if we fail to update the status, don't delete the task. Let us try again next time.
      if (updateStatusSuccessfully) {
        taskIterator.remove();
      }
      return;
    }
    try {
      completePortfolioGenerationDeployPhase(statusPollerResult);
      taskIterator.remove();
    } catch (final IOException | MongoException ex) {
      logger.warn(String.format("Failed to mark deployment %s as complete", deploymentId), ex);
    }
  }

  /**
   * Handle a cancelled task, we should update the status indicating we can retry the task, and then
   * remove it from the task list.
   **/
  private void handleCancelledTask(final Iterator<Task> taskIterator, final Task task) {
    final String deploymentId = task.deploymentSweeperAction.deploymentId;
    final boolean success = updateDeploymentStatus(DeploymentStatusType.PENDING_RETRY, 0L, deploymentId);
    if (success) {
      taskIterator.remove();
    }
  }

  /**
   * Handle a task that is running for too long, we need to attempt to cancel the task, then remove it from the task
   * list. If the cancellation fails, don't do anything, next time we retry we notice the cancelled status and will
   * try to update it again.
   **/
  private void handleTaskRunningTooLong(final Iterator<Task> taskIterator, final Task task) {
    logger.warn(String.format("Task %s running too long, start at %s, attempting to cancel task",
        task,
        task.submittedTime));
    final boolean cancelSuccessful = cancelDeployment(task);
    if (cancelSuccessful) {
      taskIterator.remove();
    }
  }

  private boolean taskAlreadySubmitted(final String deploymentId) {
    final PendingDeploymentSweeperAction request = new PendingDeploymentSweeperAction.Builder()
        .setDeploymentId(deploymentId)
        .build();
    final Task task = new Task.Builder()
        .withDeploymentSweeperAction(request)
        .build();
    return tasks.contains(task);
  }

  private boolean hasTaskBeenRunningTooLong(final Task task) {
    final LocalDateTime submittedTime = task.submittedTime;
    final LocalDateTime now = dateTimeFactory.getLocalDateTimeNow(TIMEZONE_ID);
    return now.minus(Duration.ofMinutes(10)).isAfter(submittedTime);
  }


  /**
   * Complete the build phase of the portfolio generation task, updating the deployed URL for AWS requests, doing
   * nothing for GitHub.
   **/
  private void completePortfolioGenerationBuildPhase(final PendingDeploymentSweeperAction deploymentRequest)
      throws MongoException, IOException {
    Validate.notNull(deploymentRequest, "deploymentRequest was null");
    Validate.notNull(deploymentRequest.deploymentProvider, "deploymentProvider was null");
    Validate.notBlank(deploymentRequest.deploymentId, "deploymentId was null");
  }

  /**
   * Complete deployment phase.
   **/
  private void completePortfolioGenerationDeployPhase(final StatusPollerResult statusPollerResult)
      throws MongoException, IOException {
    Validate.notNull(statusPollerResult, "deploymentRequest was null");
    final String deploymentId = statusPollerResult.deploymentId;
    final DeploymentProvider deploymentProvider = statusPollerResult.deploymentProvider;
    Validate.notNull(deploymentProvider, "deploymentProvider was null");
    Validate.notBlank(deploymentId, "deploymentId was null or empty");
    if (deploymentProvider == DeploymentProvider.GITHUB) {
      final String deployedUrl = statusPollerResult.deployedUrl;
      Validate.notNull(deployedUrl, "deployedUrl was null or empty");
      databaseService.updateDeploymentUrl(deploymentId, deployedUrl);
    }
    deploymentStatusHelper.updateDeploymentProgress(
        new com.portfolio.generator.utilities.helpers.DeploymentStatus(DeploymentStatusType.SUCCESSFUL, 100L, deploymentId
        ));
    databaseService.deletePendingDeployment(deploymentId);
  }

  private boolean shouldCancelDeployment(final DbDeploymentModel deployment) {
    final Boolean cancellationRequested = deployment.getCancellationRequested();
    final DeploymentStatus deploymentStatus = deployment.getStatus();
    return Boolean.TRUE.equals(cancellationRequested) &&
        (
            deploymentStatus == DeploymentStatus.PENDING ||
                deploymentStatus == DeploymentStatus.PENDING_RETRY
        );
  }


  private boolean shouldScheduleDeploymentForGeneration(final DeploymentStatus deploymentStatus) {
    return deploymentStatus == null ||
        deploymentStatus == DeploymentStatus.PENDING_RETRY ||
        deploymentStatus == DeploymentStatus.PENDING ||
        deploymentStatus == DeploymentStatus.FAILED;
  }

  private List<DbDeploymentModel> getPendingDeployments() {
    final List<DbPendingDeploymentModel> pendingDeployments = databaseService.getPendingDeployments();
    return databaseService.getDeployments(pendingDeployments.stream()
        .map(DbPendingDeploymentModel::get_id).collect(Collectors.toList()));
  }

  private Future<?> getFutureForTask(final Task task) {
    Validate.notNull(task);
    final DeploymentPhase deploymentPhase = task.deploymentPhase;
    Validate.notNull(deploymentPhase);
    if (deploymentPhase == DeploymentPhase.DEPLOY) {
      return task.deployPhaseFuture;
    } else if (deploymentPhase == DeploymentPhase.BUILD) {
      return task.buildPhaseFuture;
    } else {
      throw new IllegalArgumentException(String.format("Unknown deployment phase %s", deploymentPhase));
    }
  }

  private static class Task {
    private final Future<PendingDeploymentSweeperAction> buildPhaseFuture;
    private final Future<StatusPollerResult> deployPhaseFuture;
    private final LocalDateTime submittedTime;
    private final PendingDeploymentSweeperAction deploymentSweeperAction;
    private final DeploymentPhase deploymentPhase;

    private Task(final Builder builder) {
      this.buildPhaseFuture = builder.buildPhaseFuture;
      this.deployPhaseFuture = builder.deployPhaseFuture;
      this.submittedTime = builder.submittedTime;
      this.deploymentSweeperAction = builder.deploymentSweeperAction;
      this.deploymentPhase = builder.deploymentPhase;
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      final Task task = (Task) o;
      return Objects.equals(deploymentSweeperAction.deploymentId, task.deploymentSweeperAction.deploymentId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(deploymentSweeperAction.deploymentId);
    }

    @Override
    public String toString() {
      return ReflectionToStringBuilder.toString(this);
    }

    private static class Builder {
      Future<PendingDeploymentSweeperAction> buildPhaseFuture;
      Future<StatusPollerResult> deployPhaseFuture;
      LocalDateTime submittedTime;
      PendingDeploymentSweeperAction deploymentSweeperAction;
      DeploymentPhase deploymentPhase;

      public Builder withSubmittedTime(final LocalDateTime submittedTime) {
        this.submittedTime = submittedTime;
        return this;
      }

      public Builder withBuildPhaseFuture(final Future<PendingDeploymentSweeperAction> buildPhaseFuture) {
        this.buildPhaseFuture = buildPhaseFuture;
        return this;
      }

      public Builder withDeployPhaseFuture(final Future<StatusPollerResult> deployPhaseFuture) {
        this.deployPhaseFuture = deployPhaseFuture;
        return this;
      }

      public Builder withDeploymentSweeperAction(final PendingDeploymentSweeperAction deploymentSweeperAction) {
        this.deploymentSweeperAction = deploymentSweeperAction;
        return this;
      }

      public Builder withDeploymentPhase(final DeploymentPhase deploymentPhase) {
        this.deploymentPhase = deploymentPhase;
        return this;
      }

      public Task build() {
        return new Task(this);
      }
    }
  }

  private static class Summary {
    public final String deploymentId;
    public final int retryCount;
    public final DeploymentStatus deploymentStatus;
    public final String actionTaken;

    private Summary(final Builder builder) {
      this.deploymentId = builder.deploymentId;
      this.retryCount = builder.retryCount;
      this.deploymentStatus = builder.deploymentStatus;
      this.actionTaken = builder.actionTaken;
    }

    private static class Builder {
      private String deploymentId;
      private int retryCount;
      private DeploymentStatus deploymentStatus;
      private String actionTaken;

      public Builder withDeploymentId(final String deploymentId) {
        this.deploymentId = deploymentId;
        return this;
      }

      public Builder withRetryCount(final int retryCount) {
        this.retryCount = retryCount;
        return this;
      }

      public Builder withDeploymentStatus(final DeploymentStatus deploymentStatus) {
        this.deploymentStatus = deploymentStatus;
        return this;
      }

      public Builder withActionTaken(final String actionTaken) {
        this.actionTaken = actionTaken;
        return this;
      }

      public Summary build() {
        return new Summary(this);
      }
    }

  }
}
