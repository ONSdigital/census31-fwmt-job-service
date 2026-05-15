package uk.gov.ons.census.fwmt.jobservice.rabbit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction;

import java.time.Instant;

@RequiredArgsConstructor
@Slf4j
@Component
@RabbitListener(queues = "${app.rabbitmq.gw.queues.input}", containerFactory = "gwContainerFactory", concurrency = "${app.rabbitmq.gw.concurrentConsumers}")
public class GWReceiver {

  private final GWMessageProcessor gwMessageProcessor;

  @RabbitHandler
  public void receiveCreateMessage(FwmtActionInstruction rmRequest, @Header("timestamp") String timestamp, Message message) {
    long epochTimeStamp = Long.parseLong(timestamp);
    final Instant receivedMessageTime = Instant.ofEpochMilli(epochTimeStamp);
    gwMessageProcessor.processCreateInstruction(rmRequest, receivedMessageTime, message);
  }

  @RabbitHandler
  public void receiveCancelMessage(FwmtCancelActionInstruction rmRequest, @Header("timestamp") String timestamp, Message message) {
    long epochTimeStamp = Long.parseLong(timestamp);
    Instant receivedMessageTime = Instant.ofEpochMilli(epochTimeStamp);
    gwMessageProcessor.processCancelInstruction(rmRequest, receivedMessageTime, message);
  }
}
