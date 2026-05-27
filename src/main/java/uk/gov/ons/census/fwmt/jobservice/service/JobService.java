package uk.gov.ons.census.fwmt.jobservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.messaging.RmFieldMessagePublisher;
import uk.gov.ons.census.fwmt.jobservice.service.processor.InboundProcessor;
import uk.gov.ons.census.fwmt.jobservice.service.processor.ProcessorKey;
import uk.gov.ons.census.fwmt.jobservice.service.routing.ignore.CeUpdateIgnoreProcessor;
import uk.gov.ons.census.fwmt.jobservice.transition.Transitioner;

import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.CONVERT_SPG_UNIT_UPDATE_TO_CREATE;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.ROUTING_FAILED;

@Slf4j
@Service
public class JobService {

  @Autowired
  private GatewayCacheService cacheService;

  @Autowired
  private GatewayEventManager eventManager;

  @Autowired
  private Transitioner transitioner;

  @Autowired
  private RmFieldMessagePublisher rmFieldPublisher;

  @Autowired
  private CeUpdateIgnoreProcessor ceUpdateIgnoreProcessor;

  @Autowired
  @Qualifier("CreateProcessorMap")
  private Map<ProcessorKey, List<InboundProcessor<FwmtActionInstruction>>> createProcessorMap;

  @Autowired
  @Qualifier("UpdateProcessorMap")
  private Map<ProcessorKey, List<InboundProcessor<FwmtActionInstruction>>> updateProcessorMap;

  @Autowired
  @Qualifier("CancelProcessorMap")
  private Map<ProcessorKey, List<InboundProcessor<FwmtCancelActionInstruction>>> cancelProcessorMap;

  @Autowired
  @Qualifier("PauseProcessorMap")
  private Map<ProcessorKey, List<InboundProcessor<FwmtActionInstruction>>> pauseProcessorMap;

  @Transactional
  public void processCreate(FwmtActionInstruction rmRequest, Instant messageReceivedTime) throws GatewayException {
    final GatewayCache cache = cacheService.getById(rmRequest.getCaseId());

    ProcessorKey key = ProcessorKey.buildKey(rmRequest);
    List<InboundProcessor<FwmtActionInstruction>> processors = createProcessorMap.get(key);

    if (processors == null)
      processors = Collections.emptyList();
    else
      processors = createProcessorMap.get(key).stream().filter(p -> p.isValid(rmRequest, cache)).collect(Collectors.toList());

    if (processors.size() == 0) {
      // TODO throw routing error & exit;
      eventManager.triggerErrorEvent(this.getClass(), "Could not find a CREATE processor for request from RM", String.valueOf(rmRequest.getCaseId()), ROUTING_FAILED,
          "FwmtActionInstruction", rmRequest.toString(), "cache", (cache != null) ? cache.toString() : "no cache");
      throw new GatewayException(GatewayException.Fault.VALIDATION_FAILED, "Could not find a CREATE processor for request from RM", rmRequest.toString(),
          (cache != null) ? cache.toString() : "no cache");
    }
    if (processors.size() > 1) {
      // TODO throw routing error & exit;
      eventManager.triggerErrorEvent(this.getClass(), "Found multiple CREATE processors for request from RM", String.valueOf(rmRequest.getCaseId()), ROUTING_FAILED,
          "FwmtActionInstruction", rmRequest.toString(), (cache != null) ? cache.toString() : "no cache");
      throw new GatewayException(GatewayException.Fault.VALIDATION_FAILED, "Found multiple CREATE processors for request from RM", rmRequest.toString(),
          (cache != null) ? cache.toString() : "no cache");
    }
    transitioner.processTransition(cache, rmRequest, processors.get(0), messageReceivedTime);

  }

  @Transactional
  public void processUpdate(FwmtActionInstruction rmRequest, Instant messageReceivedTime) throws GatewayException {
    final GatewayCache cache = cacheService.getById(rmRequest.getCaseId());
    boolean isHeld = false;

    if (cache != null) {
      if ("UPDATE(HELD)".equals(cache.getLastActionInstruction()) || "CANCEL(HELD)".equals(cache.getLastActionInstruction())) {
        isHeld = true;
      }
    }
    if (cache == null && rmRequest.isUndeliveredAsAddress() && (rmRequest.getAddressType().equals("HH") || rmRequest.getAddressType().equals("SPG"))) {
      rmRequest.setActionInstruction(ActionInstructionType.CREATE);

      eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), CONVERT_SPG_UNIT_UPDATE_TO_CREATE,
          "Case Ref", rmRequest.getCaseRef());

      rmFieldPublisher.publish(rmRequest);
      return;
    }

    ProcessorKey key = ProcessorKey.buildKey(rmRequest);
    List<InboundProcessor<FwmtActionInstruction>> processors = updateProcessorMap.get(key);
    if (processors == null)
      processors = new ArrayList<>();
    else
      processors = processors.stream().filter(p -> p.isValid(rmRequest, cache)).collect(Collectors.toList());

    if (processors.size() == 0 && cache != null && !isHeld) {
      if("UPDATE".equals(rmRequest.getActionInstruction().toString())&& "CE".equals(rmRequest.getAddressType())) {
        ceUpdateIgnoreProcessor.process(rmRequest);
      }else {
        // TODO throw routing error & exit;
        eventManager.triggerErrorEvent(this.getClass(), "Could not find a UPDATE processor for request from RM",
            String.valueOf(rmRequest.getCaseId()), ROUTING_FAILED, "rmRequest", rmRequest.toString());
        throw new GatewayException(GatewayException.Fault.VALIDATION_FAILED,
            "Could not find a UPDATE processor for request from RM", rmRequest, cache);
      }
    }
    if (processors.size() > 1) {
      // TODO throw routing error & exit;
      eventManager.triggerErrorEvent(this.getClass(), "Found multiple UPDATE processors for request from RM", String.valueOf(rmRequest.getCaseId()), ROUTING_FAILED,
          "FwmtActionInstruction", rmRequest.toString(), (cache != null) ? cache.toString() : "no cache");
      throw new GatewayException(GatewayException.Fault.VALIDATION_FAILED, "Found multiple UPDATE processors for request from RM", rmRequest, cache, "rmRequest",
          rmRequest.toString());
    }
    if (processors.size() == 1) {
      transitioner.processTransition(cache, rmRequest, processors.get(0), messageReceivedTime);
    }
    if (processors.size() == 0 && (cache == null || isHeld)) {
      transitioner.processTransition(cache, rmRequest, null, messageReceivedTime);
    }
  }

  @Transactional
  public void processCancel(FwmtCancelActionInstruction rmRequest, Instant messageReceivedTime) throws GatewayException {
    GatewayCache cache = cacheService.getByOriginalCaseId(rmRequest.getCaseId());

    if (cache != null) {
      rmRequest.setNc(true);
    } else {
      cache = cacheService.getById(rmRequest.getCaseId());
    }

    ProcessorKey key = ProcessorKey.buildKey(rmRequest);
    List<InboundProcessor<FwmtCancelActionInstruction>> processors = cancelProcessorMap.get(key);

    if (processors == null)
      processors = new ArrayList<>();
    else {
      final GatewayCache finalCache = cache;
      processors = processors.stream().filter(p -> p.isValid(rmRequest, finalCache)).collect(Collectors.toList());
    }

    if (processors.size() == 0 && cache != null && !"CANCEL(HELD)".equals(cache.getLastActionInstruction())) {
      // TODO throw routing error & exit;
      eventManager.triggerErrorEvent(this.getClass(), "Could not find a CANCEL processor for request from RM", String.valueOf(rmRequest.getCaseId()), ROUTING_FAILED,
          "FwmtCancelActionInstruction", rmRequest.toString(), cache.toString());
      throw new GatewayException(GatewayException.Fault.VALIDATION_FAILED, "Could not find a CANCEL processor for request from RM", rmRequest, cache);
    }
    if (processors.size() > 1) {
      // TODO throw routing error & exit;
      eventManager.triggerErrorEvent(this.getClass(), "Found multiple CANCEL processors for request from RM", String.valueOf(rmRequest.getCaseId()), ROUTING_FAILED,
          "FwmtCancelActionInstruction", rmRequest.toString(), (cache != null) ? cache.toString() : "no cache");
      throw new GatewayException(GatewayException.Fault.VALIDATION_FAILED, "Found multiple CANCEL processors for request from RM", rmRequest, cache);
    }
    if (processors.size() == 1) {
      transitioner.processTransition(cache, rmRequest, processors.get(0), messageReceivedTime);
    }
    if (processors.size() == 0 && (cache == null || cache.getLastActionInstruction().equals("CANCEL(HELD)"))) {
      processors.add(null);
      transitioner.processTransition(cache, rmRequest, processors.get(0), messageReceivedTime);
    }
  }

  @Transactional
  public void processPause(FwmtActionInstruction rmRequest, Instant messageReceivedTime)
      throws GatewayException {
    final GatewayCache cache = cacheService.getById(rmRequest.getCaseId());
    ProcessorKey key = ProcessorKey.buildKey(rmRequest);

    List<InboundProcessor<FwmtActionInstruction>> processors = pauseProcessorMap.get(key);

    if (processors == null)
      processors = new ArrayList<>();
    else
      processors = processors.stream().filter(p -> p.isValid(rmRequest, cache)).collect(Collectors.toList());

    if (processors.size() == 0) {
      // TODO throw routing error & exit;
      eventManager.triggerErrorEvent(this.getClass(), "Could not find a PAUSE processor for request from RM", String.valueOf(rmRequest.getCaseId()), ROUTING_FAILED,
          "FwmtActionInstruction", rmRequest.toString(), (cache != null) ? cache.toString() : "no cache");
      throw new GatewayException(GatewayException.Fault.VALIDATION_FAILED, "Could not find a PAUSE processor for request from RM", rmRequest, cache);
    }
    if (processors.size() > 1) {
      // TODO throw routing error & exit;
      eventManager.triggerErrorEvent(this.getClass(), "Found multiple PAUSE processors for request from RM", String.valueOf(rmRequest.getCaseId()), ROUTING_FAILED,
          "FwmtActionInstruction", rmRequest.toString(), (cache != null) ? cache.toString() : "no cache");
      throw new GatewayException(GatewayException.Fault.VALIDATION_FAILED, "Found multiple PAUSE processors for request from RM", rmRequest, cache);
    }
    processors.get(0).process(rmRequest, cache, messageReceivedTime);
  }

  /*
   * private void routingFailure() String ffuDetail = ffu.toRoutingString();
   * String cacheDetail = (cache == null) ? "null" : cache.toRoutingString();
   * String msg = this.getClass().getSimpleName() +
   * " is unable to route the following message: " + ffuDetail + " with " +
   * cacheDetail; eventManager.triggerErrorEvent(this.getClass(), msg,
   * String.valueOf(ffu.getCaseId()), ROUTING_FAILED); throw new
   * GatewayException(GatewayException.Fault.VALIDATION_FAILED, msg, ffu,
   * cache);
   */

  /*
   * public void process(FwmtActionInstruction ffu) throws GatewayException { if
   * (createRouter.isValid(ffu, cache)) { createRouter.routeUnsafe(ffu, cache);
   * } else { updateRouter.route(ffu, cache, eventManager); } }
   *
   * public void process(FwmtCancelActionInstruction ffu) throws
   * GatewayException { GatewayCache cache =
   * cacheService.getById(ffu.getCaseId()); cancelRouter.route(ffu, cache,
   * eventManager); }
   *
   */
}
