package uk.gov.ons.census.fwmt.jobservice.controller;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.ons.census.fwmt.common.error.GatewayException;

@Slf4j
@RestController
@RequestMapping("/jobs")
public class RabbitQueueController {

  public static final String originQ = "GW.Transient.ErrorQ";
  public static final String destRoute = "GW.Field";

  @Autowired
  private PubSubTemplate pubSubTemplate;

  @Value("${app.messaging.subscriptions.gwTransientError:job-service-GW-Transient-ErrorQ}")
  private String gwTransientErrorSubscription;

  @Value("${app.messaging.destinations.gwField:GW.Field}")
  private String gwFieldTopic;

  @GetMapping(value = "/migratetransients")
  public ResponseEntity<String> transferTransientMessagesToGWFieldQueue(
      @RequestParam(defaultValue = "GW.Transient.ErrorQ") String originQ,
      @RequestParam(defaultValue = "GW.Field") String destRoute) {
    try {
      migrateTransientPubSub();
    } catch (GatewayException e) {
      log.error("Failed to send message from Q {} to route {}", originQ, destRoute);
      return ResponseEntity.badRequest().body("Failed to move messages from " + originQ + "to Route " + destRoute);
    }
    return ResponseEntity.ok("MIGRATION COMPLETE.");
  }

  private void migrateTransientPubSub() throws GatewayException {
    int moved = 0;
    while (true) {
      var messages = pubSubTemplate.pull(gwTransientErrorSubscription, 500, true);
      if (messages == null || messages.isEmpty()) {
        break;
      }
      for (BasicAcknowledgeablePubsubMessage msg : messages) {
        pubSubTemplate.publish(gwFieldTopic, msg.getPubsubMessage());
        msg.ack();
        moved++;
      }
    }
    log.info("Migrated {} transient GW error messages back to {}", moved, gwFieldTopic);
  }
}
