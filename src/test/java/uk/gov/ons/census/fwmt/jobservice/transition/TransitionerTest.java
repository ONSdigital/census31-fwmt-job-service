package uk.gov.ons.census.fwmt.jobservice.transition;

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
import uk.gov.ons.census.fwmt.jobservice.data.MessageCache;
import uk.gov.ons.census.fwmt.jobservice.enums.TransitionRequestAction;
import uk.gov.ons.census.fwmt.jobservice.service.MessageCacheService;
import uk.gov.ons.census.fwmt.jobservice.service.converter.TransitionRule;
import uk.gov.ons.census.fwmt.jobservice.service.processor.InboundProcessor;
import uk.gov.ons.census.fwmt.jobservice.transition.utils.RetrieveTransitionRules;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransitionerTest {

  @InjectMocks
  private Transitioner transitioner;

  @Mock
  private GatewayEventManager eventManager;

  @Mock
  private MessageCacheService messageCacheService;

  @Mock
  private RetrieveTransitionRules retrieveTransitionRules;

  @Mock
  private NoActionTransitionProcessor<?> noActionTransitionProcessor;

  @Mock
  private RejectTransitionProcessor<?> rejectTransitionProcessor;

  @Mock
  private ProcessTransitionProcessor<?> processTransitionProcessor;

  @Mock
  private MergeTransitionProcessor<?> mergeTransitionProcessor;

  @Mock
  private InboundProcessor<FwmtActionInstruction> actionProcessor;

  @Mock
  private InboundProcessor<FwmtCancelActionInstruction> cancelProcessor;

  @Mock
  private TransitionRequestActionExecutor requestActionExecutor;

  @Test
  void shouldHandleNoActionAndSaveForActionInstruction() throws GatewayException {
    FwmtActionInstruction request = buildActionInstruction();
    GatewayCaseRecord cache = GatewayCaseRecord.builder().caseId(request.getCaseId()).lastActionInstruction("CREATE").build();
    MessageCache messageCache = MessageCache.builder().caseId(request.getCaseId()).build();
    Instant messageTime = Instant.now();

    when(retrieveTransitionRules.collectTransitionRules(any(), anyString(), anyString(), any()))
        .thenReturn(TransitionRule.builder()
            .action(uk.gov.ons.census.fwmt.jobservice.enums.TransitionAction.NO_ACTION)
            .requestAction(TransitionRequestAction.SAVE)
            .build());
    when(messageCacheService.getById(request.getCaseId())).thenReturn(messageCache);

    TransitionAction<FwmtActionInstruction> resolution = transitioner.resolveTransitionAction(
        request, null, cache, messageTime);
    transitioner.apply(resolution);

    verify(noActionTransitionProcessor).execute(any());
    verify(requestActionExecutor).execute(any());
  }

  @Test
  void shouldHandleRejectAndClearForCancelInstruction() throws GatewayException {
    FwmtCancelActionInstruction request = buildCancelInstruction();
    GatewayCaseRecord cache = GatewayCaseRecord.builder().caseId(request.getCaseId()).lastActionInstruction("UPDATE").build();
    MessageCache messageCache = MessageCache.builder().caseId(request.getCaseId()).build();
    Instant messageTime = Instant.now();

    when(retrieveTransitionRules.collectTransitionRules(any(), anyString(), anyString(), any()))
        .thenReturn(TransitionRule.builder()
            .action(uk.gov.ons.census.fwmt.jobservice.enums.TransitionAction.REJECT)
            .requestAction(TransitionRequestAction.CLEAR)
            .build());
    when(messageCacheService.getById(request.getCaseId())).thenReturn(messageCache);

    TransitionAction<FwmtCancelActionInstruction> resolution = transitioner.resolveTransitionAction(
        request, null, cache, messageTime);
    transitioner.apply(resolution);

    verify(rejectTransitionProcessor).execute(any());
    verify(requestActionExecutor).execute(any());
  }

  @Test
  void shouldProcessForProcessAction() throws GatewayException {
    FwmtActionInstruction request = buildActionInstruction();
    GatewayCaseRecord cache = GatewayCaseRecord.builder().caseId(request.getCaseId()).build();
    Instant messageTime = Instant.now();

    when(retrieveTransitionRules.collectTransitionRules(any(), anyString(), anyString(), any()))
        .thenReturn(TransitionRule.builder()
            .action(uk.gov.ons.census.fwmt.jobservice.enums.TransitionAction.PROCESS)
            .requestAction(TransitionRequestAction.NONE)
            .build());

    TransitionAction<FwmtActionInstruction> resolution = transitioner.resolveTransitionAction(
        request, actionProcessor, cache, messageTime);
    transitioner.apply(resolution);

    verify(processTransitionProcessor).execute(any());
    verify(requestActionExecutor).execute(any());
  }

  @Test
  void shouldNotProcessOnMergeWhenCancelInstruction() throws GatewayException {
    FwmtCancelActionInstruction request = buildCancelInstruction();
    GatewayCaseRecord cache = GatewayCaseRecord.builder().caseId(request.getCaseId()).build();
    MessageCache messageCache = MessageCache.builder().caseId(request.getCaseId()).build();
    Instant messageTime = Instant.now();

    when(retrieveTransitionRules.collectTransitionRules(any(), anyString(), anyString(), any()))
        .thenReturn(TransitionRule.builder()
            .action(uk.gov.ons.census.fwmt.jobservice.enums.TransitionAction.MERGE)
            .requestAction(TransitionRequestAction.NONE)
            .build());
    when(messageCacheService.getById(request.getCaseId())).thenReturn(messageCache);

    TransitionAction<FwmtCancelActionInstruction> resolution = transitioner.resolveTransitionAction(
        request, cancelProcessor, cache, messageTime);
    transitioner.apply(resolution);

    verify(mergeTransitionProcessor).execute(any());
    verify(requestActionExecutor).execute(any());
  }

  @Test
  void shouldProcessAndMergeForMergeWhenActionInstruction() throws GatewayException {
    FwmtActionInstruction request = buildActionInstruction();
    GatewayCaseRecord cache = GatewayCaseRecord.builder().caseId(request.getCaseId()).build();
    MessageCache messageCache = MessageCache.builder().caseId(request.getCaseId()).build();
    Instant messageTime = Instant.now();

    when(retrieveTransitionRules.collectTransitionRules(any(), anyString(), anyString(), any()))
        .thenReturn(TransitionRule.builder()
            .action(uk.gov.ons.census.fwmt.jobservice.enums.TransitionAction.MERGE)
            .requestAction(TransitionRequestAction.NONE)
            .build());
    when(messageCacheService.getById(request.getCaseId())).thenReturn(messageCache);

    TransitionAction<FwmtActionInstruction> resolution = transitioner.resolveTransitionAction(
        request, actionProcessor, cache, messageTime);
    transitioner.apply(resolution);

    verify(mergeTransitionProcessor).execute(any());
    verify(requestActionExecutor).execute(any());
  }

  @Test
  void shouldThrowIfMessageTimestampMissing() {
    FwmtActionInstruction request = buildActionInstruction();

    assertThrows(GatewayException.class,
        () -> transitioner.resolveTransitionAction(request, actionProcessor, null, null));
  }

  @Test
  void shouldThrowIfProcessActionHasNoProcessor() throws GatewayException {
    FwmtActionInstruction request = buildActionInstruction();
    GatewayCaseRecord cache = GatewayCaseRecord.builder().caseId(request.getCaseId()).build();
    Instant messageTime = Instant.now();

    when(retrieveTransitionRules.collectTransitionRules(any(), anyString(), anyString(), any()))
        .thenReturn(TransitionRule.builder()
            .action(uk.gov.ons.census.fwmt.jobservice.enums.TransitionAction.PROCESS)
            .requestAction(TransitionRequestAction.NONE)
            .build());

    assertThrows(GatewayException.class,
        () -> transitioner.resolveTransitionAction(request, null, cache, messageTime));
    verify(processTransitionProcessor, never()).execute(any());
    verify(requestActionExecutor, never()).execute(any());
  }

  @Test
  void shouldThrowIfNonCancelMergeHasNoProcessor() throws GatewayException {
    FwmtActionInstruction request = buildActionInstruction();
    GatewayCaseRecord cache = GatewayCaseRecord.builder().caseId(request.getCaseId()).build();
    Instant messageTime = Instant.now();

    when(retrieveTransitionRules.collectTransitionRules(any(), anyString(), anyString(), any()))
        .thenReturn(TransitionRule.builder()
            .action(uk.gov.ons.census.fwmt.jobservice.enums.TransitionAction.MERGE)
            .requestAction(TransitionRequestAction.NONE)
            .build());

    assertThrows(GatewayException.class,
        () -> transitioner.resolveTransitionAction(request, null, cache, messageTime));
    verify(mergeTransitionProcessor, never()).execute(any());
    verify(requestActionExecutor, never()).execute(any());
  }

  private FwmtActionInstruction buildActionInstruction() {
    FwmtActionInstruction request = new FwmtActionInstruction();
    request.setActionInstruction(ActionInstructionType.UPDATE);
    request.setCaseId("ac623e62-4f4b-11eb-ae93-0242ac130002");
    request.setCaseRef("10000000001");
    request.setAddressType("CE");
    request.setAddressLevel("E");
    return request;
  }

  private FwmtCancelActionInstruction buildCancelInstruction() {
    FwmtCancelActionInstruction request = new FwmtCancelActionInstruction();
    request.setActionInstruction(ActionInstructionType.CANCEL);
    request.setCaseId("ac623e62-4f4b-11eb-ae93-0242ac130002");
    request.setAddressType("CE");
    request.setAddressLevel("E");
    return request;
  }
}




