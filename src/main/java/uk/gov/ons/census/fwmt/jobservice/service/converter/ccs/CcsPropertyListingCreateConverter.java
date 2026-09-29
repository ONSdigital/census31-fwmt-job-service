package uk.gov.ons.census.fwmt.jobservice.service.converter.ccs;

import uk.gov.ons.census.fwmt.common.data.tm.Address;
import uk.gov.ons.census.fwmt.common.data.tm.CaseRequest;
import uk.gov.ons.census.fwmt.common.data.tm.CaseType;
import uk.gov.ons.census.fwmt.common.data.tm.Geography;
import uk.gov.ons.census.fwmt.common.data.tm.SurveyType;
import uk.gov.ons.census.fwmt.common.dto.rm.ActionInstruction;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCaseRecord;
import uk.gov.ons.census.fwmt.jobservice.service.converter.common.CommonCreateConverter;

import java.util.List;

public class CcsPropertyListingCreateConverter {

  private CcsPropertyListingCreateConverter() {
  }

  public static CaseRequest.CaseRequestBuilder convertCcs(
      ActionInstruction actionInstruction, GatewayCaseRecord cache, CaseRequest.CaseRequestBuilder builder) {
    CaseRequest.CaseRequestBuilder commonBuilder = CommonCreateConverter.convertCommon(actionInstruction, cache, builder);

    commonBuilder.type((actionInstruction.getAddressType()!=null)?CaseType.valueOf(actionInstruction.getAddressType()):CaseType.CCS);
    commonBuilder.surveyType((actionInstruction.getSurveyType()!=null)?actionInstruction.getSurveyType():SurveyType.CCS_PL);
    commonBuilder.category("Not applicable");

    commonBuilder.estabType("PL");
    commonBuilder.coordCode(actionInstruction.getFieldCoordinatorId());
    commonBuilder.requiredOfficer(actionInstruction.getFieldOfficerId());

    Geography outGeography = Geography.builder().oa(actionInstruction.getOa()).build();

    Address outAddress = Address.builder()
        .lines(List.of(actionInstruction.getPostcode()))
        .postcode(actionInstruction.getPostcode())
        .geography(outGeography)
        .build();
    commonBuilder.address(outAddress);

    return commonBuilder;
  }

  public static CaseRequest convertCcsPropertyListing(ActionInstruction actionInstruction, GatewayCaseRecord cache) {
    return CcsPropertyListingCreateConverter
        .convertCcs(actionInstruction, cache, CaseRequest.builder())
        .build();
  }
}