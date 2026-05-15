package uk.gov.ons.census.fwmt.jobservice.spg;

import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;

public final class SpgRequestBuilder {

  public static FwmtActionInstruction makeUnitDeliver() {
    FwmtActionInstruction fieldworkFollowup = makeBase();

    fieldworkFollowup.setAddressLevel("U");
    fieldworkFollowup.setHandDeliver(true);

    return fieldworkFollowup;
  }

  public static FwmtActionInstruction makeUnitFollowup() {
    FwmtActionInstruction fieldworkFollowup = makeBase();

    fieldworkFollowup.setAddressLevel("U");
    fieldworkFollowup.setHandDeliver(false);

    return fieldworkFollowup;
  }

  public static FwmtActionInstruction makeSite() {
    FwmtActionInstruction fieldworkFollowup = makeBase();

    fieldworkFollowup.setAddressLevel("E");
    fieldworkFollowup.setSecureEstablishment(false);

    return fieldworkFollowup;
  }

  public static FwmtActionInstruction makeSecureSite() {
    FwmtActionInstruction fieldworkFollowup = makeBase();

    fieldworkFollowup.setAddressLevel("E");
    fieldworkFollowup.setSecureEstablishment(true);

    return fieldworkFollowup;
  }

  public static FwmtActionInstruction makeBase() {
    return FwmtActionInstruction.builder()
        .actionInstruction(ActionInstructionType.CREATE)
        // TODO: Are you sure this can be re-enabled?
        .surveyName("CENSUS") // Not needed, but still in formal diagrams
        .addressType("SPG")

        .caseId("exampleCaseId")
        .caseRef("exampleCaseRef")
        .estabType("exampleEstabType")
        .fieldOfficerId("exampleOfficerId")
        .fieldCoordinatorId("exampleCoordinatorId")

        .organisationName("exampleOrgName")

        .uprn("1")
        .addressLine1("exampleAddr1")
        .addressLine2("exampleAddr2")
        .addressLine3("exampleAddr3")
        .townName("exampleTown")
        .postcode("examplePostcode")
        .oa("exampleOa")

        .latitude(2d)
        .longitude(3d)
        .undeliveredAsAddress(false)

        .build();
  }
}
