package uk.gov.ons.census.fwmt.jobservice.controller;

public class PubSubController {

  /*
  @Autowired
  private PubSubTemplate pubSubTemplate;

  @Value("${app.messaging.subscriptions.gwTransientError:job-service-GW-Transient-ErrorQ}")
  private String gwTransientErrorSubscription;

  @Value("${app.messaging.destinations.gwField:GW.Field}")
  private String gwFieldTopic;

  // Deferred until the errored-message destination is agreed.
  // @GetMapping(value = "/migratetransients")
  // public ResponseEntity<String> transferTransientMessagesToGWFieldQueue(
  //     @RequestParam(defaultValue = "GW.Transient.ErrorQ") String originQ,
  //     @RequestParam(defaultValue = "GW.Field") String destRoute) {
  //   try {
  //     migrateTransientPubSub();
  //   } catch (GatewayException e) {
  //     log.error("Failed to send message from Q {} to route {}", originQ, destRoute);
  //     return ResponseEntity.badRequest().body("Failed to move messages from " + originQ + "to Route " + destRoute);
  //   }
  //   return ResponseEntity.ok("MIGRATION COMPLETE.");
  // }

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
  */
}
