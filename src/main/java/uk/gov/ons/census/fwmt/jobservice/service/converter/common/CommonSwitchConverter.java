package uk.gov.ons.census.fwmt.jobservice.service.converter.common;

import uk.gov.ons.census.fwmt.common.data.tm.ReopenCaseRequest;
import uk.gov.ons.census.fwmt.common.data.tm.SurveyType;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstruction;

public final class CommonSwitchConverter {

  private CommonSwitchConverter() {
  }

  private static ReopenCaseRequest.ReopenCaseRequestBuilder convertCommon(ActionInstruction actionInstruction) {
    return ReopenCaseRequest.builder().id(actionInstruction.getCaseId());
  }

  public static ReopenCaseRequest convertEstabDeliver(ActionInstruction actionInstruction) {
    return CommonSwitchConverter.convertCommon(actionInstruction)
        .surveyType(SurveyType.CE_EST_D)
        .uaa(actionInstruction.isUndeliveredAsAddress())
        .blank(actionInstruction.isBlankFormReturned())
        .build();
  }

  public static ReopenCaseRequest converEstabFollowup(ActionInstruction actionInstruction) {
    return CommonSwitchConverter.convertCommon(actionInstruction)
        .surveyType(SurveyType.CE_EST)
        .uaa(actionInstruction.isUndeliveredAsAddress())
        .blank(actionInstruction.isBlankFormReturned())
        .build();
  }

  public static ReopenCaseRequest convertSite(ActionInstruction actionInstruction) {
    return CommonSwitchConverter.convertCommon(actionInstruction)
        .surveyType(SurveyType.CE_SITE)
        .build();
  }

  public static ReopenCaseRequest convertUnitDeliver(ActionInstruction actionInstruction) {
    return CommonSwitchConverter.convertCommon(actionInstruction)
        .surveyType(SurveyType.CE_UNIT_D)
        .uaa(actionInstruction.isUndeliveredAsAddress())
        .blank(actionInstruction.isBlankFormReturned())
        .build();
  }

  public static ReopenCaseRequest converUnitFollowup(ActionInstruction actionInstruction) {
    return CommonSwitchConverter.convertCommon(actionInstruction)
        .surveyType(SurveyType.CE_UNIT_F)
        .uaa(actionInstruction.isUndeliveredAsAddress())
        .blank(actionInstruction.isBlankFormReturned())
        .build();
  }
}

