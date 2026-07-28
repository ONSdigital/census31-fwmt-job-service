package uk.gov.ons.census.fwmt.jobservice.transition;

import lombok.Builder;
import lombok.Value;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCaseRecord;
import uk.gov.ons.census.fwmt.jobservice.data.MessageCache;
import uk.gov.ons.census.fwmt.jobservice.service.converter.TransitionRule;
import uk.gov.ons.census.fwmt.jobservice.service.processor.InboundProcessor;

import java.time.Instant;

@Value
@Builder(toBuilder = true)
public class TransitionContext<T> {
  String caseId;
  String caseRef;
  String actionInstruction;
  T request;
  InboundProcessor<T> inboundProcessor;
  GatewayCaseRecord gatewayCaseRecord;
  MessageCache messageCache;
  TransitionRule transitionRule;
  Instant messageQueueTime;
  boolean cancel;
}

