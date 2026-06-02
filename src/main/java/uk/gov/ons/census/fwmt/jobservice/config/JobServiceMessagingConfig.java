package uk.gov.ons.census.fwmt.jobservice.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.ons.census.fwmt.common.messaging.FieldWorkerInstructionJsonCodec;
import uk.gov.ons.census.fwmt.common.messaging.MessagingProperties;

@Configuration
@ConditionalOnProperty(name = MessagingProperties.PROVIDER, havingValue = MessagingProperties.PROVIDER_PUBSUB)
public class JobServiceMessagingConfig {

  @Bean
  public FieldWorkerInstructionJsonCodec fieldWorkerInstructionJsonCodec() {
    return new FieldWorkerInstructionJsonCodec();
  }
}
