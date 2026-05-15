package uk.gov.ons.census.fwmt.jobservice.helper;

import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction;

public class FwmtCancelJobRequestBuilder {

  public FwmtCancelActionInstruction cancelActionInstruction() {
    FwmtCancelActionInstruction fwmtCancelActionInstruction = new FwmtCancelActionInstruction();
    fwmtCancelActionInstruction.setActionInstruction(ActionInstructionType.CANCEL);
    fwmtCancelActionInstruction.setNc(false);
    fwmtCancelActionInstruction.setCaseId("ac623e62-4f4b-11eb-ae93-0242ac130002");
    fwmtCancelActionInstruction.setAddressLevel("E");
    fwmtCancelActionInstruction.setAddressType("CE");
    return fwmtCancelActionInstruction;
  }

  public FwmtCancelActionInstruction cancelCeUnitActionInstruction() {
    FwmtCancelActionInstruction fwmtCancelActionInstruction = new FwmtCancelActionInstruction();
    fwmtCancelActionInstruction.setActionInstruction(ActionInstructionType.CANCEL);
    fwmtCancelActionInstruction.setNc(false);
    fwmtCancelActionInstruction.setCaseId("ac623e62-4f4b-11eb-ae93-0242ac130002");
    fwmtCancelActionInstruction.setAddressLevel("U");
    fwmtCancelActionInstruction.setAddressType("CE");
    return fwmtCancelActionInstruction;
  }

  public FwmtCancelActionInstruction cancelSpgSiteActionInstruction() {
    FwmtCancelActionInstruction fwmtCancelActionInstruction = new FwmtCancelActionInstruction();
    fwmtCancelActionInstruction.setActionInstruction(ActionInstructionType.CANCEL);
    fwmtCancelActionInstruction.setNc(false);
    fwmtCancelActionInstruction.setCaseId("ac623e62-4f4b-11eb-ae93-0242ac130002");
    fwmtCancelActionInstruction.setAddressLevel("E");
    fwmtCancelActionInstruction.setAddressType("SPG");
    return fwmtCancelActionInstruction;
  }

  public FwmtCancelActionInstruction cancelSpgUnitActionInstruction() {
    FwmtCancelActionInstruction fwmtCancelActionInstruction = new FwmtCancelActionInstruction();
    fwmtCancelActionInstruction.setActionInstruction(ActionInstructionType.CANCEL);
    fwmtCancelActionInstruction.setNc(false);
    fwmtCancelActionInstruction.setCaseId("ac623e62-4f4b-11eb-ae93-0242ac130002");
    fwmtCancelActionInstruction.setAddressLevel("U");
    fwmtCancelActionInstruction.setAddressType("SPG");
    return fwmtCancelActionInstruction;
  }

  public FwmtCancelActionInstruction cancelFeedbackActionInstruction() {
    FwmtCancelActionInstruction fwmtCancelActionInstruction = new FwmtCancelActionInstruction();
    fwmtCancelActionInstruction.setActionInstruction(ActionInstructionType.CANCEL);
    fwmtCancelActionInstruction.setNc(false);
    fwmtCancelActionInstruction.setCaseId("ac623e62-4f4b-11eb-ae93-0242ac130002");
    fwmtCancelActionInstruction.setAddressLevel("F");
    fwmtCancelActionInstruction.setAddressType("FEEDBACK");
    fwmtCancelActionInstruction.setSurveyName("FEEDBACK");
    return fwmtCancelActionInstruction;
  }

  public FwmtCancelActionInstruction cancelCcsCeActionInstruction() {
    FwmtCancelActionInstruction fwmtCancelActionInstruction = new FwmtCancelActionInstruction();
    fwmtCancelActionInstruction.setActionInstruction(ActionInstructionType.CANCEL);
    fwmtCancelActionInstruction.setNc(false);
    fwmtCancelActionInstruction.setCaseId("ac623e62-4f4b-11eb-ae93-0242ac130002");
    fwmtCancelActionInstruction.setAddressLevel("E");
    fwmtCancelActionInstruction.setAddressType("Ce");
    fwmtCancelActionInstruction.setSurveyName("CCS");
    return fwmtCancelActionInstruction;
  }
  public FwmtCancelActionInstruction cancelCcsHhActionInstruction() {
    FwmtCancelActionInstruction fwmtCancelActionInstruction = new FwmtCancelActionInstruction();
    fwmtCancelActionInstruction.setActionInstruction(ActionInstructionType.CANCEL);
    fwmtCancelActionInstruction.setNc(false);
    fwmtCancelActionInstruction.setCaseId("ac623e62-4f4b-11eb-ae93-0242ac130002");
    fwmtCancelActionInstruction.setAddressLevel("U");
    fwmtCancelActionInstruction.setAddressType("CE");
    fwmtCancelActionInstruction.setSurveyName("CCS");
    return fwmtCancelActionInstruction;
  }
}
