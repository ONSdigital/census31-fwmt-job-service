package uk.gov.ons.census.fwmt.jobservice.service.routing.ce;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import uk.gov.ons.census.fwmt.common.data.tm.ReopenCaseRequest;
import uk.gov.ons.census.fwmt.common.data.tm.SurveyType;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.http.comet.CometRestClient;
import uk.gov.ons.census.fwmt.jobservice.service.GatewayCacheService;
import uk.gov.ons.census.fwmt.jobservice.service.converter.common.CommonSwitchConverter;
import uk.gov.ons.census.fwmt.jobservice.service.processor.InboundProcessor;
import uk.gov.ons.census.fwmt.jobservice.service.processor.ProcessorKey;
import uk.gov.ons.census.fwmt.jobservice.service.routing.RoutingValidator;

import java.time.Instant;

import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.SWITCH_ON_A_CANCEL;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_CLOSE_ACK;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_CLOSE_PRE_SENDING;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_REOPEN_ACK;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_REOPEN_PRE_SENDING;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.FAILED_TO_CLOSE_TM_JOB;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.FAILED_TO_REOPEN_TM_JOB;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.INCORRECT_SWITCH_SURVEY_TYPE;

@Qualifier("Create")
@Service
public class CeSwitchCreateProcessor implements InboundProcessor<FwmtActionInstruction> {

  private static final ProcessorKey key = ProcessorKey.builder()
      .actionInstruction(ActionInstructionType.SWITCH_CE_TYPE.toString())
      .surveyName("CENSUS")
      .addressType("CE")
      .addressLevel(null)
      .build();

  private static final String PROCESSING_CE_SWITCH_CREATE = "PROCESSING_CE_SWITCH_CREATE";

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
      return rmRequest.getActionInstruction() == ActionInstructionType.SWITCH_CE_TYPE
          && rmRequest.getSurveyName().equals("CENSUS")
          && rmRequest.getAddressType().equals("CE")
          && rmRequest.getAddressLevel() == null;
    } catch (NullPointerException e) {
      return false;
    }
  }

  @Override
  public void process(FwmtActionInstruction rmRequest, GatewayCache cache, Instant messageReceivedTime) throws GatewayException {
    ReopenCaseRequest tmRequest;

    eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), PROCESSING_CE_SWITCH_CREATE);
    if (rmRequest.getSurveyType().equals(SurveyType.CE_EST_D)) {
      cache.setType(1);
      tmRequest = CommonSwitchConverter.convertEstabDeliver(rmRequest);
      processSwitch(cache, rmRequest, tmRequest);
    } else if (rmRequest.getSurveyType().equals(SurveyType.CE_EST_F)) {
      cache.setType(1);
      tmRequest = CommonSwitchConverter.converEstabFollowup(rmRequest);
      processSwitch(cache, rmRequest, tmRequest);
    } else if (rmRequest.getSurveyType().equals(SurveyType.CE_SITE) && (cache!=null) && cache.getType() != 2) {
      cache.setType(2);
      tmRequest = CommonSwitchConverter.convertSite(rmRequest);
      processSwitch(cache, rmRequest, tmRequest);
    } else if (rmRequest.getSurveyType().equals(SurveyType.CE_SITE) && (cache!=null) && cache.getType() == 2) {
      eventManager.triggerEvent(rmRequest.getCaseId(), "Case is already a site");
    } else if (rmRequest.getSurveyType().equals(SurveyType.CE_UNIT_D)) {
      cache.setType(3);
      tmRequest = CommonSwitchConverter.convertUnitDeliver(rmRequest);
      processSwitch(cache, rmRequest, tmRequest);
    } else if (rmRequest.getSurveyType().equals(SurveyType.CE_UNIT_F)) {
      cache.setType(3);
      tmRequest = CommonSwitchConverter.converUnitFollowup(rmRequest);
      processSwitch(cache, rmRequest, tmRequest);
    } else {
      eventManager.triggerErrorEvent(this.getClass(), "Not a recognised CE Switch SurveyType",
          String.valueOf(rmRequest.getCaseId()), INCORRECT_SWITCH_SURVEY_TYPE,
          "rmRequest", rmRequest.toString(),
          "cache", (cache!=null)?cache.toString():"");
      throw new GatewayException(GatewayException.Fault.SYSTEM_ERROR, "Incorrect CE Switch survey type");
    }
  }

  private void processSwitch(GatewayCache cache, FwmtActionInstruction rmRequest, ReopenCaseRequest tmRequest)
      throws GatewayException {

    boolean alreadyCancelled = false;
    ResponseEntity<Void> closeResponse = null;

    eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), COMET_CLOSE_PRE_SENDING,
        "Survey Type", rmRequest.getSurveyType().toString());
    try {
      closeResponse = cometRestClient.sendClose(rmRequest.getCaseId());
      routingValidator.validateResponseCode(closeResponse, rmRequest.getCaseId(), "Close", FAILED_TO_CLOSE_TM_JOB,
          "rmRequest", rmRequest.toString(),
          "cache", (cache!=null)?cache.toString():"");
    } catch (RestClientException e) {
      String tmResponse = e.getMessage();
      if (tmResponse != null && tmResponse.contains("400") && tmResponse.contains("Case State must be Open")){
        eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), SWITCH_ON_A_CANCEL,
            "A switch case has been received for a case that has already been cancelled",
            "Message received: " + rmRequest.toString());
        alreadyCancelled = true;
      } else {
        throw e;
      }
    }

    if (closeResponse != null && !alreadyCancelled) {
      eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), COMET_CLOSE_ACK,
          "Survey Type", tmRequest.getSurveyType().toString());

      eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), COMET_REOPEN_PRE_SENDING,
          "Survey Type", tmRequest.getSurveyType().toString());

      ResponseEntity<Void> reopenResponse = cometRestClient.sendReopen(tmRequest, rmRequest.getCaseId());
      routingValidator.validateResponseCode(reopenResponse, rmRequest.getCaseId(), "Reopen", FAILED_TO_REOPEN_TM_JOB,
          "tmRequest", tmRequest.toString(),
          "rmRequest", rmRequest.toString(),
          "cache", (cache != null) ? cache.toString() : "");

      if (cache != null) {
        if (rmRequest.getSurveyType().equals(SurveyType.CE_SITE)) {
          cacheService.save(cache.toBuilder().usualResidents(0).build());
        } else {
          cacheService.save(cache.toBuilder().build());
        }
      }

      eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), COMET_REOPEN_ACK,
          "Survey Type", tmRequest.getSurveyType().toString(),
          "CE Switch Create", tmRequest.toString());
    }
  }
}