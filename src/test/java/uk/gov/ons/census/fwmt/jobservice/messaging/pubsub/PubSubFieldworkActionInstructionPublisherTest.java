package uk.gov.ons.census.fwmt.jobservice.messaging.pubsub;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.pubsub.v1.PubsubMessage;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction;

@ExtendWith(MockitoExtension.class)
class PubSubFieldworkActionInstructionPublisherTest {

  private static final String TOPIC = "event_fieldwork_action-instruction_internal";

  @Mock
  private PubSubTemplate pubSubTemplate;

  private PubSubFieldworkActionInstructionPublisher publisher;
  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  @BeforeEach
  void setUp() {
    publisher = new PubSubFieldworkActionInstructionPublisher(pubSubTemplate, objectMapper);
    ReflectionTestUtils.setField(publisher, "fieldworkActionInstructionInternalTopic", TOPIC);
  }

  @Test
  void publishesFlatCreatePayloadToInternalTopic() throws Exception {
    when(pubSubTemplate.publish(eq(TOPIC), any(PubsubMessage.class)))
        .thenReturn(CompletableFuture.completedFuture("message-123"));

    publisher.publish(createInstruction());

    ArgumentCaptor<PubsubMessage> messageCaptor = ArgumentCaptor.forClass(PubsubMessage.class);
    verify(pubSubTemplate).publish(eq(TOPIC), messageCaptor.capture());

    PubsubMessage message = messageCaptor.getValue();
    JsonNode payload = objectMapper.readTree(message.getData().toStringUtf8());

    assertThat(payload.get("actionInstruction").asText()).isEqualTo("CREATE");
    assertThat(payload.get("surveyName").asText()).isEqualTo("CENSUS");
    assertThat(payload.get("caseId").asText()).isEqualTo("case-123");
    assertThat(payload.has("header")).isFalse();
    assertThat(payload.has("payload")).isFalse();

    assertCommonAttributes(message, "case-123");
  }

  @Test
  void publishesFlatCancelPayloadWithoutLegacyAttributes() throws Exception {
    when(pubSubTemplate.publish(eq(TOPIC), any(PubsubMessage.class)))
        .thenReturn(CompletableFuture.completedFuture("message-123"));

    publisher.publish(cancelInstruction());

    ArgumentCaptor<PubsubMessage> messageCaptor = ArgumentCaptor.forClass(PubsubMessage.class);
    verify(pubSubTemplate).publish(eq(TOPIC), messageCaptor.capture());

    PubsubMessage message = messageCaptor.getValue();
    JsonNode payload = objectMapper.readTree(message.getData().toStringUtf8());

    assertThat(payload.get("actionInstruction").asText()).isEqualTo("CANCEL");
    assertThat(payload.get("caseId").asText()).isEqualTo("case-456");
    assertThat(message.getAttributesMap()).doesNotContainKeys("__TypeId__", "timestamp");

    assertCommonAttributes(message, "case-456");
  }

  private void assertCommonAttributes(PubsubMessage message, String caseId) {
    assertThat(message.getAttributesOrDefault("eventId", "")).isNotBlank();
    assertThat(message.getAttributesOrDefault("correlationId", "missing")).isEmpty();
    assertThat(message.getAttributesOrDefault("caseId", "")).isEqualTo(caseId);
    assertThat(message.getAttributesOrDefault("eventType", "")).isEqualTo("FIELDWORK_ACTION_INSTRUCTION");
    assertThat(message.getAttributesOrDefault("schemaVersion", "")).isEqualTo("1.0");
    assertThat(Instant.parse(message.getAttributesOrDefault("occurredAt", ""))).isNotNull();
  }

  private static FwmtActionInstruction createInstruction() {
    FwmtActionInstruction instruction = new FwmtActionInstruction();
    instruction.setActionInstruction(ActionInstructionType.CREATE);
    instruction.setSurveyName("CENSUS");
    instruction.setCaseId("case-123");
    instruction.setCaseRef("ref-123");
    instruction.setAddressType("HH");
    instruction.setAddressLevel("U");
    return instruction;
  }

  private static FwmtCancelActionInstruction cancelInstruction() {
    FwmtCancelActionInstruction instruction = new FwmtCancelActionInstruction();
    instruction.setActionInstruction(ActionInstructionType.CANCEL);
    instruction.setSurveyName("CENSUS");
    instruction.setCaseId("case-456");
    instruction.setAddressType("HH");
    instruction.setAddressLevel("U");
    return instruction;
  }
}