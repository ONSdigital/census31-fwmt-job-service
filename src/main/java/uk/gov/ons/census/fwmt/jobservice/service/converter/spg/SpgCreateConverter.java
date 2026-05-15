package uk.gov.ons.census.fwmt.jobservice.service.converter.spg;

import uk.gov.ons.census.fwmt.common.data.tm.Address;
import uk.gov.ons.census.fwmt.common.data.tm.CaseRequest;
import uk.gov.ons.census.fwmt.common.data.tm.CeCaseExtension;
import uk.gov.ons.census.fwmt.common.data.tm.Geography;
import uk.gov.ons.census.fwmt.common.data.tm.SurveyType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.service.converter.common.CommonCreateConverter;

import java.util.List;
import java.util.Objects;

public final class SpgCreateConverter {

  private final static String SECURE_SITE =  "Secure Site";

  private SpgCreateConverter() {
  }

  public static CaseRequest.CaseRequestBuilder convertSPG(
      FwmtActionInstruction ffu, GatewayCache cache, CaseRequest.CaseRequestBuilder builder) {

    CaseRequest.CaseRequestBuilder commonBuilder = CommonCreateConverter.convertCommon(ffu, cache, builder);
    commonBuilder.requiredOfficer(ffu.getFieldOfficerId());

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

    CeCaseExtension ceCaseExtension = CeCaseExtension.builder()
        .ce1Complete(false)
        .deliveryRequired(false)
        .expectedResponses(0)
        .actualResponses(0)
        .build();
    commonBuilder.ce(ceCaseExtension);
  
    return commonBuilder;
  }

  public static CaseRequest convertSecureSite(FwmtActionInstruction ffu, GatewayCache cache) {
    return SpgCreateConverter.convertSPG(ffu, cache, CaseRequest.builder())
        .surveyType(SurveyType.SPG_Site)
        .reference("SECSS_" + ffu.getCaseRef())
        .description(getCareCodes(cache).concat(SECURE_SITE))
        .specialInstructions(getSpecialInstructions(cache))
        .build();
  }
  public static CaseRequest convertSecureUnitFollowup(FwmtActionInstruction ffu, GatewayCache cache) {
    return SpgCreateConverter.convertSPG(ffu, cache, CaseRequest.builder())
        .surveyType(SurveyType.SPG_Unit_F)    
        .reference("SECSU_" + ffu.getCaseRef())
        .description(getCareCodes(cache).concat(SECURE_SITE))
        .specialInstructions(getSpecialInstructions(cache))
        .build();
  }

  public static CaseRequest convertSite(FwmtActionInstruction ffu, GatewayCache cache) {
    return SpgCreateConverter.convertSPG(ffu, cache, CaseRequest.builder())
        .surveyType(SurveyType.SPG_Site)
        .description(getCareCodes(cache))
        .specialInstructions(getSpecialInstructions(cache))
        .build();
  }

  public static CaseRequest convertUnitDeliver(FwmtActionInstruction ffu, GatewayCache cache) {
    return SpgCreateConverter.convertSPG(ffu, cache, CaseRequest.builder())
        .surveyType(SurveyType.SPG_Unit_D)
        .description(getCareCodes(cache))
        .specialInstructions(getSpecialInstructions(cache))
        .build();
  }

  public static CaseRequest convertUnitFollowup(FwmtActionInstruction ffu, GatewayCache cache) {
    return SpgCreateConverter.convertSPG(ffu, cache, CaseRequest.builder())
        .surveyType(SurveyType.SPG_Unit_F)
        .description(getCareCodes(cache))
        .specialInstructions(getSpecialInstructions(cache))
        .build();
  }

  private static String getCareCodes(GatewayCache cache) {
    if (cache != null && cache.getCareCodes() != null && !cache.getCareCodes().isEmpty()) {
      return cache.getCareCodes() + "\n";
    }
    return "";
  }

  private static String getSpecialInstructions(GatewayCache cache) {
    StringBuilder instruction = new StringBuilder();
    if (cache != null && cache.getAccessInfo() != null && !cache.getAccessInfo().isEmpty()) {
      instruction.append(cache.getAccessInfo());
      instruction.append("\n");
    }
    if (!getCareCodes(cache).isEmpty()) {
      instruction.append(getCareCodes(cache));
    }

    return instruction.toString();
  }

}