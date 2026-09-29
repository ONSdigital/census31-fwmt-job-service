package uk.gov.ons.census.fwmt.jobservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.dto.rm.ActionInstruction;
import uk.gov.ons.census.fwmt.common.dto.rm.CancelActionInstruction;
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

  public void dispatch(ActionInstruction actionInstruction,
      InboundProcessor<ActionInstruction> actionTypeHandler,
      GatewayCaseRecord cache,
      Instant messageReceivedTime) throws GatewayException {
    TransitionAction<ActionInstruction> transitionAction = transitioner.resolveTransitionAction(actionInstruction, actionTypeHandler, cache, messageReceivedTime);
    transitioner.apply(transitionAction);
  }

  public void dispatch(CancelActionInstruction actionInstruction,
      InboundProcessor<CancelActionInstruction> actionTypeHandler,
      GatewayCaseRecord cache,
      Instant messageReceivedTime) throws GatewayException {
    TransitionAction<CancelActionInstruction> transitionAction = transitioner.resolveTransitionAction(actionInstruction, actionTypeHandler, cache, messageReceivedTime);
    transitioner.apply(transitionAction);
  }
}

