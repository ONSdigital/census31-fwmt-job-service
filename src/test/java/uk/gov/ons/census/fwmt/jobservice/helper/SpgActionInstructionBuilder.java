package uk.gov.ons.census.fwmt.jobservice.helper;

import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;

public class SpgActionInstructionBuilder {

  public FwmtActionInstruction createSpgSite() {
    FwmtActionInstruction actionInstruction = new FwmtActionInstruction();
    actionInstruction.setSurveyName("CENSUS");
    actionInstruction.setAddressType("SPG");
    actionInstruction.setAddressLevel("E");
    actionInstruction.setOa("E00167164");
    actionInstruction.setCaseId("ac623e62-4f4b-11eb-ae93-0242ac130002");
    actionInstruction.setCaseRef("7541877481");
    actionInstruction.setNc(false);
    actionInstruction.setAddressLine1("10 Test Street");
    actionInstruction.setTownName("Test Town");
    actionInstruction.setPostcode("TT TS1");
    actionInstruction.setEstabType("Hotel");
    actionInstruction.setFieldCoordinatorId("Test123");
    actionInstruction.setLatitude(50.0000);
    actionInstruction.setLongitude(1.0000);
    actionInstruction.setBlankFormReturned(true);
    actionInstruction.setUndeliveredAsAddress(true);
    return actionInstruction;
  }

  public FwmtActionInstruction createSpgUnitFollowup() {
    FwmtActionInstruction actionInstruction = new FwmtActionInstruction();
    actionInstruction.setSurveyName("CENSUS");
    actionInstruction.setAddressType("SPG");
    actionInstruction.setAddressLevel("U");
    actionInstruction.setOa("E00167164");
    actionInstruction.setCaseId("ac623e62-4f4b-11eb-ae93-0242ac130002");
    actionInstruction.setCaseRef("7541877481");
    actionInstruction.setNc(false);
    actionInstruction.setAddressLine1("10 Test Street");
    actionInstruction.setTownName("Test Town");
    actionInstruction.setPostcode("TT TS1");
    actionInstruction.setEstabType("Hotel");
    actionInstruction.setFieldCoordinatorId("Test123");
    actionInstruction.setLatitude(50.0000);
    actionInstruction.setLongitude(1.0000);
    actionInstruction.setBlankFormReturned(true);
    actionInstruction.setUndeliveredAsAddress(true);
    return actionInstruction;
  }
}
