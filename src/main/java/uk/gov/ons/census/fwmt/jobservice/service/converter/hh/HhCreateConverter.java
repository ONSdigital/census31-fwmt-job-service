package uk.gov.ons.census.fwmt.jobservice.service.converter.hh;

import uk.gov.ons.census.fwmt.common.data.tm.Address;
import uk.gov.ons.census.fwmt.common.data.tm.CaseRequest;
import uk.gov.ons.census.fwmt.common.data.tm.CaseType;
import uk.gov.ons.census.fwmt.common.data.tm.Geography;
import uk.gov.ons.census.fwmt.common.data.tm.SurveyType;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstruction;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCaseRecord;
import uk.gov.ons.census.fwmt.jobservice.service.converter.common.CommonCreateConverter;

import java.util.List;
import java.util.Objects;

public final class HhCreateConverter {

  private HhCreateConverter() {
  }

  public static CaseRequest.CaseRequestBuilder convertHH(
      ActionInstruction actionInstruction, GatewayCaseRecord cache, CaseRequest.CaseRequestBuilder builder) {
    CaseRequest.CaseRequestBuilder commonBuilder = CommonCreateConverter.convertCommon(actionInstruction, cache, builder);

    commonBuilder.type(CaseType.HH);
    commonBuilder.surveyType(SurveyType.HH);
    commonBuilder.category("HH");

    Geography outGeography = Geography.builder().oa(actionInstruction.getOa()).build();

    Address outAddress = Address.builder()
        .lines(List.of(
            actionInstruction.getAddressLine1(),
            Objects.toString(actionInstruction.getAddressLine2(), ""),
            Objects.toString(actionInstruction.getAddressLine3(), "")
        ))
        .town(actionInstruction.getTownName())
        .postcode(actionInstruction.getPostcode())
        .geography(outGeography)
        .build();
    commonBuilder.address(outAddress);

    return commonBuilder;
  }

  public static CaseRequest convertHhEnglandAndWales(ActionInstruction actionInstruction, GatewayCaseRecord cache) {
    return HhCreateConverter
        .convertHH(actionInstruction, cache, CaseRequest.builder())
        .sai("Sheltered Accommodation".equals(actionInstruction.getEstabType()))
        .blankFormReturned(actionInstruction.isBlankFormReturned())
        .uaa(actionInstruction.isUndeliveredAsAddress())
        .build();
  }

  public static CaseRequest convertHhNisra(ActionInstruction actionInstruction, GatewayCaseRecord cache) {
    return HhCreateConverter
        .convertHH(actionInstruction, cache, CaseRequest.builder())
        .requiredOfficer(actionInstruction.getFieldOfficerId())
        .sai("Sheltered Accommodation".equals(actionInstruction.getEstabType()))
        .blankFormReturned(actionInstruction.isBlankFormReturned())
        .uaa(actionInstruction.isUndeliveredAsAddress())
        .build();
  }
}
