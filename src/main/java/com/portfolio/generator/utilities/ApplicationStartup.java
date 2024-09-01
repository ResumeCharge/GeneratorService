package com.portfolio.generator.utilities;

import com.portfolio.generator.services.IDeploymentService;
import com.portfolio.generator.services.IUserService;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Minutes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class ApplicationStartup
        implements ApplicationListener<ApplicationReadyEvent> {
  private static final Logger logger = LoggerFactory.getLogger(ApplicationStartup.class);
  private static final Minutes MAX_INIT_WAIT_TIME = Minutes.minutes(1);

  @Value("${HEALTH_CHECK_ON_STARTUP:false}")
  private String HEALTH_CHECK_ON_STARTUP;

  private final IUserService userService;
  private final IDeploymentService deploymentService;

  public ApplicationStartup(final IUserService userService, final IDeploymentService deploymentService) {
    this.userService = userService;
    this.deploymentService = deploymentService;
  }

  /**
   * This event is executed as late as conceivably possible to indicate that
   * the application is ready to service requests.
   */
  @Override
  public void onApplicationEvent(final ApplicationReadyEvent event) {
    if (!Boolean.parseBoolean(HEALTH_CHECK_ON_STARTUP)) {
      return;
    }
    logger.info("Beginning health check of user-service and deployment-service");
    final DateTime startTime = new DateTime();
    DateTime now = new DateTime();
    while (Minutes.minutesBetween(startTime, now).isLessThan(MAX_INIT_WAIT_TIME)) {
      final boolean isUserServiceHealthy = this.userService.healthCheckUserService();
      final boolean isDeploymentServiceHealthy = this.deploymentService.healthCheckDeploymentService();
      if (isUserServiceHealthy && isDeploymentServiceHealthy) {
        logger.info("user-service and deployment-service both health, health checks complete");
        return;
      }
      logger.warn(String.format("Health checks failed, service status: { user-service-healthy: %s, deployment-service-healthy: %s }", isUserServiceHealthy, isDeploymentServiceHealthy));
      try {
        Thread.sleep(Duration.standardSeconds(10).getMillis());
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      now = new DateTime();
    }
    logger.error("user-service and deployment-service did not become healthy in time, exiting");
    System.exit(1);
  }
}
