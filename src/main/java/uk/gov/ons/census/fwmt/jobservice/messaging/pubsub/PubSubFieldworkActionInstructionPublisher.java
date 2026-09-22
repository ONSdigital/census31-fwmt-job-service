package uk.gov.ons.census.fwmt.jobservice.messaging.pubsub;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction;
import uk.gov.ons.census.fwmt.jobservice.messaging.FieldworkActionInstructionPublisher;

@Service
@Slf4j
@RequiredArgsConstructor
public class PubSubFieldworkActionInstructionPublisher implements FieldworkActionInstructionPublisher {

  private static final String EVENT_TYPE = "FIELDWORK_ACTION_INSTRUCTION";
  private static final String SCHEMA_VERSION = "1.0";

  private final PubSubTemplate pubSubTemplate;
  private final ObjectMapper objectMapper;

  @Value("${app.messaging.destinations.fieldworkActionInstructionInternal:event_fieldwork_action-instruction_internal}")
  private String fieldworkActionInstructionInternalTopic;

  @Override
  public void publish(FwmtActionInstruction actionInstruction) {
    publishPayload(actionInstruction);
  }

  @Override
  public void publish(FwmtCancelActionInstruction cancelActionInstruction) {
    publishPayload(cancelActionInstruction);
  }

  private void publishPayload(Object payload) {
    PubsubMessage message = PubsubMessage.newBuilder()
        .setData(ByteString.copyFromUtf8(serialize(payload)))
        .putAllAttributes(buildAttributes(caseIdOf(payload)))
        .build();
    log.debug("Publishing fieldwork action instruction to topic {}", fieldworkActionInstructionInternalTopic);
    pubSubTemplate.publish(fieldworkActionInstructionInternalTopic, message);
  }

  private String serialize(Object payload) {
    try {
      return objectMapper.writeValueAsString(payload);
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Failed to encode fieldwork action instruction", exception);
    }
  }

  private Map<String, String> buildAttributes(String caseId) {
    Map<String, String> attributes = new LinkedHashMap<>();
    attributes.put("eventId", UUID.randomUUID().toString());
    attributes.put("correlationId", "");
    attributes.put("caseId", caseId == null ? "" : caseId);
    attributes.put("eventType", EVENT_TYPE);
    attributes.put("schemaVersion", SCHEMA_VERSION);
    attributes.put("occurredAt", Instant.now().toString());
    return attributes;
  }

  private String caseIdOf(Object payload) {
    if (payload instanceof FwmtActionInstruction actionInstruction) {
      return actionInstruction.getCaseId();
    }
    if (payload instanceof FwmtCancelActionInstruction cancelActionInstruction) {
      return cancelActionInstruction.getCaseId();
    }
    throw new IllegalArgumentException("Unsupported fieldwork action instruction payload: " + payload.getClass());
  }
}