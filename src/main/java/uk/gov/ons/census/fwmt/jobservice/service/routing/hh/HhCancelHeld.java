package uk.gov.ons.census.fwmt.jobservice.service.routing.hh;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.service.processor.InboundProcessor;
import uk.gov.ons.census.fwmt.jobservice.service.processor.ProcessorKey;

import java.time.Instant;

@Qualifier("Cancel")
@Service
public class HhCancelHeld implements InboundProcessor<FwmtCancelActionInstruction> {

  private static final String HH_CANCEL_HELD = "HH_CANCEL_HELD";

  public static final String COMET_CANCEL_PRE_SENDING = "COMET_CANCEL_PRE_SENDING";

  public static final String COMET_CANCEL_ACK = "COMET_CANCEL_ACK";

  private static final ProcessorKey key = ProcessorKey.builder()
      .actionInstruction(ActionInstructionType.CANCEL.toString())
      .surveyName("CENSUS")
      .addressType("HH")
      .addressLevel("U")
      .build();

  @Autowired
  private GatewayEventManager eventManager;

  @Override
  public ProcessorKey getKey() {
    return key;
  }

  @Override public boolean isValid(FwmtCancelActionInstruction rmRequest, GatewayCache cache) {
    try {
      return rmRequest.getActionInstruction() == ActionInstructionType.CANCEL
          && rmRequest.getSurveyName().equals("CENSUS")
          && rmRequest.getAddressType().equals("HH")
          && rmRequest.getAddressLevel().equals("U")
          && !rmRequest.isNc()
          && (cache == null
          || !cache.existsInFwmt);
    } catch (NullPointerException e) {
      return false;
    }
  }

  @Override public void process(FwmtCancelActionInstruction rmRequest, GatewayCache cache, Instant messageReceivedTime)
      throws GatewayException {

    eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), HH_CANCEL_HELD,
        "A HH Cancel was received before a create. Create does not exist in cache and so will be held.");
  }
}
