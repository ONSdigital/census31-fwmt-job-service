package uk.gov.ons.census.fwmt.jobservice.messaging.pubsub;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.cloud.spring.pubsub.integration.AckMode;
import com.google.cloud.spring.pubsub.integration.inbound.PubSubInboundChannelAdapter;
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;
import com.google.cloud.spring.pubsub.support.GcpPubSubHeaders;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import uk.gov.ons.census.fwmt.jobservice.messaging.FieldWorkerInstructionMessageDispatcher;
import uk.gov.ons.census.fwmt.jobservice.messaging.ActionInstructionContract;

@Configuration
public class JobServicePubSubConfig {

  @Value("${app.messaging.pubsub.fieldwork-action-instruction-subscription:job-service-fieldwork-action-instruction}")
  private String fieldworkActionInstructionSubscription;

  @Value("${app.messaging.pubsub.fieldwork-action-instruction-internal-subscription:job-service-fieldwork-action-instruction-internal}")
  private String fieldworkActionInstructionInternalSubscription;

  @Bean(name = "fieldworkActionInstructionPubSubInputChannel")
  public MessageChannel fieldworkActionInstructionPubSubInputChannel() {
    return new DirectChannel();
  }

  @Bean(name = "fieldworkActionInstructionInternalPubSubInputChannel")
  public MessageChannel fieldworkActionInstructionInternalPubSubInputChannel() {
    return new DirectChannel();
  }

  @Bean
  public PubSubInboundChannelAdapter fieldworkActionInstructionPubSubInbound(
      @Qualifier("fieldworkActionInstructionPubSubInputChannel") MessageChannel inputChannel,
      PubSubTemplate pubSubTemplate) {
    PubSubInboundChannelAdapter adapter =
        new PubSubInboundChannelAdapter(pubSubTemplate, fieldworkActionInstructionSubscription);
    adapter.setOutputChannel(inputChannel);
    adapter.setAckMode(AckMode.AUTO);
    return adapter;
  }

  @Bean
  public PubSubInboundChannelAdapter fieldworkActionInstructionInternalPubSubInbound(
      @Qualifier("fieldworkActionInstructionInternalPubSubInputChannel") MessageChannel inputChannel,
      PubSubTemplate pubSubTemplate) {
    PubSubInboundChannelAdapter adapter =
        new PubSubInboundChannelAdapter(pubSubTemplate, fieldworkActionInstructionInternalSubscription);
    adapter.setOutputChannel(inputChannel);
    adapter.setAckMode(AckMode.AUTO);
    return adapter;
  }

  @Bean
  @ServiceActivator(inputChannel = "fieldworkActionInstructionPubSubInputChannel")
  public MessageHandler fieldworkActionInstructionPubSubHandler(
      FieldWorkerInstructionMessageDispatcher dispatcher) {
    return actionInstructionHandler(dispatcher, ActionInstructionContract.EXTERNAL_RM_ADAPTER);
  }

  @Bean
  @ServiceActivator(inputChannel = "fieldworkActionInstructionInternalPubSubInputChannel")
  public MessageHandler fieldworkActionInstructionInternalPubSubHandler(
      FieldWorkerInstructionMessageDispatcher dispatcher) {
    return actionInstructionHandler(dispatcher, ActionInstructionContract.INTERNAL_FWMT);
  }

  private static MessageHandler actionInstructionHandler(
      FieldWorkerInstructionMessageDispatcher dispatcher, ActionInstructionContract contract) {
    return message -> {
      BasicAcknowledgeablePubsubMessage original = message.getHeaders()
          .get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);
      if (original == null) {
        throw new IllegalStateException("Missing original Pub/Sub message header");
      }
      if (contract == ActionInstructionContract.INTERNAL_FWMT) {
        dispatcher.dispatchInternalActionInstruction(original.getPubsubMessage());
      } else {
        dispatcher.dispatchExternalActionInstruction(original.getPubsubMessage());
      }
    };
  }

}
