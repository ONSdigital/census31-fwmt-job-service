package uk.gov.ons.census.fwmt.jobservice.service.converter.nc;

import uk.gov.ons.census.fwmt.common.data.tm.*;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstruction;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCaseRecord;
import uk.gov.ons.census.fwmt.jobservice.service.converter.common.CommonCreateConverter;

import java.util.List;
import java.util.Objects;

public class NcCreateConverter {

  private NcCreateConverter() {
  }

  public static CaseRequest.CaseRequestBuilder convertNC(
      ActionInstruction actionInstruction, GatewayCaseRecord cache, CaseRequest.CaseRequestBuilder builder) {
    CaseRequest.CaseRequestBuilder commonBuilder = CommonCreateConverter.convertCommon(actionInstruction, cache, builder);

    commonBuilder.reference(actionInstruction.getCaseRef());
    commonBuilder.type(CaseType.NC);
    commonBuilder.surveyType(SurveyType.NC);
    commonBuilder.estabType(actionInstruction.getEstabType());
    commonBuilder.coordCode(actionInstruction.getFieldCoordinatorId());
    commonBuilder.requiredOfficer(actionInstruction.getFieldOfficerId());

    Location location = Location
        .builder()
        .lat(actionInstruction.getLatitude())
        ._long(actionInstruction.getLongitude())
        .build();

    commonBuilder.location(location);

    Geography outGeography = Geography
        .builder()
        .oa(actionInstruction.getOa())
        .build();

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
    commonBuilder.uaa(actionInstruction.isUndeliveredAsAddress());
    commonBuilder.blankFormReturned(actionInstruction.isBlankFormReturned());

    return commonBuilder;
  }

  public static CaseRequest convertHhNcEnglandAndWales(ActionInstruction actionInstruction, GatewayCaseRecord cache, String householder,
      GatewayCaseRecord previousDetails) {
    return NcCreateConverter
        .convertNC(actionInstruction, cache, CaseRequest.builder())
        .category("HH")
        .sai("Sheltered Accommodation".equals(actionInstruction.getEstabType()))
        .specialInstructions(getSpecialInstructions(previousDetails))
        .description(getDescription(actionInstruction, previousDetails, householder))
        .build();
  }

  public static CaseRequest convertCeNcEnglandAndWales(ActionInstruction actionInstruction, GatewayCaseRecord cache, String householder,
      GatewayCaseRecord previousDetails) {
    return NcCreateConverter
        .convertNC(actionInstruction, cache, CaseRequest.builder())
        .category("CE")
        .sai("Sheltered Accommodation".equals(actionInstruction.getEstabType()))
        .specialInstructions(getSpecialInstructions(previousDetails))
        .description(getDescription(actionInstruction, previousDetails, householder))
        .build();
  }

  private static String getDescription(ActionInstruction actionInstruction, GatewayCaseRecord cache, String householder) {
    StringBuilder description = new StringBuilder();
    if (cache != null && cache.getCareCodes() != null && !cache.getCareCodes().isEmpty()) {
      description.append(cache.getCareCodes());
      description.append("\n");
    }
    if (actionInstruction.getAddressType().equals(CaseType.HH.toString()) && householder != null && !householder.equals("")) {
      description.append(householder);
      description.append("\n");
    }
    return description.toString();
  }

  private static String getSpecialInstructions(GatewayCaseRecord cache) {
    StringBuilder instruction = new StringBuilder();
    if (cache != null && cache.getCareCodes() != null && !cache.getCareCodes().isEmpty()) {
      instruction.append(cache.getCareCodes());
      instruction.append("\n");
    }
    if (cache != null && cache.getAccessInfo() != null && !cache.getAccessInfo().isEmpty()) {
      instruction.append(cache.getAccessInfo());
      instruction.append("\n");
    }
    return instruction.toString();
  }

}

