package uk.gov.ons.census.fwmt.jobservice.rabbit;

import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCommonInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.service.GatewayCacheService;
import uk.gov.ons.census.fwmt.jobservice.service.JobService;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GWMessageProcessorTest {

  @InjectMocks
  private GWMessageProcessor gwMessageProcessor;

  @Mock
  private GatewayCacheService cacheService;

  @Mock
  private JobService jobService;

  @Mock
  private GatewayEventManager gatewayEventManager;

  @Mock
  private MessageExceptionHandler messageExceptionHandler;

  @DisplayName("Should call JobService process for CREATE")
  @Test
  public void shouldProcessActionInstruction_CREATE() throws GatewayException {
    FwmtActionInstruction instruction = new FwmtActionInstruction();
    instruction.setActionInstruction(ActionInstructionType.CREATE);
    PubsubMessage message = createPubsubMessage();
    Instant now = Instant.now();
    gwMessageProcessor.processCreateInstruction(instruction, now, message);
    verify(jobService).processCreate(eq(instruction), eq(now));
  }

  @DisplayName("Should call JobService process for SWITCH_CE_TYPE")
  @Test
  public void shouldProcessActionInstruction_SWITCH_CE_TYPE() throws GatewayException {
    FwmtActionInstruction instruction = new FwmtActionInstruction();
    instruction.setActionInstruction(ActionInstructionType.SWITCH_CE_TYPE);
    PubsubMessage message = createPubsubMessage();
    Instant now = Instant.now();
    gwMessageProcessor.processCreateInstruction(instruction, now, message);
    verify(jobService).processCreate(eq(instruction), eq(now));
  }

  @DisplayName("Should call JobService process for UPDATE")
  @Test
  public void shouldProcessActionInstruction_UPDATE() throws GatewayException {
    FwmtActionInstruction instruction = new FwmtActionInstruction();
    instruction.setActionInstruction(ActionInstructionType.UPDATE);
    PubsubMessage message = createPubsubMessage();
    Instant now = Instant.now();
    gwMessageProcessor.processCreateInstruction(instruction, now, message);
    verify(jobService).processUpdate(eq(instruction), eq(now));
  }

  @DisplayName("Should call JobService process for PAUSE")
  @Test
  public void shouldProcessActionInstruction_PAUSE() throws GatewayException {
    FwmtActionInstruction instruction = new FwmtActionInstruction();
    instruction.setActionInstruction(ActionInstructionType.PAUSE);
    PubsubMessage message = createPubsubMessage();
    Instant now = Instant.now();
    gwMessageProcessor.processCreateInstruction(instruction, now, message);
    verify(jobService).processPause(eq(instruction), eq(now));
  }

  @DisplayName("Should put gateway exception on the transient queue ")
  @Test()
  public void shouldProcessGatewayException() throws GatewayException {
    FwmtActionInstruction instruction = new FwmtActionInstruction();
    instruction.setActionInstruction(ActionInstructionType.CREATE);
    PubsubMessage message = createPubsubMessage();
    Instant now = Instant.now();

    doThrow(RestClientException.class).when(jobService).processCreate(any(), any());

    gwMessageProcessor.processCreateInstruction(instruction, now, message);
    verify(messageExceptionHandler).handleTransientMessage(eq(message), any(FwmtCommonInstruction.class));
  }

  @DisplayName("Should process Process Cancel Message ")
  @Test
  public void shouldProcessCancelMessage() throws GatewayException {

    FwmtCancelActionInstruction instruction = new FwmtCancelActionInstruction();
    instruction.setActionInstruction(ActionInstructionType.CANCEL);
    PubsubMessage message = createPubsubMessage();
    Instant now = Instant.now();

    gwMessageProcessor.processCancelInstruction(instruction, now, message);
    verify(jobService).processCancel(eq(instruction), eq(now));
  }

  @DisplayName("Should process move from to transient queue for exception ")
  @Test
  public void shouldProcessExceptionForCancelMessage() throws GatewayException {

    FwmtCancelActionInstruction instruction = new FwmtCancelActionInstruction();
    instruction.setActionInstruction(ActionInstructionType.CANCEL);
    PubsubMessage message = createPubsubMessage();
    Instant now = Instant.now();
    doThrow(RestClientException.class).when(jobService).processCancel(any(), any());

    gwMessageProcessor.processCancelInstruction(instruction, now, message);
    verify(jobService).processCancel(eq(instruction), eq(now));
  }

  private static PubsubMessage createPubsubMessage() {
    return PubsubMessage.newBuilder()
        .setData(ByteString.copyFromUtf8("payload"))
        .build();
  }
}
