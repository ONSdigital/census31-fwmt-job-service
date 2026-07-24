package uk.gov.ons.census.fwmt.jobservice.transition;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;

import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.NO_ACTION_REQUIRED;

@Component
@RequiredArgsConstructor
public class NoActionTransitionProcessor<T> implements TransitionProcessor<T> {

  private final GatewayEventManager eventManager;

  @Override
  public void execute(TransitionContext<T> context) throws GatewayException {
    eventManager.triggerEvent(context.getCaseId(), NO_ACTION_REQUIRED, "Case Ref", context.getCaseRef());
  }
}

