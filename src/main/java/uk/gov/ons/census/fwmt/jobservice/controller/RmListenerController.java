package uk.gov.ons.census.fwmt.jobservice.controller;

import com.google.cloud.spring.pubsub.integration.inbound.PubSubInboundChannelAdapter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RmListenerController {

  private final PubSubInboundChannelAdapter externalActionInstructionAdapter;
  private final PubSubInboundChannelAdapter internalActionInstructionAdapter;

  public RmListenerController(
      @Qualifier("fieldworkActionInstructionPubSubInbound")
          PubSubInboundChannelAdapter externalActionInstructionAdapter,
      @Qualifier("fieldworkActionInstructionInternalPubSubInbound")
          PubSubInboundChannelAdapter internalActionInstructionAdapter) {
    this.externalActionInstructionAdapter = externalActionInstructionAdapter;
    this.internalActionInstructionAdapter = internalActionInstructionAdapter;
  }

  @PostMapping("/admin/pubsub/fieldwork-action-instruction/stop")
  public ResponseEntity<Void> stopExternalActionInstructionListener() {
    stopIfRunning(externalActionInstructionAdapter);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/admin/pubsub/fieldwork-action-instruction/start")
  public ResponseEntity<Void> startExternalActionInstructionListener() {
    startIfStopped(externalActionInstructionAdapter);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/admin/pubsub/fieldwork-action-instruction-internal/stop")
  public ResponseEntity<Void> stopInternalActionInstructionListener() {
    stopIfRunning(internalActionInstructionAdapter);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/admin/pubsub/fieldwork-action-instruction-internal/start")
  public ResponseEntity<Void> startInternalActionInstructionListener() {
    startIfStopped(internalActionInstructionAdapter);
    return ResponseEntity.noContent().build();
  }

  /** Backward-compatible alias for pausing the external RM-adapter subscription. */
  @GetMapping("/RM/stopListener")
  public ResponseEntity<String> stopListener() {
    stopIfRunning(externalActionInstructionAdapter);
    return ResponseEntity.ok("RM listener stopped.");
  }

  /** Backward-compatible alias for resuming the external RM-adapter subscription. */
  @GetMapping("/RM/startListener")
  public ResponseEntity<String> startListener() {
    startIfStopped(externalActionInstructionAdapter);
    return ResponseEntity.ok("RM listener started.");
  }

  private synchronized void stopIfRunning(PubSubInboundChannelAdapter adapter) {
    if (adapter.isRunning()) {
      adapter.stop();
    }
  }

  private synchronized void startIfStopped(PubSubInboundChannelAdapter adapter) {
    if (!adapter.isRunning()) {
      adapter.start();
    }
  }
}
