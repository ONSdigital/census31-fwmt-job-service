package uk.gov.ons.census.fwmt.jobservice.service.converter.ccs;

import uk.gov.ons.census.fwmt.common.data.tm.Address;
import uk.gov.ons.census.fwmt.common.data.tm.CaseRequest;
import uk.gov.ons.census.fwmt.common.data.tm.CaseType;
import uk.gov.ons.census.fwmt.common.data.tm.CcsCaseExtension;
import uk.gov.ons.census.fwmt.common.data.tm.Contact;
import uk.gov.ons.census.fwmt.common.data.tm.Geography;
import uk.gov.ons.census.fwmt.common.data.tm.SurveyType;
import uk.gov.ons.census.fwmt.common.dto.rm.ActionInstruction;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCaseRecord;
import uk.gov.ons.census.fwmt.jobservice.service.converter.common.CommonCreateConverter;

import java.util.List;
import java.util.Objects;

public class CcsInterviewCreateConverter {

  private CcsInterviewCreateConverter() {
  }

  public static CaseRequest.CaseRequestBuilder convertCcs(
      ActionInstruction actionInstruction, GatewayCaseRecord cache, CaseRequest.CaseRequestBuilder builder) {
    CaseRequest.CaseRequestBuilder commonBuilder = CommonCreateConverter.convertCommon(actionInstruction, cache, builder);

    commonBuilder.type(CaseType.CCS);
    commonBuilder.surveyType(SurveyType.CCS_INT);
    commonBuilder.category("HH".equals(actionInstruction.getAddressType()) ? "HH" : "CE");

    if (actionInstruction.getEstabType() != null) {
      commonBuilder.estabType(actionInstruction.getEstabType());
    } else {
      commonBuilder.estabType(actionInstruction.getAddressType());
    }

    commonBuilder.coordCode(actionInstruction.getFieldCoordinatorId());
    commonBuilder.requiredOfficer(actionInstruction.getFieldOfficerId());

    String title = (cache != null && cache.getManagerTitle() != null ? cache.getManagerTitle() : "");
    String firstName = (cache != null && cache.getManagerFirstname() != null ? cache.getManagerFirstname() : "");
    String surname = (cache != null && cache.getManagerSurname() != null ? cache.getManagerSurname() : "");

    Contact outContact = Contact.builder()
        .organisationName(actionInstruction.getOrganisationName() != null ? actionInstruction.getOrganisationName() : "")
        .name(title + " " + firstName + " " + surname)
        .phone(cache != null && cache.getManagerContactNumber() != null ? cache.getManagerContactNumber() : "")
        .build();

    commonBuilder.contact(outContact);

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

  public static CaseRequest convertCcsInterview(ActionInstruction actionInstruction, GatewayCaseRecord cache, String eqUrl) {
    return CcsInterviewCreateConverter
        .convertCcs(actionInstruction, cache, CaseRequest.builder())
        .ccs(CcsCaseExtension.builder().questionnaireUrl(eqUrl).build())
        .specialInstructions(getSpecialInstructions(cache))
        .description(getDescription(actionInstruction, cache))
        .build();
  }

  private static String getDescription(ActionInstruction actionInstruction, GatewayCaseRecord cache) {
    StringBuilder description = new StringBuilder();
    if ("CE".equals(actionInstruction.getAddressType())) {
      description
          .append("No of Residents: ")
          .append(cache.getUsualResidents() != null ? cache.getUsualResidents() : "0")
          .append("\n")
          .append("Bedspaces: ")
          .append(cache.getBedspaces() != null ? cache.getBedspaces() : "0")
          .append("\n");
    }
    return description.toString();
  }

  private static String getSpecialInstructions(GatewayCaseRecord cache) {
    StringBuilder instruction = new StringBuilder();
    if (cache != null && cache.getAccessInfo() != null && !cache.getAccessInfo().isEmpty()) {
      instruction.append(cache.getAccessInfo());
      instruction.append("\n");
    }
    if (cache != null && cache.getCareCodes() != null && !cache.getCareCodes().isEmpty()) {
      instruction.append(cache.getCareCodes());
      instruction.append("\n");
    }
    return instruction.toString();
  }
}
