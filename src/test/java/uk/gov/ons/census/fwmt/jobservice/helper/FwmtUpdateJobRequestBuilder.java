package uk.gov.ons.census.fwmt.jobservice.helper;

import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstruction;

public class FwmtUpdateJobRequestBuilder {

  public ActionInstruction createSpgUpdateUnit() {
    ActionInstruction fwmtActionInstruction = new ActionInstruction();
    fwmtActionInstruction.setActionInstruction(ActionInstructionType.UPDATE);
    fwmtActionInstruction.setCaseId("ac623e62-4f4b-11eb-ae93-0242ac130002");
    fwmtActionInstruction.setSurveyName("CENSUS");
    fwmtActionInstruction.setAddressType("SPG");
    fwmtActionInstruction.setAddressLevel("U");
    fwmtActionInstruction.setHandDeliver(true);
    fwmtActionInstruction.setUprn("1234");
    fwmtActionInstruction.setNc(false);
    return fwmtActionInstruction;
  }
}
