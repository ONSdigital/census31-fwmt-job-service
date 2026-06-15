package uk.gov.ons.census.fwmt.jobservice.rabbit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.pubsub.v1.PubsubMessage;
import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCommonInstruction;
import uk.gov.ons.census.fwmt.jobservice.data.QuarantinedMessage;
import uk.gov.ons.census.fwmt.jobservice.repository.QuarantinedMessageRepository;

import jakarta.annotation.PostConstruct;
import java.util.Map;

@Slf4j
@Component
public class MessageExceptionHandler {

  @Autowired
  private PubSubTemplate pubSubTemplate;

  @Autowired
  private QuarantinedMessageRepository quarantinedMessageRepository;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Value("${app.messaging.maxRetryCount:5}")
  private int maxRetryCount;

  @Value("${app.messaging.destinations.gwField:GW.Field}")
  private String gwFieldQueue;

  @Value("${app.messaging.destinations.gwTransientError:GW.Transient.ErrorQ}")
  private String gwTransientErrorTopic;

  @Value("${app.messaging.destinations.gwPermanentError:GW.Permanent.ErrorQ}")
  private String gwPermanentErrorTopic;

  @PostConstruct
  public void transientExceptionHandler() {
    log.info("TransientExceptionHandler maxRetryCount :{}", maxRetryCount);
    log.info("TransientExceptionHandler gwTransientErrorTopic :{}", gwTransientErrorTopic);
    log.info("TransientExceptionHandler gwPermanentErrorTopic :{}", gwPermanentErrorTopic);
  }

  public void handleTransientMessage(PubsubMessage message, FwmtCommonInstruction instruction) {
    Integer retryCount = parseRetryCount(message);
    if (retryCount < maxRetryCount) {
      int nextRetryCount = retryCount + 1;
      publishPubSub(gwTransientErrorTopic, message, Map.of("retryCount", String.valueOf(nextRetryCount)));
      log.warn("Republished transient error to Pub/Sub topic={} retryCount={}", gwTransientErrorTopic, nextRetryCount);
    } else {
      log.error("We've reached our retry limit {}", maxRetryCount);
      handlePermMessage(message, instruction);
    }
  }

  public void handlePermMessage(PubsubMessage message, FwmtCommonInstruction instruction) {
    publishPubSub(gwPermanentErrorTopic, message, Map.of());
    log.warn("Republished permanent error to Pub/Sub topic={}", gwPermanentErrorTopic);

    final QuarantinedMessage quarantinedMessage = QuarantinedMessage.builder()
        .messagePayload(messagePayload(message, instruction))
        .caseId(instruction.getCaseId())
        .routingKey("")
        .actionInstruction(instruction.getActionInstruction())
        .addressLevel(instruction.getAddressLevel())
        .addressType(instruction.getAddressType())
        .nc(instruction.isNc())
        .surveyName(instruction.getSurveyName())
        .queue(gwFieldQueue)
        .headers(message.getAttributesMap().entrySet().stream()
            .collect(java.util.stream.Collectors.toMap(
                java.util.Map.Entry::getKey,
                e -> (Object) e.getValue())))
        .build();

    quarantinedMessageRepository.save(quarantinedMessage);
    log.info("Stored perm issue in DB for later processing {}", maxRetryCount);
  }

  private void publishPubSub(String topic, PubsubMessage original, Map<String, String> extraAttributes) {
    PubsubMessage.Builder builder = PubsubMessage.newBuilder()
        .setData(original.getData() == null ? ByteString.EMPTY : original.getData())
        .putAllAttributes(original.getAttributesMap());

    if (extraAttributes != null && !extraAttributes.isEmpty()) {
      builder.putAllAttributes(extraAttributes);
    }

    pubSubTemplate.publish(topic, builder.build());
  }

  private Integer parseRetryCount(PubsubMessage message) {
    try {
      return Integer.parseInt(message.getAttributesOrDefault("retryCount", "0"));
    } catch (RuntimeException ex) {
      return 0;
    }
  }

  private byte[] messagePayload(PubsubMessage message, FwmtCommonInstruction instruction) {
    if (message != null) {
      return message.getData().toByteArray();
    }
    try {
      return objectMapper.writeValueAsBytes(instruction);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Failed to serialise instruction for quarantine", e);
    }
  }
}
