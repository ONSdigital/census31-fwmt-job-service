package uk.gov.ons.census.fwmt.jobservice.messaging.pubsub;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.common.messaging.MessagingProperties;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction;
import uk.gov.ons.census.fwmt.jobservice.messaging.RmFieldMessagePublisher;

/**
 * Pub/Sub adapter placeholder — wired in Stage 2 when RM.Field lane migrates.
 */
@Service
@Slf4j
@ConditionalOnProperty(name = MessagingProperties.PROVIDER, havingValue = MessagingProperties.PROVIDER_PUBSUB)
public class PubSubRmFieldMessagePublisher implements RmFieldMessagePublisher {

  @Override
  public void publish(FwmtActionInstruction actionInstruction) {
    throw new UnsupportedOperationException(
        "Pub/Sub RM.Field publish is not implemented (Stage 2). Set app.messaging.provider=rabbit.");
  }

  @Override
  public void publish(FwmtCancelActionInstruction cancelActionInstruction) {
    throw new UnsupportedOperationException(
        "Pub/Sub RM.Field publish is not implemented (Stage 2). Set app.messaging.provider=rabbit.");
  }
}
