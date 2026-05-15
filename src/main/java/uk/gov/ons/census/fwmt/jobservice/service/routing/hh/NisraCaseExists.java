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

@Qualifier("Create")
@Service
public class NisraCaseExists implements InboundProcessor<FwmtActionInstruction> {

  final private static String NISRA_CASE_EXISTS = "NISRA_CASE_EXISTS";

  private static final ProcessorKey key = ProcessorKey.builder()
      .actionInstruction(ActionInstructionType.CREATE.toString())
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
      return rmRequest.getActionInstruction() == ActionInstructionType.CREATE
          && rmRequest.getSurveyName().equals("CENSUS")
          && rmRequest.getAddressType().equals("HH")
          && rmRequest.getOa().startsWith("N")
          && (cache != null && cache.existsInFwmt);
    } catch (NullPointerException e) {
      return false;
    }
  }

  @Override
  public void process(FwmtActionInstruction rmRequest, GatewayCache cache, Instant messageReceivedTime) throws GatewayException {
    eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), NISRA_CASE_EXISTS,
        "UPRN", rmRequest.getUprn(),
        "Estab UPRN", rmRequest.getEstabUprn());
  }
}
