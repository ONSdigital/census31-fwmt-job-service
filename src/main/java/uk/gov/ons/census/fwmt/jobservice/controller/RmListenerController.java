package uk.gov.ons.census.fwmt.jobservice.controller;

import com.google.cloud.spring.pubsub.integration.inbound.PubSubInboundChannelAdapter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/RM")
public class RmListenerController {

  private final ObjectProvider<PubSubInboundChannelAdapter> rmFieldPubSubInbound;
  private final ObjectProvider<PubSubInboundChannelAdapter> gwFieldPubSubInbound;

  public RmListenerController(
      @Qualifier("rmFieldPubSubInbound") ObjectProvider<PubSubInboundChannelAdapter> rmFieldPubSubInbound,
      @Qualifier("gwFieldPubSubInbound") ObjectProvider<PubSubInboundChannelAdapter> gwFieldPubSubInbound) {
    this.rmFieldPubSubInbound = rmFieldPubSubInbound;
    this.gwFieldPubSubInbound = gwFieldPubSubInbound;
  }

  @GetMapping("/stopListener")
  public ResponseEntity<String> stopListener() {
    rmFieldPubSubInbound.ifAvailable(PubSubInboundChannelAdapter::stop);
    gwFieldPubSubInbound.ifAvailable(PubSubInboundChannelAdapter::stop);
    return ResponseEntity.ok("RM listener stopped.");
  }

  @GetMapping("/startListener")
  public ResponseEntity<String> startListener() {
    rmFieldPubSubInbound.ifAvailable(PubSubInboundChannelAdapter::start);
    gwFieldPubSubInbound.ifAvailable(PubSubInboundChannelAdapter::start);
    return ResponseEntity.ok("RM listener started.");
  }
}
