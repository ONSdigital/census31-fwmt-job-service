package uk.gov.ons.census.fwmt.jobservice.nc;

import uk.gov.ons.census.fwmt.common.dto.rm.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.dto.rm.ActionInstruction;

public final class NcRequestBuilder {

    public static ActionInstruction makeUnitDeliver() {
        ActionInstruction fieldworkFollowup = makeBase();

        fieldworkFollowup.setAddressLevel("U");
        fieldworkFollowup.setHandDeliver(true);

        return fieldworkFollowup;
    }

    public static ActionInstruction makeUnitFollowup() {
        ActionInstruction fieldworkFollowup = makeBase();

        fieldworkFollowup.setAddressLevel("U");
        fieldworkFollowup.setHandDeliver(false);

        return fieldworkFollowup;
    }

    public static ActionInstruction makeSite() {
        ActionInstruction fieldworkFollowup = makeBase();

        fieldworkFollowup.setAddressLevel("E");
        fieldworkFollowup.setSecureEstablishment(false);

        return fieldworkFollowup;
    }

    public static ActionInstruction makeSecureSite() {
        ActionInstruction fieldworkFollowup = makeBase();

        fieldworkFollowup.setAddressLevel("E");
        fieldworkFollowup.setSecureEstablishment(true);

        return fieldworkFollowup;
    }

    public static ActionInstruction makeBase() {
        return ActionInstruction.builder()
                .actionInstruction(ActionInstructionType.CREATE)
                // TODO: Are you sure this can be re-enabled?
                .surveyName("CENSUS") // Not needed, but still in formal diagrams
                .addressType("CE")

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
                .estabUprn("111")
                .latitude(2d)
                .longitude(3d)
                .undeliveredAsAddress(false)

                .build();
    }
}