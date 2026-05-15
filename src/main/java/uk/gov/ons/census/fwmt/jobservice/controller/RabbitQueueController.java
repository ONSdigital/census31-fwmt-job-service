package uk.gov.ons.census.fwmt.jobservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.jobservice.rabbit.QueueMigrator;

@Slf4j
@RestController
@RequestMapping("/jobs")
public class RabbitQueueController {

  public static final String originQ = "GW.Transient.ErrorQ";
  public static final String destRoute = "GW.Field";

  @Autowired
  private QueueMigrator queueMigrator;

  @GetMapping(value = "/migratetransients")
  public ResponseEntity<String> transferTransientMessagesToGWFieldQueue(@RequestParam(defaultValue = "GW.Transient.ErrorQ") String originQ,
      @RequestParam(defaultValue = "GW.Field") String destRoute) {
    try {
      queueMigrator.migrate(originQ, destRoute);
    } catch (GatewayException e) {
      log.error("Failed to send message from Q {} to route {}", originQ, destRoute);
      return ResponseEntity.badRequest().body("Failed to move messages from " + originQ + "to Route " + destRoute);
    }
    return ResponseEntity.ok("MIGRATION COMPLETE.");
  }
}
