package uk.gov.ons.census.fwmt.jobservice.messaging;

import com.google.pubsub.v1.PubsubMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.common.messaging.FieldWorkerInstructionJsonCodec;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction;
import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class FieldWorkerInstructionMessageDispatcher {

  private final GWMessageProcessor gwMessageProcessor;
  private final FieldWorkerInstructionJsonCodec codec;
  private final RmAdapterActionInstructionDecoder rmAdapterDecoder;

  public void dispatch(PubsubMessage pubsubMessage) {
    Object payload = codec.fromPubsubMessage(pubsubMessage);
    String timestamp = pubsubMessage.getAttributesOrDefault(
        FieldWorkerInstructionJsonCodec.TIMESTAMP_HEADER,
        String.valueOf(System.currentTimeMillis()));
    Instant receivedMessageTime = Instant.ofEpochMilli(Long.parseLong(timestamp));

    if (payload instanceof FwmtActionInstruction instruction) {
      gwMessageProcessor.processCreateInstruction(instruction, receivedMessageTime, pubsubMessage);
    } else if (payload instanceof FwmtCancelActionInstruction instruction) {
      gwMessageProcessor.processCancelInstruction(instruction, receivedMessageTime, pubsubMessage);
    } else {
      throw new IllegalArgumentException("Unsupported field worker instruction payload: " + payload.getClass());
    }
  }

  public void dispatchRmAdapterInstruction(PubsubMessage pubsubMessage) {
    RmAdapterActionInstructionDecoder.DecodedMessage decoded = rmAdapterDecoder.decode(pubsubMessage);
    if (decoded.getMetadata().getOccurredAt().isEmpty()) {
      log.warn("RM adapter message has no occurredAt; using receive time eventId={} correlationId={} "
          + "actionInstruction={}", decoded.getMetadata().getEventId(), decoded.getMetadata().getCorrelationId(),
          actionInstruction(decoded.getInstruction()));
    }
    log.info("Received RM adapter action instruction eventId={} correlationId={} eventType={} "
        + "schemaVersion={} actionInstruction={}", decoded.getMetadata().getEventId(),
        decoded.getMetadata().getCorrelationId(), decoded.getMetadata().getEventType(),
        decoded.getMetadata().getSchemaVersion(), actionInstruction(decoded.getInstruction()));

    if (decoded.getInstruction() instanceof FwmtActionInstruction instruction) {
      gwMessageProcessor.processCreateInstructionAndPropagate(instruction, decoded.getMessageTime(), pubsubMessage);
    } else if (decoded.getInstruction() instanceof FwmtCancelActionInstruction instruction) {
      gwMessageProcessor.processCancelInstructionAndPropagate(instruction, decoded.getMessageTime(), pubsubMessage);
    } else {
      throw new IllegalArgumentException("Unsupported RM adapter instruction payload");
    }
  }

  private static String actionInstruction(Object instruction) {
    if (instruction instanceof FwmtActionInstruction action) {
      return String.valueOf(action.getActionInstruction());
    }
    if (instruction instanceof FwmtCancelActionInstruction cancel) {
      return String.valueOf(cancel.getActionInstruction());
    }
    return "UNKNOWN";
  }
}
