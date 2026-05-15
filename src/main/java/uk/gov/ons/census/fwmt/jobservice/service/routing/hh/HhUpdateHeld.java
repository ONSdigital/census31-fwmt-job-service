package uk.gov.ons.census.fwmt.jobservice.service.routing.hh;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.service.processor.InboundProcessor;
import uk.gov.ons.census.fwmt.jobservice.service.processor.ProcessorKey;

import java.time.Instant;

@Qualifier("Update")
@Service
public class HhUpdateHeld implements InboundProcessor<FwmtActionInstruction> {

  private static final String HH_UPDATE_HELD = "HH_UPDATE_HELD";

  @Autowired
  private GatewayEventManager eventManager;

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
          && (cache == null
          || !cache.existsInFwmt);
    } catch (NullPointerException e) {
      return false;
    }
  }

  @Override
  public void process(FwmtActionInstruction rmRequest, GatewayCache cache, Instant messageReceivedTime) throws GatewayException {
    eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), HH_UPDATE_HELD,
        "A HH Update was received before a create. Update held.");
  }
}
