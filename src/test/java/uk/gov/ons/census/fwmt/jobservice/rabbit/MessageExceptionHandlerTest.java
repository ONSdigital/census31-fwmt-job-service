package uk.gov.ons.census.fwmt.jobservice.rabbit;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCommonInstruction;
import uk.gov.ons.census.fwmt.jobservice.data.QuarantinedMessage;
import uk.gov.ons.census.fwmt.jobservice.repository.QuarantinedMessageRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MessageExceptionHandlerTest {

  @Captor
  private ArgumentCaptor<QuarantinedMessage> commonInstructionArgumentCaptor;

  @Captor
  private ArgumentCaptor<PubsubMessage> pubsubMessageArgumentCaptor;

  private FwmtCommonInstruction commonInstruction = Mockito.mock(FwmtCommonInstruction.class);

  @InjectMocks
  private MessageExceptionHandler messageExceptionHandler;

  @Mock
  private PubSubTemplate pubSubTemplate;

  @Mock
  private QuarantinedMessageRepository quarantinedMessageRepository;

  @BeforeEach
  public void setup() {
    ReflectionTestUtils.setField(messageExceptionHandler, "maxRetryCount", 5);
    ReflectionTestUtils.setField(messageExceptionHandler, "gwTransientErrorTopic", "GW.Transient.ErrorQ");
    ReflectionTestUtils.setField(messageExceptionHandler, "gwPermanentErrorTopic", "GW.Permanent.ErrorQ");
    ReflectionTestUtils.setField(messageExceptionHandler, "gwFieldQueue", "GW.Field");
  }

  @DisplayName("Should publish message to transient error topic")
  @Test
  void shouldPushMessageToTransientQueue() {
    final PubsubMessage message = createPubsubMessage(null);
    messageExceptionHandler.handleTransientMessage(message, commonInstruction);
    verify(pubSubTemplate).publish(eq("GW.Transient.ErrorQ"), pubsubMessageArgumentCaptor.capture());
    assertEquals("1", pubsubMessageArgumentCaptor.getValue().getAttributesOrDefault("retryCount", "0"));
    verify(quarantinedMessageRepository, never()).save(any());
  }

  @DisplayName("Should set the retryCount to one if first time messages has been handled ")
  @Test
  void shouldSetFailCount() {
    final PubsubMessage message = createPubsubMessage(null);
    messageExceptionHandler.handleTransientMessage(message, commonInstruction);
    verify(pubSubTemplate).publish(anyString(), pubsubMessageArgumentCaptor.capture());
    assertEquals("1", pubsubMessageArgumentCaptor.getValue().getAttributesOrDefault("retryCount", "0"));
  }

  @DisplayName("Should increment the retryCount if message has been rejected before ")
  @Test
  void shouldIncrementFailCount() {
    final PubsubMessage message = createPubsubMessage(1);
    messageExceptionHandler.handleTransientMessage(message, commonInstruction);
    verify(pubSubTemplate).publish(anyString(), pubsubMessageArgumentCaptor.capture());
    assertEquals("2", pubsubMessageArgumentCaptor.getValue().getAttributesOrDefault("retryCount", "0"));
  }

  @DisplayName("Should send failed message to perm queue if it has been processed more times than the maximum")
  @Test
  void shouldSendMessaageToPemStore() {
    final PubsubMessage message = createPubsubMessage(5);
    messageExceptionHandler.handleTransientMessage(message, commonInstruction);
    verify(pubSubTemplate).publish(eq("GW.Permanent.ErrorQ"), any(PubsubMessage.class));
    verify(quarantinedMessageRepository).save(any());
  }

  @DisplayName("Should Persist messages sent to Perm Queue ")
  @Test
  void shouldPersistMessagesSentToPermQueue() {
    final PubsubMessage message = createPubsubMessage(null);
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

  private static PubsubMessage createPubsubMessage(Integer retryCount) {
    PubsubMessage.Builder builder = PubsubMessage.newBuilder()
        .setData(ByteString.copyFromUtf8("payload"));
    if (retryCount != null) {
      builder.putAttributes("retryCount", String.valueOf(retryCount));
    }
    return builder.build();
  }
}
