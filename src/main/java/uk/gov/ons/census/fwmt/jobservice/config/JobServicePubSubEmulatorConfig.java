package uk.gov.ons.census.fwmt.jobservice.config;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.NoCredentialsProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import uk.gov.ons.census.fwmt.common.messaging.MessagingProperties;

@Configuration
@ConditionalOnProperty(name = MessagingProperties.PROVIDER, havingValue = MessagingProperties.PROVIDER_PUBSUB)
public class JobServicePubSubEmulatorConfig {

  @Bean
  @Primary
  public CredentialsProvider googleCredentials() {
    return NoCredentialsProvider.create();
  }
}
