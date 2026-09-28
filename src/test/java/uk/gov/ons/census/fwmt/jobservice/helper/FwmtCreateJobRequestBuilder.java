package uk.gov.ons.census.fwmt.jobservice.helper;

import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstruction;

public class FwmtCreateJobRequestBuilder {

  public ActionInstruction createCeEstabDeliver() {
    ActionInstruction fwmtActionInstruction = new ActionInstruction();
    fwmtActionInstruction.setActionInstruction(ActionInstructionType.CREATE);
    fwmtActionInstruction.setCaseId("ac623e62-4f4b-11eb-ae93-0242ac130002");
    fwmtActionInstruction.setSurveyName("CENSUS");
    fwmtActionInstruction.setAddressType("CE");
    fwmtActionInstruction.setAddressLevel("E");
    fwmtActionInstruction.setHandDeliver(true);
    fwmtActionInstruction.setUprn("1234");
    fwmtActionInstruction.setNc(false);
    return fwmtActionInstruction;
  }

  public ActionInstruction createCeEstabFollowup() {
    ActionInstruction fwmtActionInstruction = new ActionInstruction();
    fwmtActionInstruction.setActionInstruction(ActionInstructionType.CREATE);
    fwmtActionInstruction.setCaseId("ac623e62-4f4b-11eb-ae93-0242ac130002");
    fwmtActionInstruction.setSurveyName("CENSUS");
    fwmtActionInstruction.setAddressType("CE");
    fwmtActionInstruction.setAddressLevel("E");
    fwmtActionInstruction.setHandDeliver(false);
    fwmtActionInstruction.setUprn("1234");
    fwmtActionInstruction.setNc(false);
    return fwmtActionInstruction;
  }

  public ActionInstruction createCeSite() {
    ActionInstruction fwmtActionInstruction = new ActionInstruction();
    fwmtActionInstruction.setActionInstruction(ActionInstructionType.CREATE);
    fwmtActionInstruction.setCaseId("ac623e62-4f4b-11eb-ae93-0242ac130002");
    fwmtActionInstruction.setSurveyName("CENSUS");
    fwmtActionInstruction.setAddressType("CE");
    fwmtActionInstruction.setAddressLevel("E");
    fwmtActionInstruction.setUprn("1234");
    fwmtActionInstruction.setNc(false);
    return fwmtActionInstruction;
  }

}
