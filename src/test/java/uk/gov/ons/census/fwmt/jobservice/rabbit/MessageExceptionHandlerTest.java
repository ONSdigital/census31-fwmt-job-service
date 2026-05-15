package uk.gov.ons.census.fwmt.jobservice.rabbit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCommonInstruction;
import uk.gov.ons.census.fwmt.jobservice.data.QuarantinedMessage;
import uk.gov.ons.census.fwmt.jobservice.repository.QuarantinedMessageRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.ons.census.fwmt.jobservice.rabbit.RabbitTestUtils.createMessage;

@ExtendWith(MockitoExtension.class)
class MessageExceptionHandlerTest {

  @Captor
  private ArgumentCaptor<QuarantinedMessage> commonInstructionArgumentCaptor;

  private FwmtCommonInstruction commonInstruction = Mockito.mock(FwmtCommonInstruction.class);

  @InjectMocks
  private MessageExceptionHandler messageExceptionHandler;

  @Captor
  private ArgumentCaptor<Message> messageArgumentCaptor;

  @Mock
  private RabbitTemplate rabbitTemplate;

  @Mock
  private QuarantinedMessageRepository quarantinedMessageRepository;

  @BeforeEach
  public void setup() {
    ReflectionTestUtils.setField(messageExceptionHandler, "maxRetryCount", 5);
    ReflectionTestUtils.setField(messageExceptionHandler, "errorExchange", "GW.Error.Exchange");
    ReflectionTestUtils.setField(messageExceptionHandler, "permanentRoutingKey", "gw.permanent.error");
    ReflectionTestUtils.setField(messageExceptionHandler, "transientRoutingKey", "gw.transient.error");
  }

  @DisplayName("Should Send message to TRANSIENT QUEUE via Routing Key - gw.transient.error")
  @Test
  void shouldPushMessageToTransientQueue() {
    final Message message = createMessage(null);
    messageExceptionHandler.handleTransientMessage(message, commonInstruction);
    verify(rabbitTemplate).convertAndSend(eq("GW.Error.Exchange"), eq("gw.transient.error"), eq(message));
    verify(quarantinedMessageRepository, never()).save(any());

  }

  @DisplayName("Should set the retryCount to one if first time messages has been handled ")
  @Test
  void shouldSetFailCount() {
    final Message message = createMessage(null);
    messageExceptionHandler.handleTransientMessage(message, commonInstruction);
    verify(rabbitTemplate).convertAndSend(anyString(), anyString(), messageArgumentCaptor.capture());
    Message resultMessage = messageArgumentCaptor.getValue();
    int retryCount = resultMessage.getMessageProperties().getHeader("retryCount");
    assertEquals(1, retryCount);
  }

  @DisplayName("Should increment the retryCount if message has been rejected before ")
  @Test
  void shouldIncrementFailCount() {
    final Message message = createMessage(1);
    messageExceptionHandler.handleTransientMessage(message, commonInstruction);
    verify(rabbitTemplate).convertAndSend(anyString(), anyString(), messageArgumentCaptor.capture());
    Message resultMessage = messageArgumentCaptor.getValue();
    int retryCount = resultMessage.getMessageProperties().getHeader("retryCount");
    assertEquals(2, retryCount);
  }

  @DisplayName("Should send failed message to perm queue if it has been processed more times than the maximum")
  @Test
  void shouldSendMessaageToPemStore() {
    final Message message = createMessage(5);
    messageExceptionHandler.handleTransientMessage(message, commonInstruction);
    verify(rabbitTemplate).convertAndSend(eq("GW.Error.Exchange"), eq("gw.permanent.error"), eq(message));
    verify(rabbitTemplate, never()).convertAndSend(eq("GW.Error.Exchange"), eq("gw.transient.error"), eq(message));
    verify(quarantinedMessageRepository).save(any());

  }

  @DisplayName("Should Persist messages sent to Perm Queue ")
  @Test
  void shouldPersistMessagesSentToPermQueue() {
    final Message message = createMessage(null);
    final FwmtCommonInstruction actionInstruction = createCanceActionInstruction();

    messageExceptionHandler.handlePermMessage(message, actionInstruction);
    verify(quarantinedMessageRepository).save(commonInstructionArgumentCaptor.capture());
    QuarantinedMessage savedItem = commonInstructionArgumentCaptor.getValue();
    assertEquals(actionInstruction.getCaseId(), savedItem.getCaseId());
    assertEquals(actionInstruction.getActionInstruction(), savedItem.getActionInstruction());
    assertEquals(actionInstruction.getAddressLevel(), savedItem.getAddressLevel());
    assertEquals(actionInstruction.getSurveyName(), savedItem.getSurveyName());

  }

  public FwmtCommonInstruction createCanceActionInstruction() {
    FwmtCancelActionInstruction inst = new FwmtCancelActionInstruction();
    inst.setNc(true);
    inst.setAddressLevel("level");
    inst.setAddressType("type");
    inst.setActionInstruction(ActionInstructionType.CANCEL);

    return inst;
  }

}