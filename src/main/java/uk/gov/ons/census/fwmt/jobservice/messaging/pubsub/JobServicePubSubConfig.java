package uk.gov.ons.census.fwmt.jobservice.messaging.pubsub;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.cloud.spring.pubsub.integration.AckMode;
import com.google.cloud.spring.pubsub.integration.inbound.PubSubInboundChannelAdapter;
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;
import com.google.cloud.spring.pubsub.support.GcpPubSubHeaders;
import com.google.pubsub.v1.PubsubMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import uk.gov.ons.census.fwmt.common.messaging.MessagingProperties;
import uk.gov.ons.census.fwmt.jobservice.messaging.FieldWorkerInstructionMessageDispatcher;

@Configuration
@ConditionalOnProperty(name = MessagingProperties.PROVIDER, havingValue = MessagingProperties.PROVIDER_PUBSUB)
@Slf4j
public class JobServicePubSubConfig {

  @Value("${app.messaging.pubsub.rm-field-subscription:job-service-RM-Field}")
  private String rmFieldSubscription;

  @Value("${app.messaging.pubsub.gw-field-subscription:job-service-GW-Field}")
  private String gwFieldSubscription;

  @Bean(name = "rmFieldPubSubInputChannel")
  public MessageChannel rmFieldPubSubInputChannel() {
    return new DirectChannel();
  }

  @Bean(name = "gwFieldPubSubInputChannel")
  public MessageChannel gwFieldPubSubInputChannel() {
    return new DirectChannel();
  }

  @Bean
  public PubSubInboundChannelAdapter rmFieldPubSubInbound(
      @Qualifier("rmFieldPubSubInputChannel") MessageChannel inputChannel,
      PubSubTemplate pubSubTemplate) {
    PubSubInboundChannelAdapter adapter = new PubSubInboundChannelAdapter(pubSubTemplate, rmFieldSubscription);
    adapter.setOutputChannel(inputChannel);
    adapter.setAckMode(AckMode.MANUAL);
    return adapter;
  }

  @Bean
  public PubSubInboundChannelAdapter gwFieldPubSubInbound(
      @Qualifier("gwFieldPubSubInputChannel") MessageChannel inputChannel,
      PubSubTemplate pubSubTemplate) {
    PubSubInboundChannelAdapter adapter = new PubSubInboundChannelAdapter(pubSubTemplate, gwFieldSubscription);
    adapter.setOutputChannel(inputChannel);
    adapter.setAckMode(AckMode.MANUAL);
    return adapter;
  }

  @Bean
  @ServiceActivator(inputChannel = "rmFieldPubSubInputChannel")
  public MessageHandler rmFieldPubSubHandler(FieldWorkerInstructionMessageDispatcher dispatcher) {
    return pubSubHandler(dispatcher);
  }

  @Bean
  @ServiceActivator(inputChannel = "gwFieldPubSubInputChannel")
  public MessageHandler gwFieldPubSubHandler(FieldWorkerInstructionMessageDispatcher dispatcher) {
    return pubSubHandler(dispatcher);
  }

  private static MessageHandler pubSubHandler(FieldWorkerInstructionMessageDispatcher dispatcher) {
    return message -> {
      BasicAcknowledgeablePubsubMessage original =
          message.getHeaders().get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);
      PubsubMessage pubsubMessage = original.getPubsubMessage();
      try {
        dispatcher.dispatch(pubsubMessage);
        original.ack();
      } catch (RuntimeException ex) {
        log.error("Failed to process field worker instruction Pub/Sub message", ex);
        original.nack();
        throw ex;
      }
    };
  }
}
