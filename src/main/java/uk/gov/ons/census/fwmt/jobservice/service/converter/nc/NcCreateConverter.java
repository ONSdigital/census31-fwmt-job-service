package uk.gov.ons.census.fwmt.jobservice.service.converter.nc;

import uk.gov.ons.census.fwmt.common.data.tm.*;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.service.converter.common.CommonCreateConverter;

import java.util.List;
import java.util.Objects;

public class NcCreateConverter {

  private NcCreateConverter() {
  }

  public static CaseRequest.CaseRequestBuilder convertNC(
      FwmtActionInstruction ffu, GatewayCache cache, CaseRequest.CaseRequestBuilder builder) {
    CaseRequest.CaseRequestBuilder commonBuilder = CommonCreateConverter.convertCommon(ffu, cache, builder);

    commonBuilder.reference(ffu.getCaseRef());
    commonBuilder.type(CaseType.NC);
    commonBuilder.surveyType(SurveyType.NC);
    commonBuilder.estabType(ffu.getEstabType());
    commonBuilder.coordCode(ffu.getFieldCoordinatorId());
    commonBuilder.requiredOfficer(ffu.getFieldOfficerId());

    Location location = Location
        .builder()
        .lat(ffu.getLatitude())
        ._long(ffu.getLongitude())
        .build();

    commonBuilder.location(location);

    Geography outGeography = Geography
        .builder()
        .oa(ffu.getOa())
        .build();

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
    commonBuilder.uaa(ffu.isUndeliveredAsAddress());
    commonBuilder.blankFormReturned(ffu.isBlankFormReturned());

    return commonBuilder;
  }

  public static CaseRequest convertHhNcEnglandAndWales(FwmtActionInstruction ffu, GatewayCache cache, String householder,
      GatewayCache previousDetails) {
    return NcCreateConverter
        .convertNC(ffu, cache, CaseRequest.builder())
        .category("HH")
        .sai("Sheltered Accommodation".equals(ffu.getEstabType()))
        .specialInstructions(getSpecialInstructions(previousDetails))
        .description(getDescription(ffu, previousDetails, householder))
        .build();
  }

  public static CaseRequest convertCeNcEnglandAndWales(FwmtActionInstruction ffu, GatewayCache cache, String householder,
      GatewayCache previousDetails) {
    return NcCreateConverter
        .convertNC(ffu, cache, CaseRequest.builder())
        .category("CE")
        .sai("Sheltered Accommodation".equals(ffu.getEstabType()))
        .specialInstructions(getSpecialInstructions(previousDetails))
        .description(getDescription(ffu, previousDetails, householder))
        .build();
  }

  private static String getDescription(FwmtActionInstruction ffu, GatewayCache cache, String householder) {
    StringBuilder description = new StringBuilder();
    if (cache != null && cache.getCareCodes() != null && !cache.getCareCodes().isEmpty()) {
      description.append(cache.getCareCodes());
      description.append("\n");
    }
    if (ffu.getAddressType().equals(CaseType.HH.toString()) && householder != null && !householder.equals("")) {
      description.append(householder);
      description.append("\n");
    }
    return description.toString();
  }

  private static String getSpecialInstructions(GatewayCache cache) {
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

