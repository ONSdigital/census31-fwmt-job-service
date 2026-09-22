package uk.gov.ons.census.fwmt.jobservice.messaging.pubsub;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.cloud.spring.pubsub.support.GcpPubSubHeaders;
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageHandler;
import uk.gov.ons.census.fwmt.jobservice.messaging.FieldWorkerInstructionMessageDispatcher;

@ExtendWith(MockitoExtension.class)
class JobServicePubSubConfigTest {

  private final JobServicePubSubConfig config = new JobServicePubSubConfig();

  @Mock
  private FieldWorkerInstructionMessageDispatcher dispatcher;

  @Mock
  private BasicAcknowledgeablePubsubMessage originalMessage;

  @Test
  void internalHandlerDelegatesToRmAdapterDispatcher() {
    PubsubMessage pubsubMessage = PubsubMessage.newBuilder()
        .setData(ByteString.copyFromUtf8("{}"))
        .build();
    when(originalMessage.getPubsubMessage()).thenReturn(pubsubMessage);

    MessageHandler handler = config.fieldworkActionInstructionInternalPubSubHandler(dispatcher);
    handler.handleMessage(MessageBuilder.withPayload("ignored")
        .setHeader(GcpPubSubHeaders.ORIGINAL_MESSAGE, originalMessage)
        .build());

    verify(dispatcher).dispatchRmAdapterInstruction(pubsubMessage);
  }

  @Test
  void internalHandlerRejectsMissingOriginalMessageHeader() {
    MessageHandler handler = config.fieldworkActionInstructionInternalPubSubHandler(dispatcher);

    assertThatThrownBy(() -> handler.handleMessage(MessageBuilder.withPayload("ignored").build()))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Missing original Pub/Sub message header");
  }
}