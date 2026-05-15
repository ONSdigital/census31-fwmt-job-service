package uk.gov.ons.census.fwmt.jobservice.service.routing.hh;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.common.data.tm.CaseRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.http.comet.CometRestClient;
import uk.gov.ons.census.fwmt.jobservice.service.GatewayCacheService;
import uk.gov.ons.census.fwmt.jobservice.service.converter.hh.HhCreateConverter;
import uk.gov.ons.census.fwmt.jobservice.service.processor.InboundProcessor;
import uk.gov.ons.census.fwmt.jobservice.service.processor.ProcessorKey;
import uk.gov.ons.census.fwmt.jobservice.service.routing.RoutingValidator;

import java.time.Instant;

import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_DELETE_ACK;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_DELETE_PRE_SENDING;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_UPDATE_ACK;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_UPDATE_PRE_SENDING;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.FAILED_TO_CREATE_TM_JOB;

@Qualifier("Update")
@Service
public class HhUpdateEnglandAndWales implements InboundProcessor<FwmtActionInstruction> {

  private static final String PROCESSING = "PROCESSING";

  @Autowired
  private CometRestClient cometRestClient;

  @Autowired
  private GatewayEventManager eventManager;

  @Autowired
  private RoutingValidator routingValidator;

  @Autowired
  private GatewayCacheService cacheService;

  private static final ProcessorKey key = ProcessorKey.builder()
      .actionInstruction(ActionInstructionType.UPDATE.toString())
      .surveyName("CENSUS")
      .addressType("HH")
      .addressLevel("U")
      .build();

  @Override
  public ProcessorKey getKey() {
    return key;
  }

  @Override
  public boolean isValid(FwmtActionInstruction rmRequest, GatewayCache cache) {
    try {
      return rmRequest.getActionInstruction() == ActionInstructionType.UPDATE
          && rmRequest.getSurveyName().equals("CENSUS")
          && rmRequest.getAddressType().equals("HH")
          && !rmRequest.getOa().startsWith("N")
          && (cache != null && cache.existsInFwmt);
    } catch (NullPointerException e) {
      return false;
    }
  }

  @Override
  public void process(FwmtActionInstruction rmRequest, GatewayCache cache, Instant messageReceivedTime) throws GatewayException {
    eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), PROCESSING,
        "type", "HH E & W",
        "action", "Update");
    CaseRequest tmRequest = HhCreateConverter.convertHhEnglandAndWales(rmRequest, cache);

    GatewayCache newCache = cacheService.getById(rmRequest.getCaseId());

    eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), COMET_UPDATE_PRE_SENDING,
        "Case Ref", tmRequest.getReference(),
        "Survey Type", tmRequest.getSurveyType().toString());

    ResponseEntity<Void> response = cometRestClient.sendCreate(tmRequest, rmRequest.getCaseId());
    routingValidator.validateResponseCode(response, rmRequest.getCaseId(), "Update", FAILED_TO_CREATE_TM_JOB, "tmRequest", tmRequest.toString(), "rmRequest", rmRequest.toString(), "cache", (cache!=null)?cache.toString():"");

    if (newCache != null) {
      cacheService.save(newCache.toBuilder().lastActionInstruction(rmRequest.getActionInstruction().toString())
          .lastActionTime(messageReceivedTime)
          .build());
    }

    eventManager
        .triggerEvent(String.valueOf(rmRequest.getCaseId()), COMET_UPDATE_ACK,
            "Case Ref", rmRequest.getCaseRef(),
            "Response Code", response.getStatusCode().name(),
            "Survey Type", tmRequest.getSurveyType().toString());

    if (rmRequest.isBlankFormReturned() || rmRequest.isUndeliveredAsAddress()) {
      eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), COMET_DELETE_PRE_SENDING,
          "Case Ref", tmRequest.getReference(),
          "Survey Type", tmRequest.getSurveyType().toString());

      response = cometRestClient.sendDeletePause(rmRequest.getCaseId());
      routingValidator.validateResponseCode(response, rmRequest.getCaseId(), "Delete", FAILED_TO_CREATE_TM_JOB, "rmRequest", rmRequest.toString(), "cache", (cache!=null)?cache.toString():"");

      if (newCache != null) {
        cacheService.save(newCache.toBuilder().lastActionInstruction(rmRequest.getActionInstruction().toString())
            .lastActionTime(messageReceivedTime)
            .build());
      }

      eventManager
          .triggerEvent(String.valueOf(rmRequest.getCaseId()), COMET_DELETE_ACK,
              "Case Ref", rmRequest.getCaseRef(),
              "Response Code", response.getStatusCode().name(),
              "Survey Type", tmRequest.getSurveyType().toString(),
              "HH Update England And Wales", rmRequest.toString());
    }
  }
}
