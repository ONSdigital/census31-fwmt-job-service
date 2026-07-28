package uk.gov.ons.census.fwmt.jobservice.transition;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class TransitionAction<T> {
  TransitionProcessor<T> processor;
  TransitionContext<T> context;
}

