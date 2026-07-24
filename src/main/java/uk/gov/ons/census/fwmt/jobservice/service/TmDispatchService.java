package uk.gov.ons.census.fwmt.jobservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCaseRecord;
import uk.gov.ons.census.fwmt.jobservice.service.processor.InboundProcessor;
import uk.gov.ons.census.fwmt.jobservice.transition.TransitionAction;
import uk.gov.ons.census.fwmt.jobservice.transition.Transitioner;

import java.time.Instant;

/**
 * Facade for planning and dispatching TM actions from RM instructions.
 */
@Service
public class TmDispatchService {

  @Autowired
  private Transitioner transitioner;

  public void dispatch(FwmtActionInstruction actionInstruction,
      InboundProcessor<FwmtActionInstruction> actionTypeHandler,
      GatewayCaseRecord cache,
      Instant messageReceivedTime) throws GatewayException {
    TransitionAction<FwmtActionInstruction> transitionAction = transitioner.resolveTransitionAction(actionInstruction, actionTypeHandler, cache, messageReceivedTime);
    transitioner.apply(transitionAction);
  }

  public void dispatch(FwmtCancelActionInstruction actionInstruction,
      InboundProcessor<FwmtCancelActionInstruction> actionTypeHandler,
      GatewayCaseRecord cache,
      Instant messageReceivedTime) throws GatewayException {
    TransitionAction<FwmtCancelActionInstruction> transitionAction = transitioner.resolveTransitionAction(actionInstruction, actionTypeHandler, cache, messageReceivedTime);
    transitioner.apply(transitionAction);
  }
}

