package uk.gov.ons.census.fwmt.jobservice.service.converter.ccs;

import uk.gov.ons.census.fwmt.common.data.tm.Address;
import uk.gov.ons.census.fwmt.common.data.tm.CaseRequest;
import uk.gov.ons.census.fwmt.common.data.tm.CaseType;
import uk.gov.ons.census.fwmt.common.data.tm.CcsCaseExtension;
import uk.gov.ons.census.fwmt.common.data.tm.Contact;
import uk.gov.ons.census.fwmt.common.data.tm.Geography;
import uk.gov.ons.census.fwmt.common.data.tm.SurveyType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.service.converter.common.CommonCreateConverter;

import java.util.List;
import java.util.Objects;

public class CcsInterviewCreateConverter {

  private CcsInterviewCreateConverter() {
  }

  public static CaseRequest.CaseRequestBuilder convertCcs(
      FwmtActionInstruction ffu, GatewayCache cache, CaseRequest.CaseRequestBuilder builder) {
    CaseRequest.CaseRequestBuilder commonBuilder = CommonCreateConverter.convertCommon(ffu, cache, builder);

    commonBuilder.type(CaseType.CCS);
    commonBuilder.surveyType(SurveyType.CCS_INT);
    commonBuilder.category("HH".equals(ffu.getAddressType()) ? "HH" : "CE");

    if (ffu.getEstabType() != null) {
      commonBuilder.estabType(ffu.getEstabType());
    } else {
      commonBuilder.estabType(ffu.getAddressType());
    }

    commonBuilder.coordCode(ffu.getFieldCoordinatorId());
    commonBuilder.requiredOfficer(ffu.getFieldOfficerId());

    String title = (cache != null && cache.getManagerTitle() != null ? cache.getManagerTitle() : "");
    String firstName = (cache != null && cache.getManagerFirstname() != null ? cache.getManagerFirstname() : "");
    String surname = (cache != null && cache.getManagerSurname() != null ? cache.getManagerSurname() : "");

    Contact outContact = Contact.builder()
        .organisationName(ffu.getOrganisationName() != null ? ffu.getOrganisationName() : "")
        .name(title + " " + firstName + " " + surname)
        .phone(cache != null && cache.getManagerContactNumber() != null ? cache.getManagerContactNumber() : "")
        .build();

    commonBuilder.contact(outContact);

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

  public static CaseRequest convertCcsInterview(FwmtActionInstruction ffu, GatewayCache cache, String eqUrl) {
    return CcsInterviewCreateConverter
        .convertCcs(ffu, cache, CaseRequest.builder())
        .ccs(CcsCaseExtension.builder().questionnaireUrl(eqUrl).build())
        .specialInstructions(getSpecialInstructions(cache))
        .description(getDescription(ffu, cache))
        .build();
  }

  private static String getDescription(FwmtActionInstruction ffu, GatewayCache cache) {
    StringBuilder description = new StringBuilder();
    if ("CE".equals(ffu.getAddressType())) {
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

  private static String getSpecialInstructions(GatewayCache cache) {
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
