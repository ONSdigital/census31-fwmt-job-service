package uk.gov.ons.census.fwmt.jobservice.transition.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.data.MessageCache;
import uk.gov.ons.census.fwmt.jobservice.service.GatewayCacheService;
import uk.gov.ons.census.fwmt.jobservice.service.MessageCacheService;

import java.time.Instant;

import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.MESSAGE_HELD;

@Slf4j
@Component
public class CacheHeldMessages {

  @Autowired
  private GatewayCacheService cacheService;

  @Autowired
  private GatewayEventManager eventManager;

  @Autowired
  private MessageCacheService messageCacheService;

  public void cacheMessage(MessageCache messageCache, GatewayCache cache, Object rmRequest, Instant messageQueueTime) {
    int type = 0;
    String actionInstruction;
    String addressLevel;
    String addressType = "";
    String caseId;
    String message;
    ObjectMapper rmRequestMapper = new ObjectMapper();

    if (rmRequest instanceof FwmtActionInstruction) {
      FwmtActionInstruction requestReceived = (FwmtActionInstruction) rmRequest;
      caseId = requestReceived.getCaseId();
      actionInstruction = "UPDATE(HELD)";
      addressLevel = requestReceived.getAddressLevel();
    } else {
      FwmtCancelActionInstruction requestReceived = (FwmtCancelActionInstruction) rmRequest;
      caseId = requestReceived.getCaseId();
      actionInstruction = "CANCEL(HELD)";
      addressLevel = requestReceived.getAddressLevel();
    }

    if ("E".equals(addressLevel)) {
      type = 1;
    } else if ("U".equals(addressLevel)) {
      type = 3;
    }

    if (cache == null) {
      cacheService.save(GatewayCache.builder().caseId(caseId).existsInFwmt(false)
          .lastActionTime(messageQueueTime).lastActionInstruction(actionInstruction).type(type).build());
    } else {
      cacheService.save(cache.toBuilder().lastActionTime(messageQueueTime)
          .lastActionInstruction(actionInstruction).build());
    }

    try {
      message = rmRequestMapper.writeValueAsString(rmRequest);
    } catch (JsonProcessingException processFailure) {
      return;
    }
    if (messageCache == null) {
      messageCacheService.save(
          MessageCache.builder().caseId(caseId).messageType(actionInstruction).message(message)
              .build());
    } else {
      messageCacheService.save(
          messageCache.toBuilder().messageType(actionInstruction).message(message).build());
    }

    eventManager
        .triggerEvent(caseId, MESSAGE_HELD,
            "Message type: ", actionInstruction);
  }
}