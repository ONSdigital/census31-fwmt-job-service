package uk.gov.ons.census.fwmt.jobservice.helper;

import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.CancelActionInstruction;

public class FwmtCancelJobRequestBuilder {

  public CancelActionInstruction cancelActionInstruction() {
    CancelActionInstruction fwmtCancelActionInstruction = new CancelActionInstruction();
    fwmtCancelActionInstruction.setActionInstruction(ActionInstructionType.CANCEL);
    fwmtCancelActionInstruction.setNc(false);
    fwmtCancelActionInstruction.setCaseId("ac623e62-4f4b-11eb-ae93-0242ac130002");
    fwmtCancelActionInstruction.setAddressLevel("E");
    fwmtCancelActionInstruction.setAddressType("CE");
    return fwmtCancelActionInstruction;
  }

  public CancelActionInstruction cancelCeUnitActionInstruction() {
    CancelActionInstruction fwmtCancelActionInstruction = new CancelActionInstruction();
    fwmtCancelActionInstruction.setActionInstruction(ActionInstructionType.CANCEL);
    fwmtCancelActionInstruction.setNc(false);
    fwmtCancelActionInstruction.setCaseId("ac623e62-4f4b-11eb-ae93-0242ac130002");
    fwmtCancelActionInstruction.setAddressLevel("U");
    fwmtCancelActionInstruction.setAddressType("CE");
    return fwmtCancelActionInstruction;
  }

  public CancelActionInstruction cancelSpgSiteActionInstruction() {
    CancelActionInstruction fwmtCancelActionInstruction = new CancelActionInstruction();
    fwmtCancelActionInstruction.setActionInstruction(ActionInstructionType.CANCEL);
    fwmtCancelActionInstruction.setNc(false);
    fwmtCancelActionInstruction.setCaseId("ac623e62-4f4b-11eb-ae93-0242ac130002");
    fwmtCancelActionInstruction.setAddressLevel("E");
    fwmtCancelActionInstruction.setAddressType("SPG");
    return fwmtCancelActionInstruction;
  }

  public CancelActionInstruction cancelSpgUnitActionInstruction() {
    CancelActionInstruction fwmtCancelActionInstruction = new CancelActionInstruction();
    fwmtCancelActionInstruction.setActionInstruction(ActionInstructionType.CANCEL);
    fwmtCancelActionInstruction.setNc(false);
    fwmtCancelActionInstruction.setCaseId("ac623e62-4f4b-11eb-ae93-0242ac130002");
    fwmtCancelActionInstruction.setAddressLevel("U");
    fwmtCancelActionInstruction.setAddressType("SPG");
    return fwmtCancelActionInstruction;
  }

  public CancelActionInstruction cancelFeedbackActionInstruction() {
    CancelActionInstruction fwmtCancelActionInstruction = new CancelActionInstruction();
    fwmtCancelActionInstruction.setActionInstruction(ActionInstructionType.CANCEL);
    fwmtCancelActionInstruction.setNc(false);
    fwmtCancelActionInstruction.setCaseId("ac623e62-4f4b-11eb-ae93-0242ac130002");
    fwmtCancelActionInstruction.setAddressLevel("F");
    fwmtCancelActionInstruction.setAddressType("FEEDBACK");
    fwmtCancelActionInstruction.setSurveyName("FEEDBACK");
    return fwmtCancelActionInstruction;
  }

  public CancelActionInstruction cancelCcsCeActionInstruction() {
    CancelActionInstruction fwmtCancelActionInstruction = new CancelActionInstruction();
    fwmtCancelActionInstruction.setActionInstruction(ActionInstructionType.CANCEL);
    fwmtCancelActionInstruction.setNc(false);
    fwmtCancelActionInstruction.setCaseId("ac623e62-4f4b-11eb-ae93-0242ac130002");
    fwmtCancelActionInstruction.setAddressLevel("E");
    fwmtCancelActionInstruction.setAddressType("Ce");
    fwmtCancelActionInstruction.setSurveyName("CCS");
    return fwmtCancelActionInstruction;
  }
  public CancelActionInstruction cancelCcsHhActionInstruction() {
    CancelActionInstruction fwmtCancelActionInstruction = new CancelActionInstruction();
    fwmtCancelActionInstruction.setActionInstruction(ActionInstructionType.CANCEL);
    fwmtCancelActionInstruction.setNc(false);
    fwmtCancelActionInstruction.setCaseId("ac623e62-4f4b-11eb-ae93-0242ac130002");
    fwmtCancelActionInstruction.setAddressLevel("U");
    fwmtCancelActionInstruction.setAddressType("CE");
    fwmtCancelActionInstruction.setSurveyName("CCS");
    return fwmtCancelActionInstruction;
  }
}
