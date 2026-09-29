package uk.gov.ons.census.fwmt.jobservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.dto.rm.ActionInstruction;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCaseRecord;
import uk.gov.ons.census.fwmt.jobservice.service.processor.InboundProcessor;
import uk.gov.ons.census.fwmt.jobservice.service.processor.ProcessorKey;
import uk.gov.ons.census.fwmt.jobservice.service.processor.ProcessorRouter;

import java.time.Instant;

@Service
public class PauseActionOrchestrator {

  @Autowired
  private GatewayCaseRecordService cacheService;

  @Autowired
  @Qualifier("PauseProcessorRouter")
  private ProcessorRouter<ActionInstruction> pauseRouter;

  public void process(ActionInstruction actionInstruction, Instant messageReceivedTime)
      throws GatewayException {
    GatewayCaseRecord cache = cacheService.getById(actionInstruction.getCaseId());
    InboundProcessor<ActionInstruction> actionTypeHandler = pauseRouter.resolveExactlyOne(
        ProcessorKey.buildKey(actionInstruction), actionInstruction, cache);
    actionTypeHandler.process(actionInstruction, cache, messageReceivedTime);
  }
}

