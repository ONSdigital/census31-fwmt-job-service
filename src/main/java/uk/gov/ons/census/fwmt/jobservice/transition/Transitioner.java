package uk.gov.ons.census.fwmt.jobservice.transition;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.data.MessageCache;
import uk.gov.ons.census.fwmt.jobservice.service.MessageCacheService;
import uk.gov.ons.census.fwmt.jobservice.service.converter.TransitionRule;
import uk.gov.ons.census.fwmt.jobservice.service.processor.InboundProcessor;
import uk.gov.ons.census.fwmt.jobservice.transition.utils.CacheHeldMessages;
import uk.gov.ons.census.fwmt.jobservice.transition.utils.MergeMessages;
import uk.gov.ons.census.fwmt.jobservice.transition.utils.RetrieveTransitionRules;

import java.time.Instant;

import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.NO_ACTION_REQUIRED;

@Slf4j
@Component
public class Transitioner {
  private static final String REJECTED_RM_REQUEST = "REJECTED_RM_REQUEST";
  private static final String PRE_TRANSITION = "PRE_TRANSITION";
  private static final String POST_TRANSITION = "POST_TRANSITION";

  @Autowired
  private GatewayEventManager eventManager;

  @Autowired
  private MessageCacheService messageCacheService;

  @Autowired
  private CacheHeldMessages cacheHeldMessages;

  @Autowired
  private RetrieveTransitionRules retrieveTransitionRules;

  @Autowired
  private MergeMessages mergeMessages;

  public void processTransition(GatewayCache cache, Object rmRequestReceived,
        InboundProcessor<?> processor, Instant messageQueueTime) throws GatewayException {
    boolean isCancel = false;
    String actionInstruction;
    String caseId;
    String caseRef;
    FwmtActionInstruction rmRequestCreateUpdate = null;
    FwmtCancelActionInstruction rmRequestCancel = null;
    InboundProcessor<FwmtActionInstruction> processorCreateUpdate = null;
    InboundProcessor<FwmtCancelActionInstruction> processorCancel = null;

    if (rmRequestReceived instanceof FwmtActionInstruction) {
      rmRequestCreateUpdate = (FwmtActionInstruction) rmRequestReceived;
      processorCreateUpdate = (InboundProcessor<FwmtActionInstruction>) processor;
      actionInstruction = rmRequestCreateUpdate.getActionInstruction().toString();
      caseId = rmRequestCreateUpdate.getCaseId();
      caseRef = rmRequestCreateUpdate.getCaseRef();
    } else {
      rmRequestCancel = (FwmtCancelActionInstruction) rmRequestReceived;
      processorCancel = (InboundProcessor<FwmtCancelActionInstruction>) processor;
      actionInstruction = rmRequestCancel.getActionInstruction().toString();
      caseId = rmRequestCancel.getCaseId();
      caseRef = "";
      isCancel = true;
    }

    eventManager.triggerEvent(caseId, PRE_TRANSITION,
        "Case Reference", caseRef,
        "Action Instruction", actionInstruction,
        "Cached Action Instruction", (cache != null && cache.getLastActionInstruction() !=null ? cache.getLastActionInstruction() : "no cache"));

    if (messageQueueTime == null) {
      throw new GatewayException(GatewayException.Fault.VALIDATION_FAILED, "Message did not include a timestamp", caseId);
    }

    TransitionRule returnedRules = retrieveTransitionRules
        .collectTransitionRules(cache, actionInstruction, caseId, messageQueueTime);

    MessageCache messageCache = messageCacheService.getById(caseId);

    eventManager.triggerEvent(caseId, POST_TRANSITION,
        "Action type", returnedRules.getAction().toString(),
        "Request action", returnedRules.getRequestAction().toString());

    switch (returnedRules.getAction()) {
      case NO_ACTION:
        eventManager
            .triggerEvent(caseId, NO_ACTION_REQUIRED,
                "Case Ref", caseRef);
        break;
      case REJECT:
        eventManager.triggerErrorEvent(this.getClass(), "Request from RM rejected",
            String.valueOf(caseId), REJECTED_RM_REQUEST);
        break;
      case PROCESS:
        if (isCancel) {
          processorCancel.process(rmRequestCancel, cache, messageQueueTime);
        } else {
          processorCreateUpdate.process(rmRequestCreateUpdate, cache, messageQueueTime);
        }
        break;
      case MERGE:
        if (!isCancel) {
          processorCreateUpdate.process(rmRequestCreateUpdate, cache, messageQueueTime);
        }
        mergeMessages.mergeRecords(messageCache);
        break;
      default:
        throw new GatewayException(GatewayException.Fault.VALIDATION_FAILED, "No such transition rule", caseId);
    }

  switch (returnedRules.getRequestAction()) {
    case SAVE:
      if (isCancel) {
        cacheHeldMessages.cacheMessage(messageCache, cache, rmRequestCancel, messageQueueTime);
      } else {
        cacheHeldMessages.cacheMessage(messageCache, cache, rmRequestCreateUpdate, messageQueueTime);
      }
      break;
    case CLEAR:
      if (messageCache != null) {
        messageCacheService.delete(messageCache);
      }
      break;
    default:
      break;
    }
  }
}