package uk.gov.ons.census.fwmt.jobservice.service.routing.ccs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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
import uk.gov.ons.census.fwmt.jobservice.service.converter.ccs.CcsInterviewCreateConverter;
import uk.gov.ons.census.fwmt.jobservice.service.processor.InboundProcessor;
import uk.gov.ons.census.fwmt.jobservice.service.processor.ProcessorKey;
import uk.gov.ons.census.fwmt.jobservice.service.routing.RoutingValidator;

import java.time.Instant;

import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_CREATE_ACK;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_CREATE_PRE_SENDING;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.FAILED_TO_CREATE_TM_JOB;

@Qualifier("Create")
@Service
public class CcsInterviewCECreate implements InboundProcessor<FwmtActionInstruction> {

  private static final ProcessorKey key = ProcessorKey.builder()
      .actionInstruction(ActionInstructionType.CREATE.toString())
      .surveyName("CCS")
      .addressType("CE")
      .addressLevel("E")
      .build();

  @Value("${eq.url}")
  private String eqUrl;

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
      return rmRequest.getActionInstruction() == ActionInstructionType.CREATE
          && rmRequest.getSurveyName().equals("CCS")
          && rmRequest.getAddressType().equals("CE")
          && rmRequest.getAddressLevel().equals("E")
          && cache!=null;
    } catch (NullPointerException e) {
      return false;
    }
  }

  @Override
  public void process(FwmtActionInstruction rmRequest, GatewayCache cache, Instant messageReceivedTime)
      throws GatewayException {
    CaseRequest tmRequest = CcsInterviewCreateConverter.convertCcsInterview(rmRequest, cache, eqUrl);

    eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), COMET_CREATE_PRE_SENDING,
        "Case Ref", tmRequest.getReference(),
        "Survey Type", tmRequest.getSurveyType().toString());

    ResponseEntity<Void> response = cometRestClient.sendCreate(tmRequest, rmRequest.getCaseId());
    routingValidator.validateResponseCode(response, rmRequest.getCaseId(), "Create", FAILED_TO_CREATE_TM_JOB);

    GatewayCache newCache = cacheService.getById(rmRequest.getCaseId());
    if (newCache != null) {
      cacheService.save(newCache.toBuilder()
          .caseId(rmRequest.getCaseId())
          .existsInFwmt(true)
          .lastActionInstruction(rmRequest.getActionInstruction().toString())
          .lastActionTime(messageReceivedTime)
          .build());
    }

    eventManager
        .triggerEvent(String.valueOf(rmRequest.getCaseId()), COMET_CREATE_ACK,
            "Case Ref", rmRequest.getCaseRef(),
            "CCS Int CE Create", tmRequest.toString(),
            "Response Code", response.getStatusCode().name(),
            "Survey Type", tmRequest.getSurveyType().toString());
  }
}
