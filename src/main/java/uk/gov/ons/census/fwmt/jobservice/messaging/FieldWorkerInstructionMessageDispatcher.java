package uk.gov.ons.census.fwmt.jobservice.messaging;

import com.google.pubsub.v1.PubsubMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.common.messaging.FieldWorkerInstructionJsonCodec;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class FieldWorkerInstructionMessageDispatcher {

  private final GWMessageProcessor gwMessageProcessor;
  private final FieldWorkerInstructionJsonCodec codec;

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
}
