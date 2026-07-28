package uk.gov.ons.census.fwmt.jobservice.transition;

import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.jobservice.service.processor.InboundProcessor;

@Component
public class ProcessTransitionProcessor<T> implements TransitionProcessor<T> {

  @Override
  public void execute(TransitionContext<T> context) throws GatewayException {
    InboundProcessor<T> inboundProcessor = context.getInboundProcessor();
    if (inboundProcessor == null) {
      throw new GatewayException(GatewayException.Fault.VALIDATION_FAILED,
          "No processor available for transition action PROCESS", context.getCaseId());
    }
    inboundProcessor.process(context.getRequest(), context.getGatewayCaseRecord(), context.getMessageQueueTime());
  }
}

