package uk.gov.ons.census.fwmt.jobservice.transition;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.jobservice.service.processor.InboundProcessor;
import uk.gov.ons.census.fwmt.jobservice.transition.utils.MergeMessages;

@Component
@RequiredArgsConstructor
public class MergeTransitionProcessor<T> implements TransitionProcessor<T> {

  private final MergeMessages mergeMessages;

  @Override
  public void execute(TransitionContext<T> context) throws GatewayException {
    InboundProcessor<T> inboundProcessor = context.getInboundProcessor();
    if (!context.isCancel()) {
      if (inboundProcessor == null) {
        throw new GatewayException(GatewayException.Fault.VALIDATION_FAILED,
            "No processor available for transition action MERGE", context.getCaseId());
      }
      inboundProcessor.process(context.getRequest(), context.getGatewayCaseRecord(), context.getMessageQueueTime());
    }
    mergeMessages.mergeRecords(context.getMessageCache());
  }
}

