package uk.gov.ons.census.fwmt.jobservice.service.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class InboundProcessorConfig {

  @Autowired
  @Qualifier("Create")
  private List<InboundProcessor<FwmtActionInstruction>> createProcessors;

  @Autowired
  @Qualifier("Update")
  private List<InboundProcessor<FwmtActionInstruction>> updateProcessors;

  @Autowired
  @Qualifier("Cancel")
  private List<InboundProcessor<FwmtCancelActionInstruction>> cancelProcessors;

  @Autowired
  @Qualifier("Pause")
  private List<InboundProcessor<FwmtActionInstruction>> pauseProcessors;

  @Bean
  @Qualifier("CreateProcessorMap")
  public Map<ProcessorKey, List<InboundProcessor<FwmtActionInstruction>>> buildCreateProcessorMap(
      @Qualifier("Create") List<InboundProcessor<FwmtActionInstruction>> processors) {
    var createProcessorMap = new HashMap<ProcessorKey, List<InboundProcessor<FwmtActionInstruction>>>();
    for (InboundProcessor<FwmtActionInstruction> p : processors) {
      if (!createProcessorMap.containsKey(p.getKey())) {
        createProcessorMap.put(p.getKey(), new ArrayList<>());
      }
      List<InboundProcessor<FwmtActionInstruction>> list = createProcessorMap.get(p.getKey());
      list.add(p);
      createProcessorMap.put(p.getKey(), list);
    }
    return createProcessorMap;
  }

  @Bean
  @Qualifier("UpdateProcessorMap")
  public Map<ProcessorKey, List<InboundProcessor<FwmtActionInstruction>>> buildUpdateProcessorMap(
      @Qualifier("Update") List<InboundProcessor<FwmtActionInstruction>> processors) {
    var updateProcessorMap = new HashMap<ProcessorKey, List<InboundProcessor<FwmtActionInstruction>>>();
    for (InboundProcessor<FwmtActionInstruction> p : processors) {
      if (!updateProcessorMap.containsKey(p.getKey())) {
        updateProcessorMap.put(p.getKey(), new ArrayList<>());
      }
      List<InboundProcessor<FwmtActionInstruction>> list = updateProcessorMap.get(p.getKey());
      list.add(p);
      updateProcessorMap.put(p.getKey(), list);
    }
    return updateProcessorMap;
  }

  @Bean
  @Qualifier("CancelProcessorMap")
  public Map<ProcessorKey, List<InboundProcessor<FwmtCancelActionInstruction>>> buildCancelProcessorMap(
      @Qualifier("Cancel") List<InboundProcessor<FwmtCancelActionInstruction>> processors) {
    var cancelProcessorMap = new HashMap<ProcessorKey, List<InboundProcessor<FwmtCancelActionInstruction>>>();
    for (InboundProcessor<FwmtCancelActionInstruction> p : processors) {
      if (!cancelProcessorMap.containsKey(p.getKey())) {
        cancelProcessorMap.put(p.getKey(), new ArrayList<>());
      }
      List<InboundProcessor<FwmtCancelActionInstruction>> list = cancelProcessorMap.get(p.getKey());
      list.add(p);
      cancelProcessorMap.put(p.getKey(), list);
    }
    return cancelProcessorMap;
  }

  @Bean
  @Qualifier("PauseProcessorMap")
  public Map<ProcessorKey, List<InboundProcessor<FwmtActionInstruction>>> buildPauseProcessorMap(
      @Qualifier("Pause") List<InboundProcessor<FwmtActionInstruction>> processors) {
    var pauseProcessorMap = new HashMap<ProcessorKey, List<InboundProcessor<FwmtActionInstruction>>>();
    for (InboundProcessor<FwmtActionInstruction> p : processors) {
      if (!pauseProcessorMap.containsKey(p.getKey())) {
        pauseProcessorMap.put(p.getKey(), new ArrayList<>());
      }
      List<InboundProcessor<FwmtActionInstruction>> list = pauseProcessorMap.get(p.getKey());
      list.add(p);
      pauseProcessorMap.put(p.getKey(), list);
    }
    return pauseProcessorMap;
  }
}