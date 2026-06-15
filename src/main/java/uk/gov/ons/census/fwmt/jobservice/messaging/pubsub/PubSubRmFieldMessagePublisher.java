package uk.gov.ons.census.fwmt.jobservice.messaging.pubsub;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.pubsub.v1.PubsubMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.common.messaging.FieldWorkerInstructionJsonCodec;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction;
import uk.gov.ons.census.fwmt.jobservice.messaging.RmFieldMessagePublisher;

@Service
@Slf4j
@RequiredArgsConstructor
public class PubSubRmFieldMessagePublisher implements RmFieldMessagePublisher {

  private final PubSubTemplate pubSubTemplate;
  private final FieldWorkerInstructionJsonCodec codec;

  @Value("${app.messaging.destinations.rmField:RM.Field}")
  private String rmFieldTopic;

  @Override
  public void publish(FwmtActionInstruction actionInstruction) {
    publishPayload(actionInstruction);
  }

  @Override
  public void publish(FwmtCancelActionInstruction cancelActionInstruction) {
    publishPayload(cancelActionInstruction);
  }

  private void publishPayload(Object payload) {
    PubsubMessage message = codec.toPubsubMessage(payload, true);
    log.debug("Publishing field worker instruction to topic {}", rmFieldTopic);
    pubSubTemplate.publish(rmFieldTopic, message);
  }
}
