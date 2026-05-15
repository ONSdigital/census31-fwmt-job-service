package uk.gov.ons.census.fwmt.jobservice.service.routing.hh;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import uk.gov.ons.census.fwmt.common.data.tm.CasePauseRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.http.comet.CometRestClient;
import uk.gov.ons.census.fwmt.jobservice.service.GatewayCacheService;
import uk.gov.ons.census.fwmt.jobservice.service.converter.hh.HhPauseConverter;
import uk.gov.ons.census.fwmt.jobservice.service.processor.InboundProcessor;
import uk.gov.ons.census.fwmt.jobservice.service.processor.ProcessorKey;
import uk.gov.ons.census.fwmt.jobservice.service.routing.RoutingValidator;

import java.time.Instant;

import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.CASE_DOES_NOT_EXIST;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.FAILED_TO_CREATE_TM_JOB;

@Qualifier("Pause")
@Service
public class HhPause implements InboundProcessor<FwmtActionInstruction> {

  private static final String PROCESSING = "PROCESSING";
  
  public static final String COMET_PAUSE_PRE_SENDING = "COMET_PAUSE_PRE_SENDING";

  public static final String COMET_PAUSE_ACK = "COMET_PAUSE_ACK";

  private static final ProcessorKey key = ProcessorKey.builder()
      .actionInstruction(ActionInstructionType.PAUSE.toString())
      .surveyName("CENSUS")
      .addressType("HH")
      .addressLevel("U")
      .build();

  @Autowired
  private CometRestClient cometRestClient;

  @Autowired
  private GatewayEventManager eventManager;

  @Autowired
  private RoutingValidator routingValidator;

  @Autowired
  private GatewayCacheService cacheService;

  @Override
  public ProcessorKey getKey() {
    return key;
  }

  @Override
  public boolean isValid(FwmtActionInstruction rmRequest, GatewayCache cache) {
    try {
      return rmRequest.getActionInstruction() == ActionInstructionType.PAUSE
          && rmRequest.getSurveyName().equals("CENSUS")
          && rmRequest.getAddressType().equals("HH")
          && rmRequest.getAddressLevel().equals("U")
          && (cache != null && cache.existsInFwmt && !cache.lastActionInstruction.equals("CANCEL"));
    } catch (NullPointerException e) {
      return false;
    }
  }

  @Override
  public void process(FwmtActionInstruction rmRequest, GatewayCache cache, Instant messageReceivedTime) throws GatewayException {

    boolean alreadyCancelled = false;
    ResponseEntity<Void> response = null;

    eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), PROCESSING,
        "type", "HH Pause Case",
        "action", "Pause");
    

    CasePauseRequest tmRequest = HhPauseConverter.buildPause(rmRequest);

    eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), COMET_PAUSE_PRE_SENDING,
        "Case Ref", rmRequest.getCaseRef());

    try {
      response = cometRestClient.sendPause(tmRequest, rmRequest.getCaseId());
      routingValidator.validateResponseCode(response, rmRequest.getCaseId(), "Pause", FAILED_TO_CREATE_TM_JOB, "tmRequest", tmRequest.toString(), "rmRequest", rmRequest.toString(), "cache", (cache!=null)?cache.toString():"");
    } catch (RestClientException e) {
      String tmResponse = e.getMessage();
      if (tmResponse != null && tmResponse.contains("404") && tmResponse.contains("Unable to find Case")){
        eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), CASE_DOES_NOT_EXIST,
            "A HH pause case has been received for a case that does not exist",
            "Message received: " + rmRequest.toString());
        alreadyCancelled = true;
      } else {
        throw e;
      }
    }

    if (response != null && !alreadyCancelled) {
      GatewayCache newCache = cacheService.getById(rmRequest.getCaseId());
      if (newCache != null) {
        cacheService.save(newCache.toBuilder().lastActionInstruction(ActionInstructionType.CANCEL.toString())
            .lastActionTime(messageReceivedTime)
            .build());
      }

      eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), COMET_PAUSE_ACK,
          "Case Ref", rmRequest.getCaseRef(),
          "Response Code", response.getStatusCode().name(),
          "HH Pause", tmRequest.toString());
    }
  }
}
