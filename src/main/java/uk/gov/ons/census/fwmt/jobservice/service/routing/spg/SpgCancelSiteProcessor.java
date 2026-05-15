package uk.gov.ons.census.fwmt.jobservice.service.routing.spg;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.http.comet.CometRestClient;
import uk.gov.ons.census.fwmt.jobservice.service.GatewayCacheService;
import uk.gov.ons.census.fwmt.jobservice.service.processor.InboundProcessor;
import uk.gov.ons.census.fwmt.jobservice.service.processor.ProcessorKey;
import uk.gov.ons.census.fwmt.jobservice.service.routing.RoutingValidator;

import java.time.Instant;

import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.CANCEL_ON_A_CANCEL;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_CANCEL_ACK;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_CANCEL_PRE_SENDING;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.FAILED_TO_CANCEL_TM_JOB;

@Qualifier("Cancel")
@Component
public class SpgCancelSiteProcessor implements InboundProcessor<FwmtCancelActionInstruction> {

  @Autowired
  private CometRestClient cometRestClient;

  @Autowired
  private GatewayEventManager eventManager;

  @Autowired
  private RoutingValidator routingValidator;

  @Autowired
  private GatewayCacheService cacheService;

  private static ProcessorKey key = ProcessorKey.builder()
      .actionInstruction(ActionInstructionType.CANCEL.toString())
      .surveyName("CENSUS")
      .addressType("SPG")
      .addressLevel("E")
      .build();

  @Override
  public ProcessorKey getKey() {
    return key;
  }

  // TODO Remove format on save
  // TODO add ffa formatter (modify)
  // TODO Find ignore formatting tag
  // TODO Make eventManager Annotation
  @Override
  public boolean isValid(FwmtCancelActionInstruction rmRequest, GatewayCache cache) {
    try {
      // relies on the validation of: SpgRouter, SpgCancelRouter
      return rmRequest.getActionInstruction() == ActionInstructionType.CANCEL
          && rmRequest.getSurveyName().equals("CENSUS")
          && rmRequest.getAddressType().equals("SPG")
          && rmRequest.getAddressLevel().equals("E")
          && cache != null;
    } catch (NullPointerException e) {
      return false;
    }
  }

  // TODO Acceptance test should check delete is sent (new event)
  // TODO Can event be added in class where its used, rather than config, or can it be added when used first time
  @Override
  public void process(FwmtCancelActionInstruction rmRequest, GatewayCache cache, Instant messageReceivedTime) throws GatewayException {
    boolean alreadyCancelled = false;
    ResponseEntity<Void> response = null;
    eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), COMET_CANCEL_PRE_SENDING,
        "Case Ref", "N/A",
        "TM Action", "CLOSE",
        "Source", "RM");

    try{
      response = cometRestClient.sendClose(rmRequest.getCaseId());
      routingValidator.validateResponseCode(response, rmRequest.getCaseId(), "Cancel", FAILED_TO_CANCEL_TM_JOB, "rmRequest", rmRequest.toString(), "cache", (cache!=null)?cache.toString():"");
    } catch (RestClientException e) {
      String tmResponse = e.getMessage();
      if (tmResponse != null && tmResponse.contains("400") && tmResponse.contains("Case State must be Open")){
        eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), CANCEL_ON_A_CANCEL,
            "A cancel case has been received for a case that already has been cancelled",
            "Message received: " + rmRequest.toString());
        alreadyCancelled = true;
      } else {
        throw e;
      }
    }

    GatewayCache newCache = cacheService.getById(rmRequest.getCaseId());
    if (newCache == null) {
      cacheService.save(GatewayCache.builder().caseId(rmRequest.getCaseId()).existsInFwmt(true)
          .type(2).lastActionInstruction(rmRequest.getActionInstruction().toString())
          .lastActionTime(messageReceivedTime).build());
    } else {
      cacheService.save(newCache.toBuilder().existsInFwmt(true).lastActionInstruction(rmRequest.getActionInstruction().toString())
          .lastActionTime(messageReceivedTime).build());
    }


    if (response != null && !alreadyCancelled) {
      eventManager
          .triggerEvent(String.valueOf(rmRequest.getCaseId()), COMET_CANCEL_ACK,
              "Case Ref", "N/A",
              "Response Code", response.getStatusCode().name());

    }
  }
}
