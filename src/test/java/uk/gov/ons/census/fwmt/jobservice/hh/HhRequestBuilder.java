package uk.gov.ons.census.fwmt.jobservice.hh;

import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstruction;
import uk.gov.ons.census.fwmt.common.rm.dto.CancelActionInstruction;

public final class HhRequestBuilder {

    public static ActionInstruction createPauseInstruction() {
        return ActionInstruction.builder()
                .actionInstruction(ActionInstructionType.PAUSE)
                .surveyName("CENSUS")
                .addressType("HH")
                .addressLevel("U")
                .caseId("ac623e62-4f4b-11eb-ae93-0242ac130002")
                .pauseFrom("2020")
                .build();
    }

    public static ActionInstruction updateActionInstruction() {
        return ActionInstruction.builder()
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

    public static CancelActionInstruction cancelActionInstruction() {
        return CancelActionInstruction.builder()
            .actionInstruction(ActionInstructionType.CANCEL)
            .surveyName("CENSUS")
            .addressType("HH")
            .addressLevel("U")
            .caseId("ac623e62-4f4b-11eb-ae93-0242ac130002")
            .build();
    }
}