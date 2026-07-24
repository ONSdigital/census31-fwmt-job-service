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
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCaseRecord;
import uk.gov.ons.census.fwmt.jobservice.messaging.RmFieldMessagePublisher;
import uk.gov.ons.census.fwmt.jobservice.service.processor.InboundProcessor;
import uk.gov.ons.census.fwmt.jobservice.service.processor.ProcessorRouter;
import uk.gov.ons.census.fwmt.jobservice.service.routing.ignore.CeUpdateIgnoreProcessor;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActionOrchestratorRoutingTest {

  private static final String CASE_ID = "ac623e62-4f4b-11eb-ae93-0242ac130002";

  @InjectMocks private CreateActionOrchestrator createActionOrchestrator;
  @InjectMocks private UpdateActionOrchestrator updateActionOrchestrator;
  @InjectMocks private CancelActionOrchestrator cancelActionOrchestrator;
  @InjectMocks private PauseActionOrchestrator pauseActionOrchestrator;

  @Mock private GatewayCaseRecordService cacheService;
  @Mock private TmDispatchService tmDispatchService;
  @Mock private GatewayEventManager eventManager;
  @Mock private RmFieldMessagePublisher rmFieldPublisher;
  @Mock private CeUpdateIgnoreProcessor ceUpdateIgnoreProcessor;

  @Mock private ProcessorRouter<FwmtActionInstruction> createRouter;
  @Mock private ProcessorRouter<FwmtActionInstruction> updateRouter;
  @Mock private ProcessorRouter<FwmtCancelActionInstruction> cancelRouter;
  @Mock private ProcessorRouter<FwmtActionInstruction> pauseRouter;

  @Mock private InboundProcessor<FwmtActionInstruction> actionHandler;
  @Mock private InboundProcessor<FwmtCancelActionInstruction> cancelHandler;

  @Test
  void create_singleHandler_dispatches() throws GatewayException {
    FwmtActionInstruction request = buildActionRequest(ActionInstructionType.CREATE, "HH");
    Instant messageTime = Instant.now();

    when(cacheService.getById(CASE_ID)).thenReturn(null);
    when(createRouter.resolveExactlyOne(any(), any(), any())).thenReturn(actionHandler);

    createActionOrchestrator.process(request, messageTime);

    verify(tmDispatchService).dispatch(request, actionHandler, null, messageTime);
  }

  @Test
  void update_undeliveredHhWithoutCache_republishesAsCreate() throws GatewayException {
    FwmtActionInstruction request = buildActionRequest(ActionInstructionType.UPDATE, "HH");
    request.setUndeliveredAsAddress(true);

    when(cacheService.getById(CASE_ID)).thenReturn(null);

    updateActionOrchestrator.process(request, Instant.now());

    verify(rmFieldPublisher).publish(request);
    verify(updateRouter, never()).resolveOptional(any(), any(), any());
  }

  @Test
  void update_withHandler_dispatches() throws GatewayException {
    FwmtActionInstruction request = buildActionRequest(ActionInstructionType.UPDATE, "HH");
    Instant messageTime = Instant.now();
    GatewayCaseRecord cache = GatewayCaseRecord.builder().caseId(CASE_ID).build();

    when(cacheService.getById(CASE_ID)).thenReturn(cache);
    when(updateRouter.resolveOptional(any(), any(), any())).thenReturn(Optional.of(actionHandler));

    updateActionOrchestrator.process(request, messageTime);

    verify(tmDispatchService).dispatch(request, actionHandler, cache, messageTime);
  }

  @Test
  void update_noHandlerHeld_dispatchesWithNullHandler() throws GatewayException {
    FwmtActionInstruction request = buildActionRequest(ActionInstructionType.UPDATE, "HH");
    Instant messageTime = Instant.now();
    GatewayCaseRecord cache = GatewayCaseRecord.builder().caseId(CASE_ID).lastActionInstruction("UPDATE(HELD)").build();

    when(cacheService.getById(CASE_ID)).thenReturn(cache);
    when(updateRouter.resolveOptional(any(), any(), any())).thenReturn(Optional.empty());

    updateActionOrchestrator.process(request, messageTime);

    verify(tmDispatchService).dispatch(request, null, cache, messageTime);
  }

  @Test
  void update_noHandlerCeUpdate_invokesIgnoreProcessor() throws GatewayException {
    FwmtActionInstruction request = buildActionRequest(ActionInstructionType.UPDATE, "CE");
    GatewayCaseRecord cache = GatewayCaseRecord.builder().caseId(CASE_ID).lastActionInstruction("PROCESS").build();

    when(cacheService.getById(CASE_ID)).thenReturn(cache);
    when(updateRouter.resolveOptional(any(), any(), any())).thenReturn(Optional.empty());

    updateActionOrchestrator.process(request, Instant.now());

    verify(ceUpdateIgnoreProcessor).process(request);
    verify(tmDispatchService, never()).dispatch(any(FwmtActionInstruction.class), any(), any(), any());
  }

  @Test
  void update_noHandlerNonHeldNonCe_throwsRoutingError() throws GatewayException {
    FwmtActionInstruction request = buildActionRequest(ActionInstructionType.UPDATE, "HH");
    GatewayCaseRecord cache = GatewayCaseRecord.builder().caseId(CASE_ID).lastActionInstruction("PROCESS").build();

    when(cacheService.getById(CASE_ID)).thenReturn(cache);
    when(updateRouter.resolveOptional(any(), any(), any())).thenReturn(Optional.empty());
    doThrow(new GatewayException(GatewayException.Fault.VALIDATION_FAILED, "routing failed"))
        .when(updateRouter).throwRoutingError(any(), any());

    assertThrows(GatewayException.class, () -> updateActionOrchestrator.process(request, Instant.now()));
    verify(updateRouter).throwRoutingError(eq(request), eq(cache));
  }

  @Test
  void cancel_originalCaseMatch_setsNcAndDispatches() throws GatewayException {
    FwmtCancelActionInstruction request = buildCancelRequest();
    Instant messageTime = Instant.now();
    GatewayCaseRecord cache = GatewayCaseRecord.builder().caseId("original-id").build();

    when(cacheService.getByOriginalCaseId(CASE_ID)).thenReturn(cache);
    when(cancelRouter.resolveOptional(any(), any(), any())).thenReturn(Optional.of(cancelHandler));

    cancelActionOrchestrator.process(request, messageTime);

    verify(tmDispatchService).dispatch(request, cancelHandler, cache, messageTime);
  }

  @Test
  void cancel_noHandlerCancelHeld_dispatchesWithNullHandler() throws GatewayException {
    FwmtCancelActionInstruction request = buildCancelRequest();
    Instant messageTime = Instant.now();
    GatewayCaseRecord cache = GatewayCaseRecord.builder().caseId(CASE_ID).lastActionInstruction("CANCEL(HELD)").build();

    when(cacheService.getByOriginalCaseId(CASE_ID)).thenReturn(null);
    when(cacheService.getById(CASE_ID)).thenReturn(cache);
    when(cancelRouter.resolveOptional(any(), any(), any())).thenReturn(Optional.empty());

    cancelActionOrchestrator.process(request, messageTime);

    verify(tmDispatchService).dispatch(request, null, cache, messageTime);
  }

  @Test
  void pause_singleHandler_processesDirectly() throws GatewayException {
    FwmtActionInstruction request = buildActionRequest(ActionInstructionType.PAUSE, "HH");
    Instant messageTime = Instant.now();
    GatewayCaseRecord cache = GatewayCaseRecord.builder().caseId(CASE_ID).build();

    when(cacheService.getById(CASE_ID)).thenReturn(cache);
    when(pauseRouter.resolveExactlyOne(any(), any(), any())).thenReturn(actionHandler);

    pauseActionOrchestrator.process(request, messageTime);

    verify(actionHandler).process(request, cache, messageTime);
  }

  private FwmtActionInstruction buildActionRequest(ActionInstructionType action, String addressType) {
    FwmtActionInstruction request = new FwmtActionInstruction();
    request.setActionInstruction(action);
    request.setCaseId(CASE_ID);
    request.setAddressType(addressType);
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



