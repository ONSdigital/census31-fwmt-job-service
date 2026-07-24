package uk.gov.ons.census.fwmt.jobservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.config.FeatureFlagConfig;

import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.FEATURE_FLAG_IGNORED;

/**
 * Consumes RM action instructions and orchestrates dispatch to TM.
 */
@Slf4j
@Service
public class JobService {

  @Autowired
  private GatewayEventManager eventManager;

  @Autowired
  private CreateActionOrchestrator createActionOrchestrator;

  @Autowired
  private UpdateActionOrchestrator updateActionOrchestrator;

  @Autowired
  private CancelActionOrchestrator cancelActionOrchestrator;

  @Autowired
  private PauseActionOrchestrator pauseActionOrchestrator;

  @Autowired
  private FeatureFlagConfig featureFlagConfig;

  @Transactional
  public void processCreate(FwmtActionInstruction actionInstruction, Instant messageReceivedTime) throws GatewayException {
    if (!isActionAllowedByFeatureFlag(actionInstruction.getCaseId(), actionInstruction.getAddressType(), actionInstruction.getActionInstruction().name())) {
      return;
    }
    createActionOrchestrator.process(actionInstruction, messageReceivedTime);
  }

  @Transactional
  public void processUpdate(FwmtActionInstruction actionInstruction, Instant messageReceivedTime) throws GatewayException {
    if (!isActionAllowedByFeatureFlag(actionInstruction.getCaseId(), actionInstruction.getAddressType(), actionInstruction.getActionInstruction().name())) {
      return;
    }
    updateActionOrchestrator.process(actionInstruction, messageReceivedTime);
  }

  @Transactional
  public void processCancel(FwmtCancelActionInstruction actionInstruction, Instant messageReceivedTime) throws GatewayException {
    if (!isActionAllowedByFeatureFlag(actionInstruction.getCaseId(), actionInstruction.getAddressType(), actionInstruction.getActionInstruction().name())) {
      return;
    }
    cancelActionOrchestrator.process(actionInstruction, messageReceivedTime);
  }

  @Transactional
  public void processPause(FwmtActionInstruction actionInstruction, Instant messageReceivedTime) throws GatewayException {
    if (!isActionAllowedByFeatureFlag(actionInstruction.getCaseId(), actionInstruction.getAddressType(), actionInstruction.getActionInstruction().name())) {
      return;
    }
    pauseActionOrchestrator.process(actionInstruction, messageReceivedTime);
  }

  private boolean isActionAllowedByFeatureFlag(Object caseId, String surveyType, String actionInstruction) {
    if (featureFlagConfig.isInstructionEnabled(surveyType, actionInstruction)) {
      return true;
    }

    eventManager.triggerEvent(String.valueOf(caseId), FEATURE_FLAG_IGNORED,
        "Survey type", String.valueOf(surveyType),
        "Action instruction", String.valueOf(actionInstruction));
    return false;
  }

}
