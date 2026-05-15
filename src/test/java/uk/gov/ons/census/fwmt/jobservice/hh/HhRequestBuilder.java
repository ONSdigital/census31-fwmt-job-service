package uk.gov.ons.census.fwmt.jobservice.hh;

import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction;

public final class HhRequestBuilder {

    public static FwmtActionInstruction createPauseInstruction() {
        return FwmtActionInstruction.builder()
                .actionInstruction(ActionInstructionType.PAUSE)
                .surveyName("CENSUS")
                .addressType("HH")
                .addressLevel("U")
                .caseId("ac623e62-4f4b-11eb-ae93-0242ac130002")
                .pauseFrom("2020")
                .build();
    }

    public static FwmtActionInstruction updateActionInstruction() {
        return FwmtActionInstruction.builder()
            .actionInstruction(ActionInstructionType.UPDATE)
            .surveyName("CENSUS")
            .addressType("HH")
            .addressLevel("U")
            .caseId("ac623e62-4f4b-11eb-ae93-0242ac130002")
            .fieldOfficerId("TestOfficer")
            .addressLine1("Test1")
            .townName("Test Town")
            .postcode("A1 1AA")
            .oa("Test")
            .build();
    }

    public static FwmtCancelActionInstruction cancelActionInstruction() {
        return FwmtCancelActionInstruction.builder()
            .actionInstruction(ActionInstructionType.CANCEL)
            .surveyName("CENSUS")
            .addressType("HH")
            .addressLevel("U")
            .caseId("ac623e62-4f4b-11eb-ae93-0242ac130002")
            .build();
    }
}