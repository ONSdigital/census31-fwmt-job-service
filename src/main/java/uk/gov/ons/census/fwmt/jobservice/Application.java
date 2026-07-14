package uk.gov.ons.census.fwmt.jobservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {
    "uk.gov.ons.census.fwmt.jobservice",
    "uk.gov.ons.census.fwmt.events",
    "uk.gov.census.ffa.storage.utils"
})
@EnableIntegration
@EnableRetry
@EnableScheduling
@EnableJpaRepositories("uk.gov.ons.census.fwmt.jobservice.repository")
public class Application {

  public static final String APPLICATION_NAME = "FWMT Gateway Job Service";

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
