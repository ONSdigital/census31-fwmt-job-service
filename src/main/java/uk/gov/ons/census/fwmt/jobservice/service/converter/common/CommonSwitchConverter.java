package uk.gov.ons.census.fwmt.jobservice.service.converter.common;

import uk.gov.ons.census.fwmt.common.data.tm.ReopenCaseRequest;
import uk.gov.ons.census.fwmt.common.data.tm.SurveyType;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstruction;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCaseRecord;

public final class CommonSwitchConverter {

  private CommonSwitchConverter() {
  }

  private static ReopenCaseRequest.ReopenCaseRequestBuilder convertCommon(ActionInstruction ffu) {
    return ReopenCaseRequest.builder().id(ffu.getCaseId());
  }

  public static ReopenCaseRequest convertEstabDeliver(ActionInstruction ffu) {
    return CommonSwitchConverter.convertCommon(ffu)
        .surveyType(SurveyType.CE_EST_D)
        .uaa(ffu.isUndeliveredAsAddress())
        .blank(ffu.isBlankFormReturned())
        .build();
  }

  public static ReopenCaseRequest converEstabFollowup(ActionInstruction ffu) {
    return CommonSwitchConverter.convertCommon(ffu)
        .surveyType(SurveyType.CE_EST)
        .uaa(ffu.isUndeliveredAsAddress())
        .blank(ffu.isBlankFormReturned())
        .build();
  }

  public static ReopenCaseRequest convertSite(ActionInstruction ffu) {
    return CommonSwitchConverter.convertCommon(ffu)
        .surveyType(SurveyType.CE_SITE)
        .build();
  }

  public static ReopenCaseRequest convertUnitDeliver(ActionInstruction ffu) {
    return CommonSwitchConverter.convertCommon(ffu)
        .surveyType(SurveyType.CE_UNIT_D)
        .uaa(ffu.isUndeliveredAsAddress())
        .blank(ffu.isBlankFormReturned())
        .build();
  }

  public static ReopenCaseRequest converUnitFollowup(ActionInstruction ffu) {
    return CommonSwitchConverter.convertCommon(ffu)
        .surveyType(SurveyType.CE_UNIT_F)
        .uaa(ffu.isUndeliveredAsAddress())
        .blank(ffu.isBlankFormReturned())
        .build();
  }
}

