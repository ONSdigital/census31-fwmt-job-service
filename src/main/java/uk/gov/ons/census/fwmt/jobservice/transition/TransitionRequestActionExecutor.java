package uk.gov.ons.census.fwmt.jobservice.transition;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.jobservice.data.MessageCache;
import uk.gov.ons.census.fwmt.jobservice.service.MessageCacheService;
import uk.gov.ons.census.fwmt.jobservice.transition.utils.CacheHeldMessages;

@Component
@RequiredArgsConstructor
public class TransitionRequestActionExecutor {

  private final CacheHeldMessages cacheHeldMessages;
  private final MessageCacheService messageCacheService;

  public <T> void execute(TransitionContext<T> context) {
    switch (context.getTransitionRule().getRequestAction()) {
      case SAVE:
        cacheHeldMessages.cacheMessage(context.getMessageCache(), context.getGatewayCaseRecord(),
            context.getRequest(), context.getMessageQueueTime());
        break;
      case CLEAR:
        MessageCache messageCache = context.getMessageCache();
        if (messageCache != null) {
          messageCacheService.delete(messageCache);
        }
        break;
      default:
        break;
    }
  }
}

