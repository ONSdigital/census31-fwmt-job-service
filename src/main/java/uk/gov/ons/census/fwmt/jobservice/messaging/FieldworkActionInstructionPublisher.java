package uk.gov.ons.census.fwmt.jobservice.messaging;

import uk.gov.ons.census.fwmt.common.dto.rm.ActionInstruction;
import uk.gov.ons.census.fwmt.common.dto.rm.CancelActionInstruction;

/**
 * Port for publishing FWMT-owned action instructions to the internal topic.
 */
public interface FieldworkActionInstructionPublisher {

  void publish(ActionInstruction actionInstruction);

  void publish(CancelActionInstruction cancelActionInstruction);
}