package uk.gov.ons.census.fwmt.jobservice.service.converter.spg;

import uk.gov.ons.census.fwmt.common.data.tm.ReopenCaseRequest;
import uk.gov.ons.census.fwmt.common.data.tm.SurveyType;
import uk.gov.ons.census.fwmt.common.dto.rm.ActionInstruction;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCaseRecord;

public final class SpgUpdateConverter {

  private SpgUpdateConverter() {
  }

  private static ReopenCaseRequest.ReopenCaseRequestBuilder convertCommon(ActionInstruction actionInstruction,
      GatewayCaseRecord cache) {
    return ReopenCaseRequest.builder().id(actionInstruction.getCaseId()).uaa(actionInstruction.isUndeliveredAsAddress())
        .blank(actionInstruction.isBlankFormReturned());
  }

  public static ReopenCaseRequest convertSite(ActionInstruction actionInstruction, GatewayCaseRecord cache) {
    return SpgUpdateConverter.convertCommon(actionInstruction, cache).build();
  }

  public static ReopenCaseRequest convertUnit(ActionInstruction actionInstruction, GatewayCaseRecord cache) {
    return SpgUpdateConverter.convertCommon(actionInstruction, cache)
        .surveyType(SurveyType.SPG_Unit_F)
        .uaa(actionInstruction.isUndeliveredAsAddress())
        .blank(actionInstruction.isBlankFormReturned())
        .build();
  }
}

