package uk.gov.ons.census.fwmt.jobservice.service.processor;

import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCommonInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCaseRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.ROUTING_FAILED;

public class ProcessorRouter<T extends FwmtCommonInstruction> {

  private final Map<ProcessorKey, List<InboundProcessor<T>>> processorMap;
  private final GatewayEventManager eventManager;
  private final String verb;
  private final Class<?> sourceClass;

  public ProcessorRouter(
      Map<ProcessorKey, List<InboundProcessor<T>>> processorMap,
      GatewayEventManager eventManager,
      String verb,
      Class<?> sourceClass) {
    this.processorMap = processorMap;
    this.eventManager = eventManager;
    this.verb = verb;
    this.sourceClass = sourceClass;
  }

  public static <T extends FwmtCommonInstruction> ProcessorRouter<T> fromProcessors(
      List<InboundProcessor<T>> processors,
      GatewayEventManager eventManager,
      String verb,
      Class<?> sourceClass) {
    Map<ProcessorKey, List<InboundProcessor<T>>> processorMap = new HashMap<>();
    for (InboundProcessor<T> processor : processors) {
      processorMap.computeIfAbsent(processor.getKey(), ignored -> new ArrayList<>()).add(processor);
    }
    return new ProcessorRouter<>(processorMap, eventManager, verb, sourceClass);
  }

  public InboundProcessor<T> resolveExactlyOne(ProcessorKey key, T request, GatewayCaseRecord cache)
      throws GatewayException {
    List<InboundProcessor<T>> validProcessors = filter(key, request, cache);

    if (validProcessors.isEmpty()) {
      throwRoutingError(request, cache);
    }
    if (validProcessors.size() > 1) {
      throwMultipleRoutingError(request, cache);
    }

    return validProcessors.getFirst();
  }

  public Optional<InboundProcessor<T>> resolveOptional(ProcessorKey key, T request, GatewayCaseRecord cache)
      throws GatewayException {
    List<InboundProcessor<T>> validProcessors = filter(key, request, cache);

    if (validProcessors.size() > 1) {
      throwMultipleRoutingError(request, cache);
    }

    return validProcessors.stream().findFirst();
  }

  private List<InboundProcessor<T>> filter(ProcessorKey key, T request, GatewayCaseRecord cache) {
    return processorMap.getOrDefault(key, Collections.emptyList())
        .stream()
        .filter(processor -> processor.isValid(request, cache))
        .toList();
  }

  public void throwRoutingError(T request, GatewayCaseRecord cache) throws GatewayException {
    String message = String.format("Could not find a %s processor for request from RM", verb);
    String cacheStr = cache != null ? cache.toString() : "no cache";
    eventManager.triggerErrorEvent(sourceClass,
        message,
        String.valueOf(request.getCaseId()), ROUTING_FAILED,
        "FwmtActionInstruction", request.toString(), "cache", cacheStr);
    throw new GatewayException(GatewayException.Fault.VALIDATION_FAILED,
        message, request.toString(), cacheStr);
  }

  private void throwMultipleRoutingError(T request, GatewayCaseRecord cache) throws GatewayException {
    String message = String.format("Found multiple %s processors for request from RM", verb);
    String cacheStr = cache != null ? cache.toString() : "no cache";
    eventManager.triggerErrorEvent(sourceClass,
        message,
        String.valueOf(request.getCaseId()), ROUTING_FAILED,
        "FwmtActionInstruction", request.toString(), cacheStr);
    throw new GatewayException(GatewayException.Fault.VALIDATION_FAILED,
        message, request.toString(), cacheStr);
  }
}









