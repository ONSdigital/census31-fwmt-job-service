package uk.gov.ons.census.fwmt.jobservice.controller;

import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/RM")
public class RmListenerController {

  private static final String RM_LISTENER_ID = "rmListener";

  private final RabbitListenerEndpointRegistry registry;

  public RmListenerController(RabbitListenerEndpointRegistry registry) {
    this.registry = registry;
  }

  @GetMapping("/stopListener")
  public ResponseEntity<String> stopListener() {
    listenerContainer().stop();
    return ResponseEntity.ok("RM listener stopped.");
  }

  @GetMapping("/startListener")
  public ResponseEntity<String> startListener() {
    listenerContainer().start();
    return ResponseEntity.ok("RM listener started.");
  }

  private MessageListenerContainer listenerContainer() {
    MessageListenerContainer container = registry.getListenerContainer(RM_LISTENER_ID);
    if (container == null) {
      throw new IllegalStateException("RM listener container not registered: " + RM_LISTENER_ID);
    }
    return container;
  }
}
