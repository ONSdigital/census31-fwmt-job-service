package uk.gov.ons.census.fwmt.jobservice.config;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.NoCredentialsProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Ensure Spring Cloud GCP Pub/Sub runs against emulator
 * without requiring Application Default Credentials.
 */
@Configuration
public class JobServicePubSubEmulatorConfig {

  @Bean
  @Primary
  @ConditionalOnProperty(name = "spring.cloud.gcp.pubsub.emulator-host")
  public CredentialsProvider googleCredentials() {
    return NoCredentialsProvider.create();
  }
}
