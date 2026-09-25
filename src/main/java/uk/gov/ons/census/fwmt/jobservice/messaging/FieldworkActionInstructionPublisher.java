package uk.gov.ons.census.fwmt.jobservice.messaging;

import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction;

/**
 * Port for publishing FWMT-owned action instructions to the internal topic.
 */
public interface FieldworkActionInstructionPublisher {

  void publish(FwmtActionInstruction actionInstruction);

  void publish(FwmtCancelActionInstruction cancelActionInstruction);
}