package uk.gov.ons.census.fwmt.jobservice.service.converter.hh;

import uk.gov.ons.census.fwmt.common.data.tm.CasePauseRequest;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstruction;

public final class HhPauseConverter {

  private HhPauseConverter() {
  }

  public static CasePauseRequest buildPause(ActionInstruction actionInstruction) {
    return CasePauseRequest.builder()
        .code(actionInstruction.getPauseCode())
        .effectiveFrom(actionInstruction.getPauseFrom().toString())
        .reason(actionInstruction.getPauseReason())
        .build();
  }
}
