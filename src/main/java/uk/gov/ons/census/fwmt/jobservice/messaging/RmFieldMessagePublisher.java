package uk.gov.ons.census.fwmt.jobservice.messaging;

import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction;

/**
 * Port for publishing RM Field instructions (queue/topic name from config).
 */
public interface RmFieldMessagePublisher {

  void publish(FwmtActionInstruction actionInstruction);

  void publish(FwmtCancelActionInstruction cancelActionInstruction);
}
