package uk.gov.ons.census.fwmt.jobservice.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction;

class RmAdapterActionInstructionDecoderTest {

  private RmAdapterActionInstructionDecoder decoder;

  @BeforeEach
  void setUp() {
    decoder = new RmAdapterActionInstructionDecoder(new ObjectMapper());
  }

  @Test
  void decodesCreatePayloadAndMetadata() {
    PubsubMessage message = message(
        "{\"actionInstruction\":\"CREATE\",\"surveyName\":\"CENSUS\",\"caseId\":\"case-123\","
            + "\"caseRef\":\"ref-123\",\"addressType\":\"HH\",\"addressLevel\":\"U\","
            + "\"fieldOfficerId\":\"officer-123\"}",
        "case-123", "2026-09-21T10:15:30Z");

      RmAdapterActionInstructionDecoder.DecodedMessage decoded = decoder.decode(
        message, ActionInstructionContract.EXTERNAL_RM_ADAPTER);

    assertThat(decoded.getInstruction()).isInstanceOf(FwmtActionInstruction.class);
    FwmtActionInstruction instruction = (FwmtActionInstruction) decoded.getInstruction();
    assertThat(instruction.getCaseId()).isEqualTo("case-123");
    assertThat(instruction.getCaseRef()).isEqualTo("ref-123");
    assertThat(decoded.getMessageTime()).isEqualTo(Instant.parse("2026-09-21T10:15:30Z"));
  }

  @Test
  void decodesCancelPayloadWithoutOptionalFields() {
    PubsubMessage message = message(
        "{\"actionInstruction\":\"CANCEL\",\"surveyName\":\"CENSUS\",\"caseId\":\"case-123\"}",
        "case-123", "2026-09-21T10:15:30+01:00");

      RmAdapterActionInstructionDecoder.DecodedMessage decoded = decoder.decode(
        message, ActionInstructionContract.EXTERNAL_RM_ADAPTER);

    assertThat(decoded.getInstruction()).isInstanceOf(FwmtCancelActionInstruction.class);
    assertThat(decoded.getMessageTime()).isEqualTo(Instant.parse("2026-09-21T09:15:30Z"));
  }

  @Test
  void usesReceiveTimeWhenOccurredAtIsEmpty() {
    PubsubMessage message = message(
        "{\"actionInstruction\":\"UPDATE\",\"surveyName\":\"CENSUS\",\"caseId\":\"case-123\"}",
        "case-123", "");

    Instant before = Instant.now();
    RmAdapterActionInstructionDecoder.DecodedMessage decoded = decoder.decode(message);
    Instant after = Instant.now();

    assertThat(decoded.getMessageTime()).isBetween(before, after);
  }

  @Test
  void rejectsInvalidTimeAndCaseMismatch() {
    assertThatThrownBy(() -> decoder.decode(message(
        "{\"actionInstruction\":\"CREATE\",\"surveyName\":\"CENSUS\",\"caseId\":\"case-123\"}",
        "case-123", "not-an-instant")))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Invalid occurredAt");

    assertThatThrownBy(() -> decoder.decode(message(
        "{\"actionInstruction\":\"CREATE\",\"surveyName\":\"CENSUS\",\"caseId\":\"case-123\"}",
        "case-456", "2026-09-21T10:15:30Z")))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("caseId");
  }

  @Test
  void rejectsMissingActionAndNonCanonicalSurvey() {
    assertThatThrownBy(() -> decoder.decode(message(
        "{\"surveyName\":\"CENSUS\",\"caseId\":\"case-123\"}", "case-123", "")))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> decoder.decode(message(
        "{\"actionInstruction\":\"CREATE\",\"surveyName\":\"Census\",\"caseId\":\"case-123\"}",
        "case-123", "")))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("surveyName");
      assertThatThrownBy(() -> decoder.decode(message(
        "{\"surveyName\":\"CENSUS\",\"caseId\":\"case-123\"}", "case-123", ""),
        ActionInstructionContract.EXTERNAL_RM_ADAPTER))
        .isInstanceOf(IllegalArgumentException.class);
  }

      @Test
      void acceptsFeedbackOnlyOnInternalLane() {
      PubsubMessage message = message(
        "{\"actionInstruction\":\"CANCEL\",\"surveyName\":\"FEEDBACK\","
          + "\"addressType\":\"FEEDBACK\",\"addressLevel\":\"F\",\"caseId\":\"case-123\"}",
        "case-123", "2026-09-21T10:15:30Z");

      assertThat(decoder.decode(message, ActionInstructionContract.INTERNAL_FWMT)
        .getInstruction()).isInstanceOf(FwmtCancelActionInstruction.class);
      assertThatThrownBy(() -> decoder.decode(message, ActionInstructionContract.EXTERNAL_RM_ADAPTER))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("surveyName");
      }

      @Test
      void rejectsFeedbackActionInstructionOnInternalLane() {
      assertThatThrownBy(() -> decoder.decode(message(
        "{\"actionInstruction\":\"FEEDBACK\",\"surveyName\":\"FEEDBACK\",\"caseId\":\"case-123\"}",
        "case-123", ""), ActionInstructionContract.INTERNAL_FWMT))
        .isInstanceOf(IllegalArgumentException.class);
      }

  private static PubsubMessage message(String payload, String caseId, String occurredAt) {
    return PubsubMessage.newBuilder()
        .setData(ByteString.copyFromUtf8(payload))
        .putAttributes("caseId", caseId)
        .putAttributes("eventId", "event-123")
        .putAttributes("correlationId", "correlation-123")
        .putAttributes("eventType", "CASE_UPDATE")
        .putAttributes("schemaVersion", "1.0")
        .putAttributes("occurredAt", occurredAt)
        .build();
  }
}
