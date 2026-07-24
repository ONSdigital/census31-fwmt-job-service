package uk.gov.ons.census.fwmt.jobservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCaseRecord;
import uk.gov.ons.census.fwmt.jobservice.service.processor.InboundProcessor;
import uk.gov.ons.census.fwmt.jobservice.transition.TransitionAction;
import uk.gov.ons.census.fwmt.jobservice.transition.Transitioner;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TmDispatchServiceTest {

  private static final String CASE_ID = "ac623e62-4f4b-11eb-ae93-0242ac130002";

  @InjectMocks
  private TmDispatchService tmDispatchService;

  @Mock
  private Transitioner transitioner;

  @Mock
  private InboundProcessor<FwmtActionInstruction> actionHandler;

  @Mock
  private InboundProcessor<FwmtCancelActionInstruction> cancelHandler;

  @Test
  void dispatchActionInstruction_resolvesAndExecutesTransitionPlan() throws GatewayException {
    FwmtActionInstruction actionInstruction = buildCreateRequest();
    GatewayCaseRecord cache = GatewayCaseRecord.builder().caseId(CASE_ID).build();
    Instant messageReceivedTime = Instant.now();
    TransitionAction<FwmtActionInstruction> dispatchPlan = TransitionAction.<FwmtActionInstruction>builder().build();

    when(transitioner.resolveTransitionAction(actionInstruction, actionHandler, cache, messageReceivedTime)).thenReturn(dispatchPlan);
    doNothing().when(transitioner).apply(dispatchPlan);

    tmDispatchService.dispatch(actionInstruction, actionHandler, cache, messageReceivedTime);

    verify(transitioner).resolveTransitionAction(actionInstruction, actionHandler, cache, messageReceivedTime);
    verify(transitioner).apply(dispatchPlan);
  }

  @Test
  void dispatchCancelInstruction_resolvesAndExecutesTransitionPlan() throws GatewayException {
    FwmtCancelActionInstruction actionInstruction = buildCancelRequest();
    GatewayCaseRecord cache = GatewayCaseRecord.builder().caseId(CASE_ID).build();
    Instant messageReceivedTime = Instant.now();
    TransitionAction<FwmtCancelActionInstruction> dispatchPlan = TransitionAction.<FwmtCancelActionInstruction>builder().build();

    when(transitioner.resolveTransitionAction(actionInstruction, cancelHandler, cache, messageReceivedTime)).thenReturn(dispatchPlan);
    doNothing().when(transitioner).apply(dispatchPlan);

    tmDispatchService.dispatch(actionInstruction, cancelHandler, cache, messageReceivedTime);

    verify(transitioner).resolveTransitionAction(actionInstruction, cancelHandler, cache, messageReceivedTime);
    verify(transitioner).apply(dispatchPlan);
  }

  @Test
  void dispatchActionInstruction_withoutActionHandler_resolvesAndExecutesTransitionPlan() throws GatewayException {
    FwmtActionInstruction actionInstruction = buildUpdateRequest();
    GatewayCaseRecord cache = GatewayCaseRecord.builder().caseId(CASE_ID).lastActionInstruction("UPDATE(HELD)").build();
    Instant messageReceivedTime = Instant.now();
    TransitionAction<FwmtActionInstruction> dispatchPlan = TransitionAction.<FwmtActionInstruction>builder().build();

    when(transitioner.resolveTransitionAction(eq(actionInstruction), eq(null), eq(cache), eq(messageReceivedTime))).thenReturn(dispatchPlan);
    doNothing().when(transitioner).apply(dispatchPlan);

    tmDispatchService.dispatch(actionInstruction, null, cache, messageReceivedTime);

    verify(transitioner).resolveTransitionAction(eq(actionInstruction), eq(null), eq(cache), eq(messageReceivedTime));
    verify(transitioner).apply(dispatchPlan);
  }

  private FwmtActionInstruction buildCreateRequest() {
    FwmtActionInstruction request = new FwmtActionInstruction();
    request.setActionInstruction(ActionInstructionType.CREATE);
    request.setCaseId(CASE_ID);
    request.setAddressType("HH");
    request.setAddressLevel("U");
    request.setSurveyName("CENSUS");
    return request;
  }

  private FwmtActionInstruction buildUpdateRequest() {
    FwmtActionInstruction request = new FwmtActionInstruction();
    request.setActionInstruction(ActionInstructionType.UPDATE);
    request.setCaseId(CASE_ID);
    request.setAddressType("HH");
    request.setAddressLevel("U");
    request.setSurveyName("CENSUS");
    return request;
  }

  private FwmtCancelActionInstruction buildCancelRequest() {
    FwmtCancelActionInstruction request = new FwmtCancelActionInstruction();
    request.setActionInstruction(ActionInstructionType.CANCEL);
    request.setCaseId(CASE_ID);
    request.setAddressType("HH");
    request.setAddressLevel("U");
    request.setSurveyName("CENSUS");
    return request;
  }
}


