package uk.gov.ons.census.fwmt.jobservice.service.routing.spg;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.common.data.tm.ReopenCaseRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.http.comet.CometRestClient;
import uk.gov.ons.census.fwmt.jobservice.service.GatewayCacheService;
import uk.gov.ons.census.fwmt.jobservice.service.converter.spg.SpgUpdateConverter;
import uk.gov.ons.census.fwmt.jobservice.service.processor.InboundProcessor;
import uk.gov.ons.census.fwmt.jobservice.service.processor.ProcessorKey;
import uk.gov.ons.census.fwmt.jobservice.service.routing.RoutingValidator;

import java.time.Instant;

import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_UPDATE_ACK;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_UPDATE_PRE_SENDING;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.FAILED_TO_UPDATE_TM_JOB;

@Qualifier("Update")
@Service
public class SpgUpdateSiteProcessor implements InboundProcessor<FwmtActionInstruction> {

  private static final ProcessorKey key = ProcessorKey.builder()
      .actionInstruction(ActionInstructionType.UPDATE.toString())
      .surveyName("CENSUS")
      .addressType("SPG")
      .addressLevel("E")
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
      return rmRequest.getActionInstruction() == ActionInstructionType.UPDATE
          && rmRequest.getSurveyName().equals("CENSUS")
          && rmRequest.getAddressType().equals("SPG")
          && rmRequest.getAddressLevel().equals("E")
          && cache != null
          && cache.existsInFwmt;
    } catch (NullPointerException e) {
      return false;
    }
  }

  @Override
  public void process(FwmtActionInstruction rmRequest, GatewayCache cache, Instant messageReceivedTime) throws GatewayException {
    eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), COMET_UPDATE_PRE_SENDING,
        "Case Ref", rmRequest.getCaseRef());

    ReopenCaseRequest tmRequest = SpgUpdateConverter.convertSite(rmRequest, cache);
    ResponseEntity<Void> response = cometRestClient.sendReopen(tmRequest, rmRequest.getCaseId());
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

    eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), COMET_UPDATE_ACK,
        "Case Ref", rmRequest.getCaseRef(),
        "Response Code", response.getStatusCode().name(),
        "UAA", tmRequest.getUaa().toString(),
        "Blank Q", tmRequest.getBlank().toString(),
        "SPG Update Site", tmRequest.toString());
  }
}
