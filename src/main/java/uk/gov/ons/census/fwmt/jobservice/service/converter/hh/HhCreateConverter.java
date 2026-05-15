package uk.gov.ons.census.fwmt.jobservice.service.converter.hh;

import uk.gov.ons.census.fwmt.common.data.tm.Address;
import uk.gov.ons.census.fwmt.common.data.tm.CaseRequest;
import uk.gov.ons.census.fwmt.common.data.tm.CaseType;
import uk.gov.ons.census.fwmt.common.data.tm.Geography;
import uk.gov.ons.census.fwmt.common.data.tm.SurveyType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.service.converter.common.CommonCreateConverter;

import java.util.List;
import java.util.Objects;

public final class HhCreateConverter {

  private HhCreateConverter() {
  }

  public static CaseRequest.CaseRequestBuilder convertHH(
      FwmtActionInstruction ffu, GatewayCache cache, CaseRequest.CaseRequestBuilder builder) {
    CaseRequest.CaseRequestBuilder commonBuilder = CommonCreateConverter.convertCommon(ffu, cache, builder);

    commonBuilder.type(CaseType.HH);
    commonBuilder.surveyType(SurveyType.HH);
    commonBuilder.category("HH");

    Geography outGeography = Geography.builder().oa(ffu.getOa()).build();

    Address outAddress = Address.builder()
        .lines(List.of(
            ffu.getAddressLine1(),
            Objects.toString(ffu.getAddressLine2(), ""),
            Objects.toString(ffu.getAddressLine3(), "")
        ))
        .town(ffu.getTownName())
        .postcode(ffu.getPostcode())
        .geography(outGeography)
        .build();
    commonBuilder.address(outAddress);

    return commonBuilder;
  }

  public static CaseRequest convertHhEnglandAndWales(FwmtActionInstruction ffu, GatewayCache cache) {
    return HhCreateConverter
        .convertHH(ffu, cache, CaseRequest.builder())
        .sai("Sheltered Accommodation".equals(ffu.getEstabType()))
        .blankFormReturned(ffu.isBlankFormReturned())
        .uaa(ffu.isUndeliveredAsAddress())
        .build();
  }

  public static CaseRequest convertHhNisra(FwmtActionInstruction ffu, GatewayCache cache) {
    return HhCreateConverter
        .convertHH(ffu, cache, CaseRequest.builder())
        .requiredOfficer(ffu.getFieldOfficerId())
        .sai("Sheltered Accommodation".equals(ffu.getEstabType()))
        .blankFormReturned(ffu.isBlankFormReturned())
        .uaa(ffu.isUndeliveredAsAddress())
        .build();
  }
}
