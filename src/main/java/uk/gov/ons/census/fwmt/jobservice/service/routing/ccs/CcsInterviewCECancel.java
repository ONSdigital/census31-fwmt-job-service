package uk.gov.ons.census.fwmt.jobservice.service.routing.ccs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.http.comet.CometRestClient;
import uk.gov.ons.census.fwmt.jobservice.service.processor.InboundProcessor;
import uk.gov.ons.census.fwmt.jobservice.service.processor.ProcessorKey;
import uk.gov.ons.census.fwmt.jobservice.service.routing.RoutingValidator;

import java.time.Instant;

import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.CANCEL_ON_A_CANCEL;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_CANCEL_ACK;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_CANCEL_PRE_SENDING;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.FAILED_TO_CANCEL_TM_JOB;

@Qualifier("Cancel")
@Service
public class CcsInterviewCECancel implements InboundProcessor<FwmtCancelActionInstruction> {

  private static final ProcessorKey key = ProcessorKey.builder()
      .actionInstruction(ActionInstructionType.CANCEL.toString())
      .surveyName("CCS")
      .addressType("CE")
      .addressLevel("E")
      .build();

  @Autowired
  private CometRestClient cometRestClient;

  @Autowired
  private GatewayEventManager eventManager;

  @Autowired
  private RoutingValidator routingValidator;

  @Override
  public ProcessorKey getKey() {
    return key;
  }

  @Override
  public boolean isValid(FwmtCancelActionInstruction rmRequest, GatewayCache cache) {
    try {
      return rmRequest.getActionInstruction() == ActionInstructionType.CANCEL
          && rmRequest.getSurveyName().equals("CCS")
          && rmRequest.getAddressType().equals("CE")
          && rmRequest.getAddressLevel().equals("E")
          && cache != null;
    } catch (NullPointerException e) {
      return false;
    }
  }

  @Override
  public void process(FwmtCancelActionInstruction rmRequest, GatewayCache cache, Instant messageReceivedTime)
      throws GatewayException {

    eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), COMET_CANCEL_PRE_SENDING,
        "Case Ref", "N/A",
        "TM Action", "CLOSE");

    try {
      ResponseEntity<Void> response = cometRestClient.sendClose(rmRequest.getCaseId());
      routingValidator.validateResponseCode(response, rmRequest.getCaseId(), "Cancel", FAILED_TO_CANCEL_TM_JOB);

      eventManager
          .triggerEvent(String.valueOf(rmRequest.getCaseId()), COMET_CANCEL_ACK,
              "Case Ref", "N/A",
              "Response Code", response.getStatusCode().name());
    } catch (RestClientException e) {
      String tmResponse = e.getMessage();
      if (tmResponse != null && tmResponse.contains("400") && tmResponse.contains("Case State must be Open")) {
        eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), CANCEL_ON_A_CANCEL,
            "A cancel case has been received for a case that already has been cancelled",
            "Message received: " + rmRequest.toString());
      } else {
        throw e;
      }
    }
  }
}
