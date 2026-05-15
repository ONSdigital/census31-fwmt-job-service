package uk.gov.ons.census.fwmt.jobservice.transition.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction;
import uk.gov.ons.census.fwmt.jobservice.data.MessageCache;
import uk.gov.ons.census.fwmt.jobservice.service.JobService;
import uk.gov.ons.census.fwmt.jobservice.service.converter.ConvertMessage;

import java.time.Instant;

@Slf4j
@Component
public class MergeMessages {

  @Autowired
  private JobService jobService;

  public void mergeRecords(MessageCache messageCache) throws GatewayException {
    ConvertMessage convertMessage = new ConvertMessage();
    if (messageCache.messageType.equals("UPDATE(HELD)")) {
      try {
        FwmtActionInstruction fwmtActionInstruction = convertMessage
            .convertMessageToDTO(FwmtActionInstruction.class, messageCache.message);
        jobService.processUpdate(fwmtActionInstruction, Instant.now());
      } catch (GatewayException e) {
        throw new GatewayException(GatewayException.Fault.SYSTEM_ERROR,  "Could not convert FWMTActionInstruction"
            ,messageCache.getCaseId());
      }
    }    
    if (messageCache.messageType.equals("SWITCH_CE_TYPE(Held)")) {
      try {
        FwmtActionInstruction fwmtActionInstruction = convertMessage
            .convertMessageToDTO(FwmtActionInstruction.class, messageCache.message);
        jobService.processUpdate(fwmtActionInstruction, Instant.now());
      } catch (GatewayException e) {
        throw new GatewayException(GatewayException.Fault.SYSTEM_ERROR,  "Could not convert FWMTActionInstruction"
            ,messageCache.getCaseId());
      }
    }
    if (messageCache.messageType.equals("CANCEL(HELD)")) {
      try {
        FwmtCancelActionInstruction fwmtCancelActionInstruction = convertMessage
            .convertMessageToDTO(FwmtCancelActionInstruction.class, messageCache.message);
        jobService.processCancel(fwmtCancelActionInstruction, Instant.now());
      } catch (GatewayException e) {
        throw new GatewayException(GatewayException.Fault.SYSTEM_ERROR,  "Could not convert FWMTActionCancelInstruction"
            ,messageCache.getCaseId());
      }
    }
  }
}