package uk.gov.ons.census.fwmt.jobservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCaseRecord;
import uk.gov.ons.census.fwmt.jobservice.service.processor.InboundProcessor;
import uk.gov.ons.census.fwmt.jobservice.service.processor.ProcessorKey;
import uk.gov.ons.census.fwmt.jobservice.service.processor.ProcessorRouter;

import java.time.Instant;

@Service
public class CreateActionOrchestrator {

  @Autowired
  private GatewayCaseRecordService cacheService;

  @Autowired
  private TmDispatchService tmDispatchService;

  @Autowired
  @Qualifier("CreateProcessorRouter")
  private ProcessorRouter<FwmtActionInstruction> createRouter;

  public void process(FwmtActionInstruction actionInstruction, Instant messageReceivedTime) throws GatewayException {
    GatewayCaseRecord cache = cacheService.getById(actionInstruction.getCaseId());
    InboundProcessor<FwmtActionInstruction> actionTypeHandler = createRouter.resolveExactlyOne(ProcessorKey.buildKey(actionInstruction), actionInstruction, cache);
    tmDispatchService.dispatch(actionInstruction, actionTypeHandler, cache, messageReceivedTime);
  }
}

