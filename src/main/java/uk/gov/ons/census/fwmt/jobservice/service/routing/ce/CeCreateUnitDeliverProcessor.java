package uk.gov.ons.census.fwmt.jobservice.service.routing.ce;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.common.data.tm.CaseRequest;
import uk.gov.ons.census.fwmt.common.data.tm.SurveyType;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.http.comet.CometRestClient;
import uk.gov.ons.census.fwmt.jobservice.rabbit.RmFieldPublisher;
import uk.gov.ons.census.fwmt.jobservice.service.GatewayCacheService;
import uk.gov.ons.census.fwmt.jobservice.service.converter.ce.CeCreateConverter;
import uk.gov.ons.census.fwmt.jobservice.service.processor.InboundProcessor;
import uk.gov.ons.census.fwmt.jobservice.service.processor.ProcessorKey;
import uk.gov.ons.census.fwmt.jobservice.service.routing.RoutingValidator;

import java.time.Instant;

import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_CREATE_ACK;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_CREATE_PRE_SENDING;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.FAILED_TO_CREATE_TM_JOB;

@Qualifier("Create")
@Service
public class CeCreateUnitDeliverProcessor implements InboundProcessor<FwmtActionInstruction> {

  @Autowired
  private CometRestClient cometRestClient;

  @Autowired
  private GatewayEventManager eventManager;

  @Autowired
  private RoutingValidator routingValidator;

  @Autowired
  private GatewayCacheService cacheService;

  @Autowired
  private RmFieldPublisher rmFieldPublisher;

  private static ProcessorKey key = ProcessorKey.builder()
      .actionInstruction(ActionInstructionType.CREATE.toString())
      .surveyName("CENSUS")
      .addressType("CE")
      .addressLevel("U")
      .build();

  @Override
  public ProcessorKey getKey() {
    return key;
  }

  @Override
  public boolean isValid(FwmtActionInstruction rmRequest, GatewayCache cache) {
    try {
      return rmRequest.getActionInstruction() == ActionInstructionType.CREATE
          && rmRequest.getSurveyName().equals("CENSUS")
          && rmRequest.getAddressType().equals("CE")
          && rmRequest.getAddressLevel().equals("U")
          && ((cache == null
          && rmRequest.isHandDeliver())
          || (cache != null
          && !cache.existsInFwmt
          && !cache.isDelivered()));
    } catch (NullPointerException e) {
      return false;
    }
  }

  @Override
  public void process(FwmtActionInstruction rmRequest, GatewayCache cache, Instant messageReceivedTime) throws GatewayException {
    CaseRequest tmRequest;

    if (cacheService.doesUprnAndTypeExist(rmRequest.getEstabUprn(), 1)) {
      FwmtActionInstruction ceSwitch = new FwmtActionInstruction();

      ceSwitch.setActionInstruction(ActionInstructionType.SWITCH_CE_TYPE);
      ceSwitch.setSurveyName("CENSUS");
      ceSwitch.setAddressType("CE");
      ceSwitch.setAddressLevel(null);
      ceSwitch.setCaseId(cacheService.getEstabCaseId(rmRequest.getEstabUprn()));
      ceSwitch.setSurveyType(SurveyType.CE_SITE);

      rmFieldPublisher.publish(ceSwitch);
    }

    if (rmRequest.isSecureEstablishment()) {
      tmRequest = CeCreateConverter.convertCeUnitDeliverSecure(rmRequest, cache);
    } else {
      tmRequest = CeCreateConverter.convertCeUnitDeliver(rmRequest, cache);
    }

    eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), COMET_CREATE_PRE_SENDING, "Case Ref", tmRequest.getReference(), "Survey Type",
        tmRequest.getSurveyType().toString());

    ResponseEntity<Void> response = cometRestClient.sendCreate(tmRequest, rmRequest.getCaseId());
    routingValidator.validateResponseCode(response, rmRequest.getCaseId(), "Create", FAILED_TO_CREATE_TM_JOB, "tmRequest", tmRequest.toString(), "rmRequest", rmRequest.toString(), "cache", (cache!=null)?cache.toString():"");

    GatewayCache newCache = cacheService.getById(rmRequest.getCaseId());
    if (newCache == null) {
      cacheService.save(GatewayCache.builder().caseId(rmRequest.getCaseId()).existsInFwmt(true)
          .uprn(rmRequest.getUprn()).estabUprn(rmRequest.getEstabUprn()).type(3).lastActionInstruction(rmRequest.getActionInstruction().toString())
          .lastActionTime(messageReceivedTime).build());
    } else {
      cacheService.save(newCache.toBuilder().existsInFwmt(true).uprn(rmRequest.getUprn()).estabUprn(rmRequest.getEstabUprn())
          .type(3).lastActionInstruction(rmRequest.getActionInstruction().toString())
          .lastActionTime(messageReceivedTime).build());
    }

    eventManager
        .triggerEvent(String.valueOf(rmRequest.getCaseId()), COMET_CREATE_ACK,
            "Case Ref", rmRequest.getCaseRef(),
            "CE Create Unit Delivered", tmRequest.toString(),
            "Response Code", response.getStatusCode().name(),
            "Survey Type", tmRequest.getSurveyType().toString());

  }
}
