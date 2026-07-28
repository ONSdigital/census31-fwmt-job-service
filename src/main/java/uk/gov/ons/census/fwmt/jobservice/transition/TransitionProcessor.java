package uk.gov.ons.census.fwmt.jobservice.transition;

import uk.gov.ons.census.fwmt.common.error.GatewayException;

public interface TransitionProcessor<T> {
  void execute(TransitionContext<T> context) throws GatewayException;
}

