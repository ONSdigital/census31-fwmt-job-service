package uk.gov.ons.census.fwmt.jobservice.transition;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCaseRecord;
import uk.gov.ons.census.fwmt.jobservice.data.MessageCache;
import uk.gov.ons.census.fwmt.jobservice.enums.TransitionAction;
import uk.gov.ons.census.fwmt.jobservice.enums.TransitionRequestAction;
import uk.gov.ons.census.fwmt.jobservice.service.MessageCacheService;
import uk.gov.ons.census.fwmt.jobservice.service.converter.TransitionRule;
import uk.gov.ons.census.fwmt.jobservice.transition.utils.CacheHeldMessages;

import java.time.Instant;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransitionRequestActionExecutorTest {

  @InjectMocks
  private TransitionRequestActionExecutor executor;

  @Mock
  private CacheHeldMessages cacheHeldMessages;

  @Mock
  private MessageCacheService messageCacheService;

  @Test
  void shouldCacheMessageOnSaveRequestAction() {
    FwmtActionInstruction request = new FwmtActionInstruction();
    request.setActionInstruction(ActionInstructionType.UPDATE);
    request.setCaseId("ac623e62-4f4b-11eb-ae93-0242ac130002");
    request.setAddressType("CE");
    request.setAddressLevel("E");

    GatewayCaseRecord gatewayCache = GatewayCaseRecord.builder().caseId(request.getCaseId()).build();
    MessageCache messageCache = MessageCache.builder().caseId(request.getCaseId()).build();
    Instant messageTime = Instant.now();

    TransitionContext<FwmtActionInstruction> context = TransitionContext.<FwmtActionInstruction>builder()
        .caseId(request.getCaseId())
        .caseRef("10000000001")
        .actionInstruction("UPDATE")
        .request(request)
        .gatewayCaseRecord(gatewayCache)
        .messageCache(messageCache)
        .transitionRule(TransitionRule.builder()
            .action(TransitionAction.NO_ACTION)
            .requestAction(TransitionRequestAction.SAVE)
            .build())
        .messageQueueTime(messageTime)
        .cancel(false)
        .build();

    executor.execute(context);

    verify(cacheHeldMessages).cacheMessage(messageCache, gatewayCache, request, messageTime);
  }

  @Test
  void shouldDeleteMessageCacheOnClearRequestAction() {
    MessageCache messageCache = MessageCache.builder().caseId("ac623e62-4f4b-11eb-ae93-0242ac130002").build();

    TransitionContext<String> context = TransitionContext.<String>builder()
        .caseId("ac623e62-4f4b-11eb-ae93-0242ac130002")
        .caseRef("")
        .actionInstruction("CANCEL")
        .request("request")
        .messageCache(messageCache)
        .transitionRule(TransitionRule.builder()
            .action(TransitionAction.REJECT)
            .requestAction(TransitionRequestAction.CLEAR)
            .build())
        .messageQueueTime(Instant.now())
        .cancel(true)
        .build();

    executor.execute(context);

    verify(messageCacheService).delete(messageCache);
  }

  @Test
  void shouldDoNothingOnNoneRequestAction() {
    TransitionContext<String> context = TransitionContext.<String>builder()
        .caseId("ac623e62-4f4b-11eb-ae93-0242ac130002")
        .caseRef("")
        .actionInstruction("UPDATE")
        .request("request")
        .transitionRule(TransitionRule.builder()
            .action(TransitionAction.PROCESS)
            .requestAction(TransitionRequestAction.NONE)
            .build())
        .messageQueueTime(Instant.now())
        .cancel(false)
        .build();

    executor.execute(context);

    verify(cacheHeldMessages, never()).cacheMessage(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    verify(messageCacheService, never()).delete(org.mockito.ArgumentMatchers.any());
  }
}

