package uk.gov.ons.census.fwmt.jobservice.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.pubsub.v1.PubsubMessage;
import java.io.IOException;
import java.time.Instant;
import lombok.Value;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction;

@Component
public class RmAdapterActionInstructionDecoder {

  private final ObjectMapper objectMapper;

  public RmAdapterActionInstructionDecoder(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public DecodedMessage decode(PubsubMessage message) {
    return decode(message, ActionInstructionContract.EXTERNAL_RM_ADAPTER);
  }

  public DecodedMessage decode(PubsubMessage message, ActionInstructionContract contract) {
    String json = message.getData().toStringUtf8();
    try {
      var payload = objectMapper.readTree(json);
      ActionInstructionType action = payload.path("actionInstruction").isTextual()
          ? ActionInstructionType.valueOf(payload.path("actionInstruction").textValue())
          : null;
      if (action == null) {
        throw new IllegalArgumentException("Missing actionInstruction in RM adapter payload");
      }
      String surveyName = payload.path("surveyName").textValue();
      boolean validSurvey = "CENSUS".equals(surveyName)
          || contract == ActionInstructionContract.INTERNAL_FWMT && "FEEDBACK".equals(surveyName);
      if (!validSurvey) {
        throw new IllegalArgumentException("Action instruction payload has an invalid surveyName for " + contract);
      }

      Object instruction = action == ActionInstructionType.CANCEL
          ? objectMapper.readValue(json, FwmtCancelActionInstruction.class)
          : objectMapper.readValue(json, FwmtActionInstruction.class);
      String payloadCaseId = action == ActionInstructionType.CANCEL
          ? ((FwmtCancelActionInstruction) instruction).getCaseId()
          : ((FwmtActionInstruction) instruction).getCaseId();
      String attributeCaseId = message.getAttributesOrDefault("caseId", "");
      if (!attributeCaseId.isEmpty() && !attributeCaseId.equals(payloadCaseId)) {
        throw new IllegalArgumentException("RM adapter payload caseId does not match Pub/Sub attribute");
      }

      return new DecodedMessage(instruction, messageTime(message), metadata(message));
    } catch (IllegalArgumentException exception) {
      throw exception;
    } catch (IOException exception) {
      throw new IllegalArgumentException("Failed to decode RM adapter action instruction", exception);
    }
  }

  private Instant messageTime(PubsubMessage message) {
    String occurredAt = message.getAttributesOrDefault("occurredAt", "");
    if (occurredAt.isEmpty()) {
      return Instant.now();
    }
    try {
      return Instant.parse(occurredAt);
    } catch (RuntimeException exception) {
      throw new IllegalArgumentException("Invalid occurredAt attribute", exception);
    }
  }

  private MessageMetadata metadata(PubsubMessage message) {
    return new MessageMetadata(
        message.getAttributesOrDefault("eventId", ""),
        message.getAttributesOrDefault("correlationId", ""),
        message.getAttributesOrDefault("eventType", ""),
        message.getAttributesOrDefault("schemaVersion", ""),
        message.getAttributesOrDefault("occurredAt", ""));
  }

  @Value
  public static class DecodedMessage {
    Object instruction;
    Instant messageTime;
    MessageMetadata metadata;
  }

  @Value
  public static class MessageMetadata {
    String eventId;
    String correlationId;
    String eventType;
    String schemaVersion;
    String occurredAt;
  }
}
