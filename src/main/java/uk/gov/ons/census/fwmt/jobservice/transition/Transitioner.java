package uk.gov.ons.census.fwmt.jobservice.transition;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCaseRecord;
import uk.gov.ons.census.fwmt.jobservice.data.MessageCache;
import uk.gov.ons.census.fwmt.jobservice.service.MessageCacheService;
import uk.gov.ons.census.fwmt.jobservice.service.converter.TransitionRule;
import uk.gov.ons.census.fwmt.jobservice.service.processor.InboundProcessor;
import uk.gov.ons.census.fwmt.jobservice.transition.utils.RetrieveTransitionRules;

import java.time.Instant;

@Slf4j
@Component
public class Transitioner {
  private static final String PRE_TRANSITION = "PRE_TRANSITION";
  private static final String POST_TRANSITION = "POST_TRANSITION";

  @Autowired
  private GatewayEventManager eventManager;

  @Autowired
  private MessageCacheService messageCacheService;

  @Autowired
  private RetrieveTransitionRules retrieveTransitionRules;

  @Autowired
  private NoActionTransitionProcessor<?> noActionTransitionProcessor;

  @Autowired
  private RejectTransitionProcessor<?> rejectTransitionProcessor;

  @Autowired
  private ProcessTransitionProcessor<?> processTransitionProcessor;

  @Autowired
  private MergeTransitionProcessor<?> mergeTransitionProcessor;

  @Autowired
  private TransitionRequestActionExecutor requestActionExecutor;

  public TransitionAction<FwmtActionInstruction> resolveTransitionAction(
      FwmtActionInstruction rmRequest, InboundProcessor<FwmtActionInstruction> processor,
      GatewayCaseRecord cache, Instant messageQueueTime) throws GatewayException {
    return resolveTransitionInternal(
        rmRequest,
        processor,
        cache,
        messageQueueTime,
        rmRequest.getCaseId(),
        rmRequest.getCaseRef(),
        rmRequest.getActionInstruction().toString(),
        false);
  }

  public TransitionAction<FwmtCancelActionInstruction> resolveTransitionAction(
      FwmtCancelActionInstruction rmRequest, InboundProcessor<FwmtCancelActionInstruction> processor,
      GatewayCaseRecord cache, Instant messageQueueTime) throws GatewayException {
    return resolveTransitionInternal(
        rmRequest,
        processor,
        cache,
        messageQueueTime,
        rmRequest.getCaseId(),
        "",
        rmRequest.getActionInstruction().toString(),
        true);
  }

  public <T> void apply(TransitionAction<T> resolution) throws GatewayException {
    resolution.getProcessor().execute(resolution.getContext());
    requestActionExecutor.execute(resolution.getContext());
  }

  private <T> TransitionAction<T> resolveTransitionInternal(T rmRequest,
                                                            InboundProcessor<T> processor, GatewayCaseRecord cache, Instant messageQueueTime, String caseId,
                                                            String caseRef, String actionInstruction, boolean isCancel) throws GatewayException {
    triggerPreTransitionEvent(caseId, caseRef, actionInstruction, cache);
    validateMessageQueueTime(messageQueueTime, caseId);

    TransitionRule transitionRule = retrieveTransitionRules.collectTransitionRules(cache, actionInstruction, caseId, messageQueueTime);
    MessageCache messageCache = messageCacheService.getById(caseId);

    triggerPostTransitionEvent(caseId, transitionRule);

    TransitionContext<T> context = TransitionContext.<T>builder()
        .caseId(caseId)
        .caseRef(caseRef)
        .actionInstruction(actionInstruction)
        .request(rmRequest)
        .inboundProcessor(processor)
        .gatewayCaseRecord(cache)
        .messageCache(messageCache)
        .transitionRule(transitionRule)
        .messageQueueTime(messageQueueTime)
        .cancel(isCancel)
        .build();

    return TransitionAction.<T>builder()
        .processor(selectTransitionProcessor(context))
        .context(context)
        .build();
  }

  private void validateMessageQueueTime(Instant messageQueueTime, String caseId)
      throws GatewayException {
    if (messageQueueTime == null) {
      throw new GatewayException(GatewayException.Fault.VALIDATION_FAILED,
          "Message did not include a timestamp", caseId);
    }
  }

  private void triggerPreTransitionEvent(String caseId, String caseRef, String actionInstruction,
      GatewayCaseRecord cache) {
    eventManager.triggerEvent(caseId, PRE_TRANSITION,
        "Case Reference", caseRef,
        "Action Instruction", actionInstruction,
        "Cached Action Instruction",
        (cache != null && cache.getLastActionInstruction() != null ? cache.getLastActionInstruction()
            : "no cache"));
  }

  private void triggerPostTransitionEvent(String caseId, TransitionRule returnedRules) {
    eventManager.triggerEvent(caseId, POST_TRANSITION,
        "Action type", returnedRules.getAction().toString(),
        "Request action", returnedRules.getRequestAction().toString());
  }

  private <T> TransitionProcessor<T> selectTransitionProcessor(TransitionContext<T> context)
      throws GatewayException {
      return switch (context.getTransitionRule().getAction()) {
          case NO_ACTION -> castTransitionProcessor(noActionTransitionProcessor);
          case REJECT -> castTransitionProcessor(rejectTransitionProcessor);
          case PROCESS -> {
              if (context.getInboundProcessor() == null) {
                  throw new GatewayException(GatewayException.Fault.VALIDATION_FAILED,
                          "No processor available for transition action PROCESS", context.getCaseId());
              }
              yield castTransitionProcessor(processTransitionProcessor);
          }
          case MERGE -> {
              if (!context.isCancel() && context.getInboundProcessor() == null) {
                  throw new GatewayException(GatewayException.Fault.VALIDATION_FAILED,
                          "No processor available for transition action MERGE", context.getCaseId());
              }
              yield castTransitionProcessor(mergeTransitionProcessor);
          }
          default -> throw new GatewayException(GatewayException.Fault.VALIDATION_FAILED,
                  "No such transition rule", context.getCaseId());
      };
  }

  @SuppressWarnings("unchecked")
  private <T> TransitionProcessor<T> castTransitionProcessor(TransitionProcessor<?> processor) {
    return (TransitionProcessor<T>) processor;
  }
}