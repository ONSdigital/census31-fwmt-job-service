package uk.gov.ons.census.fwmt.jobservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.ons.census.fwmt.common.messaging.FieldWorkerInstructionJsonCodec;

@Configuration
public class JobServiceMessagingConfig {

  @Bean
  public FieldWorkerInstructionJsonCodec fieldWorkerInstructionJsonCodec() {
    return new FieldWorkerInstructionJsonCodec();
  }
}
