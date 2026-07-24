package uk.gov.ons.census.fwmt.jobservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCaseRecord;
import uk.gov.ons.census.fwmt.jobservice.messaging.RmFieldMessagePublisher;
import uk.gov.ons.census.fwmt.jobservice.service.processor.InboundProcessor;
import uk.gov.ons.census.fwmt.jobservice.service.processor.ProcessorKey;
import uk.gov.ons.census.fwmt.jobservice.service.processor.ProcessorRouter;
import uk.gov.ons.census.fwmt.jobservice.service.routing.ignore.CeUpdateIgnoreProcessor;

import java.time.Instant;
import java.util.Optional;

import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.CONVERT_SPG_UNIT_UPDATE_TO_CREATE;

@Service
public class UpdateActionOrchestrator {

  @Autowired
  private GatewayCaseRecordService cacheService;

  @Autowired
  private TmDispatchService tmDispatchService;

  @Autowired
  private RmFieldMessagePublisher rmFieldPublisher;

  @Autowired
  private CeUpdateIgnoreProcessor ceUpdateIgnoreProcessor;

  @Autowired
  private GatewayEventManager eventManager;

  @Autowired
  @Qualifier("UpdateProcessorRouter")
  private ProcessorRouter<FwmtActionInstruction> updateRouter;

  public void process(FwmtActionInstruction actionInstruction, Instant messageReceivedTime)
      throws GatewayException {
    GatewayCaseRecord cache = cacheService.getById(actionInstruction.getCaseId());
    if (shouldRepublishUpdateAsCreate(actionInstruction, cache)) {
      republishUpdateAsCreate(actionInstruction);
      return;
    }

    Optional<InboundProcessor<FwmtActionInstruction>> actionTypeHandler =
        updateRouter.resolveOptional(ProcessorKey.buildKey(actionInstruction), actionInstruction, cache);

    if (actionTypeHandler.isPresent()) {
      tmDispatchService.dispatch(actionInstruction, actionTypeHandler.get(), cache, messageReceivedTime);
    } else if (cache == null || hasHeldActionInstruction(cache)) {
      tmDispatchService.dispatch(actionInstruction, null, cache, messageReceivedTime);
    } else if (isCeUpdate(actionInstruction)) {
      ceUpdateIgnoreProcessor.process(actionInstruction);
    } else {
      updateRouter.throwRoutingError(actionInstruction, cache);
    }
  }

  private boolean shouldRepublishUpdateAsCreate(FwmtActionInstruction actionInstruction,
      GatewayCaseRecord cache) {
    return cache == null
        && actionInstruction.isUndeliveredAsAddress()
        && ("HH".equals(actionInstruction.getAddressType())
        || "SPG".equals(actionInstruction.getAddressType()));
  }

  private void republishUpdateAsCreate(FwmtActionInstruction actionInstruction) {
    actionInstruction.setActionInstruction(ActionInstructionType.CREATE);
    eventManager.triggerEvent(String.valueOf(actionInstruction.getCaseId()),
        CONVERT_SPG_UNIT_UPDATE_TO_CREATE,
        "Case Ref", actionInstruction.getCaseRef());
    rmFieldPublisher.publish(actionInstruction);
  }

  private boolean hasHeldActionInstruction(GatewayCaseRecord cache) {
    return "UPDATE(HELD)".equals(cache.getLastActionInstruction())
        || "CANCEL(HELD)".equals(cache.getLastActionInstruction());
  }

  private boolean isCeUpdate(FwmtActionInstruction actionInstruction) {
    return "UPDATE".equals(actionInstruction.getActionInstruction().toString())
        && "CE".equals(actionInstruction.getAddressType());
  }
}

