package uk.gov.ons.census.fwmt.jobservice.controller;

import com.google.cloud.spring.pubsub.integration.inbound.PubSubInboundChannelAdapter;
import java.util.Optional;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/RM")
public class RmListenerController {

  private static final String RM_LISTENER_ID = "rmListener";

  private final ObjectProvider<RabbitListenerEndpointRegistry> registry;
  private final ObjectProvider<PubSubInboundChannelAdapter> rmFieldPubSubInbound;
  private final ObjectProvider<PubSubInboundChannelAdapter> gwFieldPubSubInbound;

  public RmListenerController(
      ObjectProvider<RabbitListenerEndpointRegistry> registry,
      @Qualifier("rmFieldPubSubInbound") ObjectProvider<PubSubInboundChannelAdapter> rmFieldPubSubInbound,
      @Qualifier("gwFieldPubSubInbound") ObjectProvider<PubSubInboundChannelAdapter> gwFieldPubSubInbound) {
    this.registry = registry;
    this.rmFieldPubSubInbound = rmFieldPubSubInbound;
    this.gwFieldPubSubInbound = gwFieldPubSubInbound;
  }

  @GetMapping("/stopListener")
  public ResponseEntity<String> stopListener() {
    rabbitContainer().ifPresent(MessageListenerContainer::stop);
    rmFieldPubSubInbound.ifAvailable(PubSubInboundChannelAdapter::stop);
    gwFieldPubSubInbound.ifAvailable(PubSubInboundChannelAdapter::stop);
    return ResponseEntity.ok("RM listener stopped.");
  }

  @GetMapping("/startListener")
  public ResponseEntity<String> startListener() {
    rabbitContainer().ifPresent(MessageListenerContainer::start);
    rmFieldPubSubInbound.ifAvailable(PubSubInboundChannelAdapter::start);
    gwFieldPubSubInbound.ifAvailable(PubSubInboundChannelAdapter::start);
    return ResponseEntity.ok("RM listener started.");
  }

  private Optional<MessageListenerContainer> rabbitContainer() {
    RabbitListenerEndpointRegistry rabbitRegistry = registry.getIfAvailable();
    if (rabbitRegistry == null) {
      return Optional.empty();
    }
    MessageListenerContainer container = rabbitRegistry.getListenerContainer(RM_LISTENER_ID);
    return Optional.ofNullable(container);
  }
}
