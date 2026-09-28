package uk.gov.ons.census.fwmt.jobservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstruction;
import uk.gov.ons.census.fwmt.common.rm.dto.CancelActionInstruction;
import uk.gov.ons.census.fwmt.common.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.config.FeatureFlagConfig;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.FEATURE_FLAG_IGNORED;

@ExtendWith(MockitoExtension.class)
class JobServiceRoutingTest {

  private static final String CASE_ID = "ac623e62-4f4b-11eb-ae93-0242ac130002";

  @InjectMocks
  private JobService jobService;

  @Mock private GatewayEventManager eventManager;
  @Mock private FeatureFlagConfig featureFlagConfig;
  @Mock private CreateActionOrchestrator createActionOrchestrator;
  @Mock private UpdateActionOrchestrator updateActionOrchestrator;
  @Mock private CancelActionOrchestrator cancelActionOrchestrator;
  @Mock private PauseActionOrchestrator pauseActionOrchestrator;

  @Test
  void processCreate_featureFlagDisabled_doesNotDelegateAndTriggersIgnoredEvent() throws GatewayException {
    ActionInstruction request = buildCreateRequest();
    when(featureFlagConfig.isInstructionEnabled(anyString(), anyString())).thenReturn(false);

    jobService.processCreate(request, Instant.now());

    verify(createActionOrchestrator, never()).process(any(), any());
    verify(eventManager).triggerEvent(eq(CASE_ID), eq(FEATURE_FLAG_IGNORED), anyString(), anyString(), anyString(), anyString());
  }

  @Test
  void processCreate_featureFlagEnabled_delegatesToCreateOrchestrator() throws GatewayException {
    ActionInstruction request = buildCreateRequest();
    Instant messageTime = Instant.now();
    when(featureFlagConfig.isInstructionEnabled(anyString(), anyString())).thenReturn(true);

    jobService.processCreate(request, messageTime);

    verify(createActionOrchestrator).process(request, messageTime);
  }

  @Test
  void processUpdate_featureFlagEnabled_delegatesToUpdateOrchestrator() throws GatewayException {
    ActionInstruction request = buildUpdateRequest("HH", false);
    Instant messageTime = Instant.now();
    when(featureFlagConfig.isInstructionEnabled(anyString(), anyString())).thenReturn(true);

    jobService.processUpdate(request, messageTime);

    verify(updateActionOrchestrator).process(request, messageTime);
  }

  @Test
  void processCancel_featureFlagEnabled_delegatesToCancelOrchestrator() throws GatewayException {
    CancelActionInstruction request = buildCancelRequest();
    Instant messageTime = Instant.now();
    when(featureFlagConfig.isInstructionEnabled(anyString(), anyString())).thenReturn(true);

    jobService.processCancel(request, messageTime);

    verify(cancelActionOrchestrator).process(request, messageTime);
  }

  @Test
  void processPause_featureFlagEnabled_delegatesToPauseOrchestrator() throws GatewayException {
    ActionInstruction request = buildPauseRequest();
    Instant messageTime = Instant.now();
    when(featureFlagConfig.isInstructionEnabled(anyString(), anyString())).thenReturn(true);

    jobService.processPause(request, messageTime);

    verify(pauseActionOrchestrator).process(request, messageTime);
  }

  private ActionInstruction buildCreateRequest() {
    ActionInstruction request = new ActionInstruction();
    request.setActionInstruction(ActionInstructionType.CREATE);
    request.setCaseId(CASE_ID);
    request.setAddressType("HH");
    request.setSurveyName("CENSUS");
    return request;
  }

  private ActionInstruction buildUpdateRequest(String addressType, boolean undeliveredAsAddress) {
    ActionInstruction request = new ActionInstruction();
    request.setActionInstruction(ActionInstructionType.UPDATE);
    request.setCaseId(CASE_ID);
    request.setAddressType(addressType);
    request.setAddressLevel("U");
    request.setSurveyName("CENSUS");
    request.setUndeliveredAsAddress(undeliveredAsAddress);
    return request;
  }

  private ActionInstruction buildPauseRequest() {
    ActionInstruction request = new ActionInstruction();
    request.setActionInstruction(ActionInstructionType.PAUSE);
    request.setCaseId(CASE_ID);
    request.setAddressType("HH");
    request.setAddressLevel("U");
    request.setSurveyName("CENSUS");
    return request;
  }

  private CancelActionInstruction buildCancelRequest() {
    CancelActionInstruction request = new CancelActionInstruction();
    request.setActionInstruction(ActionInstructionType.CANCEL);
    request.setCaseId(CASE_ID);
    request.setAddressType("HH");
    request.setAddressLevel("U");
    request.setSurveyName("CENSUS");
    return request;
  }
}
