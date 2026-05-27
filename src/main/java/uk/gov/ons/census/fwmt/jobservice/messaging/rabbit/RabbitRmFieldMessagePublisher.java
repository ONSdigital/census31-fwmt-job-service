package uk.gov.ons.census.fwmt.jobservice.messaging.rabbit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.common.messaging.MessagingProperties;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction;
import uk.gov.ons.census.fwmt.jobservice.messaging.RmFieldMessagePublisher;

import java.time.Instant;
import java.util.Date;

@Service
@Slf4j
@ConditionalOnProperty(name = MessagingProperties.PROVIDER, havingValue = MessagingProperties.PROVIDER_RABBIT, matchIfMissing = true)
public class RabbitRmFieldMessagePublisher implements RmFieldMessagePublisher {

  private final RabbitTemplate rabbitRmTemplate;
  private final String rmFieldDestination;

  public RabbitRmFieldMessagePublisher(
      @Qualifier("rmRabbitTemplate") RabbitTemplate rabbitRmTemplate,
      @Value("${app.messaging.destinations.rmField:RM.Field}") String rmFieldDestination) {
    this.rabbitRmTemplate = rabbitRmTemplate;
    this.rmFieldDestination = rmFieldDestination;
  }

  @Override
  public void publish(FwmtCancelActionInstruction cancelActionInstruction) {
    log.info("Publishing cancel event to RM Field destination {}", rmFieldDestination);
    rabbitRmTemplate.convertAndSend(rmFieldDestination, cancelActionInstruction, timestampPostProcessor());
  }

  @Override
  public void publish(FwmtActionInstruction actionInstruction) {
    rabbitRmTemplate.convertAndSend(rmFieldDestination, actionInstruction, timestampPostProcessor());
  }

  private static MessagePostProcessor timestampPostProcessor() {
    return new MessagePostProcessor() {
      @Override
      public Message postProcessMessage(Message message) throws AmqpException {
        long epochMilli = Instant.now().toEpochMilli();
        message.getMessageProperties().setTimestamp(new Date(epochMilli));
        return message;
      }
    };
  }
}
