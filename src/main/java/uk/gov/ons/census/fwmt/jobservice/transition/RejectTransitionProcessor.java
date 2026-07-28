package uk.gov.ons.census.fwmt.jobservice.transition;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;

@Component
@RequiredArgsConstructor
public class RejectTransitionProcessor<T> implements TransitionProcessor<T> {
  private static final String REJECTED_RM_REQUEST = "REJECTED_RM_REQUEST";

  private final GatewayEventManager eventManager;

  @Override
  public void execute(TransitionContext<T> context) throws GatewayException {
    eventManager.triggerErrorEvent(Transitioner.class, "Request from RM rejected",
        context.getCaseId(), REJECTED_RM_REQUEST);
  }
}

