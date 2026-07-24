package uk.gov.ons.census.fwmt.jobservice.service.processor;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction;
import uk.gov.ons.census.fwmt.jobservice.service.JobService;

import java.util.List;

@Configuration
public class InboundProcessorConfig {

  @Bean
  @Qualifier("CreateProcessorRouter")
  public ProcessorRouter<FwmtActionInstruction> buildCreateProcessorRouter(
      @Qualifier("Create") List<InboundProcessor<FwmtActionInstruction>> processors,
      GatewayEventManager eventManager) {
    return ProcessorRouter.fromProcessors(processors, eventManager, "CREATE", JobService.class);
  }

  @Bean
  @Qualifier("UpdateProcessorRouter")
  public ProcessorRouter<FwmtActionInstruction> buildUpdateProcessorRouter(
      @Qualifier("Update") List<InboundProcessor<FwmtActionInstruction>> processors,
      GatewayEventManager eventManager) {
    return ProcessorRouter.fromProcessors(processors, eventManager, "UPDATE", JobService.class);
  }

  @Bean
  @Qualifier("CancelProcessorRouter")
  public ProcessorRouter<FwmtCancelActionInstruction> buildCancelProcessorRouter(
      @Qualifier("Cancel") List<InboundProcessor<FwmtCancelActionInstruction>> processors,
      GatewayEventManager eventManager) {
    return ProcessorRouter.fromProcessors(processors, eventManager, "CANCEL", JobService.class);
  }

  @Bean
  @Qualifier("PauseProcessorRouter")
  public ProcessorRouter<FwmtActionInstruction> buildPauseProcessorRouter(
      @Qualifier("Pause") List<InboundProcessor<FwmtActionInstruction>> processors,
      GatewayEventManager eventManager) {
    return ProcessorRouter.fromProcessors(processors, eventManager, "PAUSE", JobService.class);
  }
}