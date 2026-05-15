package uk.gov.ons.census.fwmt.jobservice.helper;

import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;

public class FwmtCreateJobRequestBuilder {

  public FwmtActionInstruction createCeEstabDeliver() {
    FwmtActionInstruction fwmtActionInstruction = new FwmtActionInstruction();
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

  public FwmtActionInstruction createCeEstabFollowup() {
    FwmtActionInstruction fwmtActionInstruction = new FwmtActionInstruction();
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

  public FwmtActionInstruction createCeSite() {
    FwmtActionInstruction fwmtActionInstruction = new FwmtActionInstruction();
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
