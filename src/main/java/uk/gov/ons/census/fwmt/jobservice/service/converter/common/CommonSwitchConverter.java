package uk.gov.ons.census.fwmt.jobservice.service.converter.common;

import uk.gov.ons.census.fwmt.common.data.tm.ReopenCaseRequest;
import uk.gov.ons.census.fwmt.common.data.tm.SurveyType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;

public final class CommonSwitchConverter {

  private CommonSwitchConverter() {
  }

  private static ReopenCaseRequest.ReopenCaseRequestBuilder convertCommon(FwmtActionInstruction ffu) {
    return ReopenCaseRequest.builder().id(ffu.getCaseId());
  }

  public static ReopenCaseRequest convertEstabDeliver(FwmtActionInstruction ffu) {
    return CommonSwitchConverter.convertCommon(ffu)
        .surveyType(SurveyType.CE_EST_D)
        .uaa(ffu.isUndeliveredAsAddress())
        .blank(ffu.isBlankFormReturned())
        .build();
  }

  public static ReopenCaseRequest converEstabFollowup(FwmtActionInstruction ffu) {
    return CommonSwitchConverter.convertCommon(ffu)
        .surveyType(SurveyType.CE_EST_F)
        .uaa(ffu.isUndeliveredAsAddress())
        .blank(ffu.isBlankFormReturned())
        .build();
  }

  public static ReopenCaseRequest convertSite(FwmtActionInstruction ffu) {
    return CommonSwitchConverter.convertCommon(ffu)
        .surveyType(SurveyType.CE_SITE)
        .build();
  }

  public static ReopenCaseRequest convertUnitDeliver(FwmtActionInstruction ffu) {
    return CommonSwitchConverter.convertCommon(ffu)
        .surveyType(SurveyType.CE_UNIT_D)
        .uaa(ffu.isUndeliveredAsAddress())
        .blank(ffu.isBlankFormReturned())
        .build();
  }

  public static ReopenCaseRequest converUnitFollowup(FwmtActionInstruction ffu) {
    return CommonSwitchConverter.convertCommon(ffu)
        .surveyType(SurveyType.CE_UNIT_F)
        .uaa(ffu.isUndeliveredAsAddress())
        .blank(ffu.isBlankFormReturned())
        .build();
  }
}

