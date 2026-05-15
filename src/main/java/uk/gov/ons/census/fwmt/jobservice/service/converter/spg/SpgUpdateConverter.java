package uk.gov.ons.census.fwmt.jobservice.service.converter.spg;

import uk.gov.ons.census.fwmt.common.data.tm.ReopenCaseRequest;
import uk.gov.ons.census.fwmt.common.data.tm.SurveyType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;

public final class SpgUpdateConverter {

  private SpgUpdateConverter() {
  }

  private static ReopenCaseRequest.ReopenCaseRequestBuilder convertCommon(FwmtActionInstruction ffu,
      GatewayCache cache) {
    return ReopenCaseRequest.builder().id(ffu.getCaseId()).uaa(ffu.isUndeliveredAsAddress())
        .blank(ffu.isBlankFormReturned());
  }

  public static ReopenCaseRequest convertSite(FwmtActionInstruction ffu, GatewayCache cache) {
    return SpgUpdateConverter.convertCommon(ffu, cache).build();
  }

  public static ReopenCaseRequest convertUnit(FwmtActionInstruction ffu, GatewayCache cache) {
    return SpgUpdateConverter.convertCommon(ffu, cache)
        .surveyType(SurveyType.SPG_Unit_F)
        .uaa(ffu.isUndeliveredAsAddress())
        .blank(ffu.isBlankFormReturned())
        .build();
  }
}

