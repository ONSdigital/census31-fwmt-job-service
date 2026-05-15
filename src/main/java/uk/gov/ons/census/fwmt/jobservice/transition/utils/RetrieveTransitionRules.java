package uk.gov.ons.census.fwmt.jobservice.transition.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.service.converter.TransitionRule;
import uk.gov.ons.census.fwmt.jobservice.service.converter.TransitionRulesLookup;

import java.time.Instant;


@Slf4j
@Component
public class RetrieveTransitionRules {

  private static final String TRANSITION_RULE_RETREIVAL_FALIURE = "TRANSITION_RULE_RETREIVAL_FALIURE";

  @Autowired
  private GatewayEventManager eventManager;

  @Autowired
  private TransitionRulesLookup transitionRulesLookup;

  public TransitionRule collectTransitionRules(GatewayCache cache, String actionRequest, String caseId,
      Instant messageReceivedTime) throws GatewayException {
    String cacheType;
    String recordAge;

    if (cache == null) {
      cacheType = "EMPTY";
      recordAge = "NEWER";
    } else {
      String lastActionType = cache.lastActionInstruction;
      if(lastActionType != null && !lastActionType.isEmpty()) {
        cacheType = cache.lastActionInstruction;
      } else {
        cacheType = "EMPTY";
      }
      recordAge = checkRecordAge(cache, messageReceivedTime);
    }

    TransitionRule returnedRules = transitionRulesLookup.getLookup(cacheType, actionRequest, recordAge);

    if (returnedRules == null) {
      eventManager.triggerErrorEvent(this.getClass(), "Could not find a rule for the create request from RM",
          String.valueOf(caseId), TRANSITION_RULE_RETREIVAL_FALIURE, "cacheType", cacheType, "actionRequest", actionRequest, "recordAge", recordAge);
      throw new GatewayException(GatewayException.Fault.VALIDATION_FAILED,
          "Could not find a rule for the create request from RM", cache);
    }
    return returnedRules;
  }

  public String checkRecordAge(GatewayCache gatewayCache, Instant messageReceivedTime) {
    String recordAge = "";
    Instant lastActionTime = gatewayCache.getLastActionTime();
    if (lastActionTime != null && lastActionTime.compareTo(messageReceivedTime) >= 0) {
      recordAge = "OLDER";
    } else {
      recordAge = "NEWER";
    }
    return recordAge;
  }
}