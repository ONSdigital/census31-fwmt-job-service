package uk.gov.ons.census.fwmt.jobservice.transition;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCaseRecord;
import uk.gov.ons.census.fwmt.jobservice.data.MessageCache;
import uk.gov.ons.census.fwmt.jobservice.service.processor.InboundProcessor;
import uk.gov.ons.census.fwmt.jobservice.transition.utils.MergeMessages;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.NO_ACTION_REQUIRED;

@ExtendWith(MockitoExtension.class)
class TransitionProcessorsTest {

  @Mock
  private GatewayEventManager eventManager;

  @Mock
  private MergeMessages mergeMessages;

  @Mock
  private InboundProcessor<String> inboundProcessor;

  @Test
  void shouldTriggerNoActionEvent() throws GatewayException {
    NoActionTransitionProcessor<String> processor = new NoActionTransitionProcessor<>(eventManager);
    TransitionContext<String> context = baseContext().caseRef("10000000001").build();

    processor.execute(context);

    verify(eventManager).triggerEvent(context.getCaseId(), NO_ACTION_REQUIRED, "Case Ref", "10000000001");
  }

  @Test
  void shouldTriggerRejectEvent() throws GatewayException {
    RejectTransitionProcessor<String> processor = new RejectTransitionProcessor<>(eventManager);
    TransitionContext<String> context = baseContext().build();

    processor.execute(context);

    verify(eventManager).triggerErrorEvent(Transitioner.class, "Request from RM rejected",
        context.getCaseId(), "REJECTED_RM_REQUEST");
  }

  @Test
  void shouldProcessWhenProcessorProvided() throws GatewayException {
    ProcessTransitionProcessor<String> processor = new ProcessTransitionProcessor<>();
    TransitionContext<String> context = baseContext().inboundProcessor(inboundProcessor).build();

    processor.execute(context);

    verify(inboundProcessor).process(context.getRequest(), context.getGatewayCaseRecord(), context.getMessageQueueTime());
  }

  @Test
  void shouldFailProcessWhenProcessorMissing() {
    ProcessTransitionProcessor<String> processor = new ProcessTransitionProcessor<>();
    TransitionContext<String> context = baseContext().inboundProcessor(null).build();

    assertThrows(GatewayException.class, () -> processor.execute(context));
  }

  @Test
  void shouldProcessThenMergeForNonCancel() throws GatewayException {
    MergeTransitionProcessor<String> processor = new MergeTransitionProcessor<>(mergeMessages);
    TransitionContext<String> context = baseContext().inboundProcessor(inboundProcessor).cancel(false).build();

    processor.execute(context);

    verify(inboundProcessor).process(context.getRequest(), context.getGatewayCaseRecord(), context.getMessageQueueTime());
    verify(mergeMessages).mergeRecords(context.getMessageCache());
  }

  @Test
  void shouldOnlyMergeForCancel() throws GatewayException {
    MergeTransitionProcessor<String> processor = new MergeTransitionProcessor<>(mergeMessages);
    TransitionContext<String> context = baseContext().inboundProcessor(inboundProcessor).cancel(true).build();

    processor.execute(context);

    verify(inboundProcessor, never()).process(context.getRequest(), context.getGatewayCaseRecord(), context.getMessageQueueTime());
    verify(mergeMessages).mergeRecords(context.getMessageCache());
  }

  @Test
  void shouldFailMergeWhenNonCancelAndProcessorMissing() {
    MergeTransitionProcessor<String> processor = new MergeTransitionProcessor<>(mergeMessages);
    TransitionContext<String> context = baseContext().inboundProcessor(null).cancel(false).build();

    assertThrows(GatewayException.class, () -> processor.execute(context));
  }

  private TransitionContext.TransitionContextBuilder<String> baseContext() {
    return TransitionContext.<String>builder()
        .caseId("ac623e62-4f4b-11eb-ae93-0242ac130002")
        .caseRef("")
        .actionInstruction("UPDATE")
        .request("request")
        .gatewayCaseRecord(GatewayCaseRecord.builder().caseId("ac623e62-4f4b-11eb-ae93-0242ac130002").build())
        .messageCache(MessageCache.builder().caseId("ac623e62-4f4b-11eb-ae93-0242ac130002").build())
        .messageQueueTime(Instant.now())
        .cancel(false);
  }
}

