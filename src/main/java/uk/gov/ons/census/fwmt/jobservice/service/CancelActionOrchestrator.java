package uk.gov.ons.census.fwmt.jobservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCaseRecord;
import uk.gov.ons.census.fwmt.jobservice.service.processor.InboundProcessor;
import uk.gov.ons.census.fwmt.jobservice.service.processor.ProcessorKey;
import uk.gov.ons.census.fwmt.jobservice.service.processor.ProcessorRouter;

import java.time.Instant;
import java.util.Optional;

@Service
public class CancelActionOrchestrator {

  @Autowired
  private GatewayCaseRecordService cacheService;

  @Autowired
  private TmDispatchService tmDispatchService;

  @Autowired
  @Qualifier("CancelProcessorRouter")
  private ProcessorRouter<FwmtCancelActionInstruction> cancelRouter;

  public void process(FwmtCancelActionInstruction actionInstruction, Instant messageReceivedTime)
      throws GatewayException {
    GatewayCaseRecord cache = resolveCancelCache(actionInstruction);

    Optional<InboundProcessor<FwmtCancelActionInstruction>> actionTypeHandler = cancelRouter.resolveOptional(
        ProcessorKey.buildKey(actionInstruction), actionInstruction, cache);

    if (actionTypeHandler.isPresent()) {
      tmDispatchService.dispatch(actionInstruction, actionTypeHandler.get(), cache, messageReceivedTime);
    } else if (cache == null || hasCancelHeldInstruction(cache)) {
      tmDispatchService.dispatch(actionInstruction, null, cache, messageReceivedTime);
    } else {
      cancelRouter.throwRoutingError(actionInstruction, cache);
    }
  }

  private boolean hasCancelHeldInstruction(GatewayCaseRecord cache) {
    return "CANCEL(HELD)".equals(cache.getLastActionInstruction());
  }

  private GatewayCaseRecord resolveCancelCache(FwmtCancelActionInstruction actionInstruction) {
    GatewayCaseRecord cache = cacheService.getByOriginalCaseId(actionInstruction.getCaseId());
    if (cache != null) {
      actionInstruction.setNc(true);
      return cache;
    }
    return cacheService.getById(actionInstruction.getCaseId());
  }
}

