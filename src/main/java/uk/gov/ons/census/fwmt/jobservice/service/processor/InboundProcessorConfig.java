package uk.gov.ons.census.fwmt.jobservice.service.processor;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.ons.census.fwmt.common.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstruction;
import uk.gov.ons.census.fwmt.common.rm.dto.CancelActionInstruction;
import uk.gov.ons.census.fwmt.jobservice.service.JobService;

import java.util.List;

@Configuration
public class InboundProcessorConfig {

  @Bean
  @Qualifier("CreateProcessorRouter")
  public ProcessorRouter<ActionInstruction> buildCreateProcessorRouter(
      @Qualifier("Create") List<InboundProcessor<ActionInstruction>> processors,
      GatewayEventManager eventManager) {
    return ProcessorRouter.fromProcessors(processors, eventManager, "CREATE", JobService.class);
  }

  @Bean
  @Qualifier("UpdateProcessorRouter")
  public ProcessorRouter<ActionInstruction> buildUpdateProcessorRouter(
      @Qualifier("Update") List<InboundProcessor<ActionInstruction>> processors,
      GatewayEventManager eventManager) {
    return ProcessorRouter.fromProcessors(processors, eventManager, "UPDATE", JobService.class);
  }

  @Bean
  @Qualifier("CancelProcessorRouter")
  public ProcessorRouter<CancelActionInstruction> buildCancelProcessorRouter(
      @Qualifier("Cancel") List<InboundProcessor<CancelActionInstruction>> processors,
      GatewayEventManager eventManager) {
    return ProcessorRouter.fromProcessors(processors, eventManager, "CANCEL", JobService.class);
  }

  @Bean
  @Qualifier("PauseProcessorRouter")
  public ProcessorRouter<ActionInstruction> buildPauseProcessorRouter(
      @Qualifier("Pause") List<InboundProcessor<ActionInstruction>> processors,
      GatewayEventManager eventManager) {
    return ProcessorRouter.fromProcessors(processors, eventManager, "PAUSE", JobService.class);
  }
}
