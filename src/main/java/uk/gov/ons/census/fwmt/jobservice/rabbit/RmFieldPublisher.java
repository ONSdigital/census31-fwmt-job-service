package uk.gov.ons.census.fwmt.jobservice.rabbit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction;

import java.time.Instant;
import java.util.Date;

@Service
@Slf4j
public class RmFieldPublisher {

  @Autowired
  @Qualifier("rmRabbitTemplate")
  private RabbitTemplate rabbitRMTemplate;

  public void publish(FwmtCancelActionInstruction cancelActionInstruction) {
    log.info("Publishing event to RM - ");
    rabbitRMTemplate.convertAndSend("RM.Field", cancelActionInstruction, new MessagePostProcessor() {
      
      @Override
      public Message postProcessMessage(Message message) throws AmqpException {
        long epochMilli = Instant.now().toEpochMilli();
        message.getMessageProperties().setTimestamp(new Date(epochMilli));
        return message;
      }
    });
  }



  public void publish(FwmtActionInstruction actionInstruction) {
    rabbitRMTemplate.convertAndSend("RM.Field", actionInstruction, new MessagePostProcessor() {
      
      @Override
      public Message postProcessMessage(Message message) throws AmqpException {
        long epochMilli = Instant.now().toEpochMilli();
        message.getMessageProperties().setTimestamp(new Date(epochMilli));
        return message;
      }
    });
  }
}
