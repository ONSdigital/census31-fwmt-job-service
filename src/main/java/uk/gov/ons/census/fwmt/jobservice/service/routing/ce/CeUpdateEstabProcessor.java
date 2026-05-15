package uk.gov.ons.census.fwmt.jobservice.service.routing.ce;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.common.data.tm.CeCasePatchRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.http.comet.CometRestClient;
import uk.gov.ons.census.fwmt.jobservice.service.GatewayCacheService;
import uk.gov.ons.census.fwmt.jobservice.service.converter.ce.CeUpdateConverter;
import uk.gov.ons.census.fwmt.jobservice.service.processor.InboundProcessor;
import uk.gov.ons.census.fwmt.jobservice.service.processor.ProcessorKey;
import uk.gov.ons.census.fwmt.jobservice.service.routing.RoutingValidator;

import java.time.Instant;

import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_UPDATE_ACK;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_UPDATE_PRE_SENDING;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.FAILED_TO_UPDATE_TM_JOB;

@Qualifier("Update")
@Service
public class CeUpdateEstabProcessor implements InboundProcessor<FwmtActionInstruction> {

  @Autowired
  private CometRestClient cometRestClient;

  @Autowired
  private GatewayEventManager eventManager;

  @Autowired
  private RoutingValidator routingValidator;

  @Autowired
  private GatewayCacheService cacheService;

  private static ProcessorKey key = ProcessorKey.builder()
      .actionInstruction(ActionInstructionType.UPDATE.toString())
      .surveyName("CENSUS")
      .addressType("CE")
      .addressLevel("E")
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
          && rmRequest.getAddressType().equals("CE")
          && rmRequest.getAddressLevel().equals("E")
          && (cache != null
          && cache.getType() == 1
          && cache.existsInFwmt);
    } catch (NullPointerException e) {
      return false;
    }
  }

  @Override
  public void process(FwmtActionInstruction rmRequest, GatewayCache cache, Instant messageReceivedTime) throws GatewayException {
    CeCasePatchRequest tmRequest;

    tmRequest = CeUpdateConverter.convertEstab(rmRequest);

    eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), COMET_UPDATE_PRE_SENDING,
        "Case Ref", rmRequest.getCaseRef());

    ResponseEntity<Void> response = cometRestClient.sendCeDetails(tmRequest, rmRequest.getCaseId());
    routingValidator.validateResponseCode(response, rmRequest.getCaseId(), "Update", FAILED_TO_UPDATE_TM_JOB,
        "tmRequest", tmRequest.toString(),
        "rmRequest", rmRequest.toString(),
        "cache", (cache!=null)?cache.toString():"");

    GatewayCache newCache = cacheService.getById(rmRequest.getCaseId());
    if (newCache != null) {
      cacheService.save(newCache.toBuilder().lastActionInstruction(rmRequest.getActionInstruction().toString())
          .lastActionTime(messageReceivedTime)
          .build());
    }

    eventManager
        .triggerEvent(String.valueOf(rmRequest.getCaseId()), COMET_UPDATE_ACK,
            "Case Ref", rmRequest.getCaseRef(),
            "CE Update Establishment", tmRequest.toString(),
            "Response Code", response.getStatusCode().name());
  }
}
