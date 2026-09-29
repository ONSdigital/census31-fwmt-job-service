package uk.gov.ons.census.fwmt.jobservice.service.converter.common;

import uk.gov.ons.census.fwmt.common.data.tm.CaseRequest;
import uk.gov.ons.census.fwmt.common.data.tm.CaseType;
import uk.gov.ons.census.fwmt.common.data.tm.Contact;
import uk.gov.ons.census.fwmt.common.data.tm.Location;
import uk.gov.ons.census.fwmt.common.dto.rm.ActionInstruction;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCaseRecord;

public final class CommonCreateConverter {

  private CommonCreateConverter() {
  }

  public static CaseRequest.CaseRequestBuilder convertCommon(
      ActionInstruction actionInstruction, GatewayCaseRecord cache, CaseRequest.CaseRequestBuilder builder) {

    builder.reference(actionInstruction.getCaseRef());
    builder.type(CaseType.CE);
    builder.category("Not applicable");
    builder.estabType(actionInstruction.getEstabType());
    builder.coordCode(actionInstruction.getFieldCoordinatorId());

    Contact outContact = Contact.builder().organisationName(actionInstruction.getOrganisationName()).build();
    builder.contact(outContact);

    Location outLocation = Location.builder()
        .lat(actionInstruction.getLatitude())
        ._long(actionInstruction.getLongitude())
        .build();
    builder.location(outLocation);

    if (cache != null) {
      builder.description(cache.getCareCodes());
      builder.specialInstructions(cache.getAccessInfo());
    }

    builder.uaa(actionInstruction.isUndeliveredAsAddress());
    builder.sai(false);

    return builder;
  }
}

