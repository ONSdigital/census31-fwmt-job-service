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

@Qualifier("Pause")
@Service
public class HHCancelledPause implements InboundProcessor<FwmtActionInstruction> {

  private static final String CASE_ALREADY_CANCELLED = "CASE_ALREADY_CANCELLED";

  private static final String IGNORED_PAUSE_HH = "IGNORED_PAUSE_HH";

  private static final ProcessorKey key = ProcessorKey.builder()
      .actionInstruction(ActionInstructionType.PAUSE.toString())
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

  @Override
  public boolean isValid(FwmtActionInstruction rmRequest, GatewayCache cache) {
    try {
      return rmRequest.getActionInstruction() == ActionInstructionType.PAUSE
          && rmRequest.getSurveyName().equals("CENSUS")
          && rmRequest.getAddressType().equals("HH")
          && rmRequest.getAddressLevel().equals("U")
          && (cache != null && cache.existsInFwmt && cache.lastActionInstruction.equals("CANCEL"));
    } catch (NullPointerException e) {
      return false;
    }
  }

  @Override
  public void process(FwmtActionInstruction rmRequest, GatewayCache cache, Instant messageReceivedTime) throws GatewayException {
    eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), CASE_ALREADY_CANCELLED,
        "Type", "HH Pause Case",
        "Action", IGNORED_PAUSE_HH);
  }
}
