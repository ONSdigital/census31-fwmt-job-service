package uk.gov.ons.census.fwmt.jobservice.service.converter.spg;

import uk.gov.ons.census.fwmt.common.data.tm.Address;
import uk.gov.ons.census.fwmt.common.data.tm.CaseRequest;
import uk.gov.ons.census.fwmt.common.data.tm.CeCaseExtension;
import uk.gov.ons.census.fwmt.common.data.tm.Geography;
import uk.gov.ons.census.fwmt.common.data.tm.SurveyType;
import uk.gov.ons.census.fwmt.common.dto.rm.ActionInstruction;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCaseRecord;
import uk.gov.ons.census.fwmt.jobservice.service.converter.common.CommonCreateConverter;

import java.util.List;
import java.util.Objects;

public final class SpgCreateConverter {

  private final static String SECURE_SITE =  "Secure Site";

  private SpgCreateConverter() {
  }

  public static CaseRequest.CaseRequestBuilder convertSPG(
      ActionInstruction actionInstruction, GatewayCaseRecord cache, CaseRequest.CaseRequestBuilder builder) {

    CaseRequest.CaseRequestBuilder commonBuilder = CommonCreateConverter.convertCommon(actionInstruction, cache, builder);
    commonBuilder.requiredOfficer(actionInstruction.getFieldOfficerId());

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

    CeCaseExtension ceCaseExtension = CeCaseExtension.builder()
        .ce1Complete(false)
        .deliveryRequired(false)
        .expectedResponses(0)
        .actualResponses(0)
        .build();
    commonBuilder.ce(ceCaseExtension);
  
    return commonBuilder;
  }

  public static CaseRequest convertSecureSite(ActionInstruction actionInstruction, GatewayCaseRecord cache) {
    return SpgCreateConverter.convertSPG(actionInstruction, cache, CaseRequest.builder())
        .surveyType(SurveyType.SPG_Site)
        .reference("SECSS_" + actionInstruction.getCaseRef())
        .description(getCareCodes(cache).concat(SECURE_SITE))
        .specialInstructions(getSpecialInstructions(cache))
        .build();
  }
  public static CaseRequest convertSecureUnitFollowup(ActionInstruction actionInstruction, GatewayCaseRecord cache) {
    return SpgCreateConverter.convertSPG(actionInstruction, cache, CaseRequest.builder())
        .surveyType(SurveyType.SPG_Unit_F)    
        .reference("SECSU_" + actionInstruction.getCaseRef())
        .description(getCareCodes(cache).concat(SECURE_SITE))
        .specialInstructions(getSpecialInstructions(cache))
        .build();
  }

  public static CaseRequest convertSite(ActionInstruction actionInstruction, GatewayCaseRecord cache) {
    return SpgCreateConverter.convertSPG(actionInstruction, cache, CaseRequest.builder())
        .surveyType(SurveyType.SPG_Site)
        .description(getCareCodes(cache))
        .specialInstructions(getSpecialInstructions(cache))
        .build();
  }

  public static CaseRequest convertUnitDeliver(ActionInstruction actionInstruction, GatewayCaseRecord cache) {
    return SpgCreateConverter.convertSPG(actionInstruction, cache, CaseRequest.builder())
        .surveyType(SurveyType.SPG_Unit_D)
        .description(getCareCodes(cache))
        .specialInstructions(getSpecialInstructions(cache))
        .build();
  }

  public static CaseRequest convertUnitFollowup(ActionInstruction actionInstruction, GatewayCaseRecord cache) {
    return SpgCreateConverter.convertSPG(actionInstruction, cache, CaseRequest.builder())
        .surveyType(SurveyType.SPG_Unit_F)
        .description(getCareCodes(cache))
        .specialInstructions(getSpecialInstructions(cache))
        .build();
  }

  private static String getCareCodes(GatewayCaseRecord cache) {
    if (cache != null && cache.getCareCodes() != null && !cache.getCareCodes().isEmpty()) {
      return cache.getCareCodes() + "\n";
    }
    return "";
  }

  private static String getSpecialInstructions(GatewayCaseRecord cache) {
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