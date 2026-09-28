package uk.gov.ons.census.fwmt.jobservice.service.converter.ce;

import uk.gov.ons.census.fwmt.common.data.tm.Address;
import uk.gov.ons.census.fwmt.common.data.tm.CaseRequest;
import uk.gov.ons.census.fwmt.common.data.tm.CeCaseExtension;
import uk.gov.ons.census.fwmt.common.data.tm.Geography;
import uk.gov.ons.census.fwmt.common.data.tm.SurveyType;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstruction;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCaseRecord;
import uk.gov.ons.census.fwmt.jobservice.service.converter.common.CommonCreateConverter;

import java.util.List;
import java.util.Objects;

public final class CeCreateConverter {
  private static final String SECURE_UNIT = "Secure Unit";
  private static final String SECURE_SITE = "Secure Site";
  private static final String SECURE_ESTABLISHMENT = "Secure Establishment";

  private CeCreateConverter() {
  }

  public static CaseRequest.CaseRequestBuilder convertCE(
      ActionInstruction actionInstruction, GatewayCaseRecord cache, CaseRequest.CaseRequestBuilder builder,
      boolean isEstab, boolean isUnit) {

    boolean ce1Completed = false;
    boolean handDelivery = false;
    int actualResponse = 0;
    int expectedResponse = 0;

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
        .uprn(Long.parseLong(actionInstruction.getUprn()))
        .estabUprn(actionInstruction.getEstabUprn() == null ? null : Long.parseLong(actionInstruction.getEstabUprn()))
        .build();
    commonBuilder.address(outAddress);

    if (isEstab) {
      if (actionInstruction.isCe1Complete()) {
        ce1Completed = true;
      }
    }

    if (isEstab || isUnit) {
      if (actionInstruction.getCeActualResponses() != null && actionInstruction.getCeActualResponses() != 0) {
        actualResponse = actionInstruction.getCeActualResponses();
      }

      if (actionInstruction.getCeExpectedCapacity() != null && actionInstruction.getCeExpectedCapacity() != 0) {
        expectedResponse = actionInstruction.getCeExpectedCapacity();
      }

      if (actionInstruction.isHandDeliver()) {
        handDelivery = actionInstruction.isHandDeliver();
      }
    }

      CeCaseExtension ceCaseExtension = CeCaseExtension.builder()
          .ce1Complete(ce1Completed)
          .deliveryRequired(handDelivery)
          .expectedResponses(expectedResponse)
          .actualResponses(actualResponse)
          .build();
      commonBuilder.ce(ceCaseExtension);

    return commonBuilder;
  }

  public static CaseRequest convertCeEstabDeliver(ActionInstruction actionInstruction, GatewayCaseRecord cache) {
    return CeCreateConverter
        .convertCE(actionInstruction, cache, CaseRequest.builder(), true, false)
        .surveyType(SurveyType.CE_EST_D)
        .description(getDescription(cache))
        .specialInstructions(getSpecialInstructions(cache))
        .build();
  }

  public static CaseRequest convertCeEstabDeliverSecure(ActionInstruction actionInstruction, GatewayCaseRecord cache) {
    return CeCreateConverter.convertCE(actionInstruction, cache, CaseRequest.builder(), true, false)
        .surveyType(SurveyType.CE_EST_D)
        .reference("SECCE_" + actionInstruction.getCaseRef())
        .description(getDescription(cache,SECURE_ESTABLISHMENT))
        .specialInstructions(getSpecialInstructions(cache))
        .build();
  }

  public static CaseRequest convertCeEstabFollowup(ActionInstruction actionInstruction, GatewayCaseRecord cache) {
    SurveyType surveyType = actionInstruction.getCeExpectedCapacity() > 0 ? SurveyType.CE_EST  : SurveyType.CE_ESTWU;
    return CeCreateConverter
        .convertCE(actionInstruction, cache, CaseRequest.builder(), true, false)
        .surveyType(surveyType)
        .description(getDescription(cache))
        .specialInstructions(getSpecialInstructions(cache))
        .build();
  }

  public static CaseRequest convertCeEstabFollowupSecure(ActionInstruction actionInstruction, GatewayCaseRecord cache)  {
    SurveyType surveyType = actionInstruction.getCeExpectedCapacity() > 0 ? SurveyType.CE_EST  : SurveyType.CE_ESTWU;
    return CeCreateConverter.convertCE(actionInstruction, cache, CaseRequest.builder(), true, false)
        .surveyType(surveyType)
        .reference("SECCE_" + actionInstruction.getCaseRef())
        .description(getDescription(cache, SECURE_ESTABLISHMENT))
        .specialInstructions(getSpecialInstructions(cache))
        .build();
  }

  public static CaseRequest convertCeSite(ActionInstruction actionInstruction, GatewayCaseRecord cache) {
    return CeCreateConverter
        .convertCE(actionInstruction, cache, CaseRequest.builder(), false, false)
        .surveyType(SurveyType.CE_SITE)
        .description(getDescription(cache))
        .specialInstructions(getSpecialInstructions(cache))
        .build();
  }

  public static CaseRequest convertCeSiteSecure(ActionInstruction actionInstruction, GatewayCaseRecord cache) {
    return CeCreateConverter.convertCE(actionInstruction, cache, CaseRequest.builder(), false, false)
        .surveyType(SurveyType.CE_SITE)
        .reference("SECCS_" + actionInstruction.getCaseRef())
        .description(getDescription(cache, SECURE_SITE))
        .specialInstructions(getSpecialInstructions(cache))
        .build();
  }

  public static CaseRequest convertCeUnitDeliver(ActionInstruction actionInstruction, GatewayCaseRecord cache) {
    return CeCreateConverter
        .convertCE(actionInstruction, cache, CaseRequest.builder(), false, true)
        .surveyType(SurveyType.CE_UNIT_D)
        .description(getDescription(cache) )
        .specialInstructions(getSpecialInstructions(cache))
        .build();
  }

  public static CaseRequest convertCeUnitDeliverSecure(ActionInstruction actionInstruction, GatewayCaseRecord cache) {
    return CeCreateConverter.convertCE(actionInstruction, cache, CaseRequest.builder(), false, true)
        .surveyType(SurveyType.CE_UNIT_D)
        .reference("SECCU_" + actionInstruction.getCaseRef())
        .description(getDescription(cache, SECURE_UNIT))
        .specialInstructions(getSpecialInstructions(cache))
        .build();
  }

  public static CaseRequest convertCeUnitFollowup(ActionInstruction actionInstruction, GatewayCaseRecord cache) {
    return CeCreateConverter
        .convertCE(actionInstruction, cache, CaseRequest.builder(), false, true)
        .surveyType(SurveyType.CE_UNIT_F)
        .description(getDescription(cache) )
        .specialInstructions(getSpecialInstructions(cache))
        .build();
  }

  public static CaseRequest convertCeUnitFollowupSecure(ActionInstruction actionInstruction, GatewayCaseRecord cache) {
    return CeCreateConverter.convertCE(actionInstruction, cache, CaseRequest.builder(), false, true)
        .surveyType(SurveyType.CE_UNIT_F)
        .reference("SECCU_" + actionInstruction.getCaseRef())
        .description(getDescription(cache, SECURE_UNIT))
        .specialInstructions(getSpecialInstructions(cache))
        .build();
  }

  private static String getDescription(GatewayCaseRecord cache, String referenceType) {
    StringBuilder description = new StringBuilder(getDescription(cache));
    description.append(referenceType);
    return description.toString();
  }

  private static String getDescription(GatewayCaseRecord cache) {
    StringBuilder description = new StringBuilder("");
    if (cache != null && cache.getCareCodes() != null && !cache.getCareCodes().isEmpty()) {
      description.append(cache.getCareCodes()).append("\n");
    }
    return description.toString();
  }

  private static String getSpecialInstructions(GatewayCaseRecord cache) {
    StringBuilder instruction = new StringBuilder(getDescription(cache));
    if (cache != null && cache.getAccessInfo() != null && !cache.getAccessInfo().isEmpty()) {
      instruction.append(cache.getAccessInfo());
    }
    return instruction.toString();
  }


}

