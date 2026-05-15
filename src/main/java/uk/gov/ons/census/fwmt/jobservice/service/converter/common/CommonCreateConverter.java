package uk.gov.ons.census.fwmt.jobservice.service.converter.common;

import uk.gov.ons.census.fwmt.common.data.tm.CaseRequest;
import uk.gov.ons.census.fwmt.common.data.tm.CaseType;
import uk.gov.ons.census.fwmt.common.data.tm.Contact;
import uk.gov.ons.census.fwmt.common.data.tm.Location;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;

public final class CommonCreateConverter {

  private CommonCreateConverter() {
  }

  public static CaseRequest.CaseRequestBuilder convertCommon(
      FwmtActionInstruction ffu, GatewayCache cache, CaseRequest.CaseRequestBuilder builder) {

    builder.reference(ffu.getCaseRef());
    builder.type(CaseType.CE);
    builder.category("Not applicable");
    builder.estabType(ffu.getEstabType());
    builder.coordCode(ffu.getFieldCoordinatorId());

    Contact outContact = Contact.builder().organisationName(ffu.getOrganisationName()).build();
    builder.contact(outContact);

    Location outLocation = Location.builder()
        .lat(ffu.getLatitude())
        ._long(ffu.getLongitude())
        .build();
    builder.location(outLocation);

    if (cache != null) {
      builder.description(cache.getCareCodes());
      builder.specialInstructions(cache.getAccessInfo());
    }

    builder.uaa(ffu.isUndeliveredAsAddress());
    builder.sai(false);

    return builder;
  }
}

