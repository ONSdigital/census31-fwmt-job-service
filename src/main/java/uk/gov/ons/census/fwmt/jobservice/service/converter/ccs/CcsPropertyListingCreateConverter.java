package uk.gov.ons.census.fwmt.jobservice.service.converter.ccs;

import uk.gov.ons.census.fwmt.common.data.tm.Address;
import uk.gov.ons.census.fwmt.common.data.tm.CaseRequest;
import uk.gov.ons.census.fwmt.common.data.tm.CaseType;
import uk.gov.ons.census.fwmt.common.data.tm.Geography;
import uk.gov.ons.census.fwmt.common.data.tm.SurveyType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.service.converter.common.CommonCreateConverter;

import java.util.List;

public class CcsPropertyListingCreateConverter {

  private CcsPropertyListingCreateConverter() {
  }

  public static CaseRequest.CaseRequestBuilder convertCcs(
      FwmtActionInstruction ffu, GatewayCache cache, CaseRequest.CaseRequestBuilder builder) {
    CaseRequest.CaseRequestBuilder commonBuilder = CommonCreateConverter.convertCommon(ffu, cache, builder);

    commonBuilder.type((ffu.getAddressType()!=null)?CaseType.valueOf(ffu.getAddressType()):CaseType.CCS);
    commonBuilder.surveyType((ffu.getSurveyType()!=null)?ffu.getSurveyType():SurveyType.CCS_PL);
    commonBuilder.category("Not applicable");

    commonBuilder.estabType("PL");
    commonBuilder.coordCode(ffu.getFieldCoordinatorId());
    commonBuilder.requiredOfficer(ffu.getFieldOfficerId());

    Geography outGeography = Geography.builder().oa(ffu.getOa()).build();

    Address outAddress = Address.builder()
        .lines(List.of(ffu.getPostcode()))
        .postcode(ffu.getPostcode())
        .geography(outGeography)
        .build();
    commonBuilder.address(outAddress);

    return commonBuilder;
  }

  public static CaseRequest convertCcsPropertyListing(FwmtActionInstruction ffu, GatewayCache cache) {
    return CcsPropertyListingCreateConverter
        .convertCcs(ffu, cache, CaseRequest.builder())
        .build();
  }
}