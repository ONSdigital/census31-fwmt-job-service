package uk.gov.ons.census.fwmt.jobservice.rabbit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCommonInstruction;
import uk.gov.ons.census.fwmt.jobservice.data.QuarantinedMessage;
import uk.gov.ons.census.fwmt.jobservice.repository.QuarantinedMessageRepository;

import javax.annotation.PostConstruct;

@Slf4j
@Component
public class MessageExceptionHandler {
  @Autowired
  @Qualifier("GW_EVENT_RT")
  private RabbitTemplate gatewayRabbitTemplate;

  @Autowired
  private QuarantinedMessageRepository quarantinedMessageRepository;

  @Value("${app.rabbitmq.gw.maxRetryCount}")
  private int maxRetryCount;
  @Value("${app.rabbitmq.gw.exchanges.error}")
  private String errorExchange;
  @Value("${app.rabbitmq.gw.routingkey.perm}")
  private String permanentRoutingKey;
  @Value("${app.rabbitmq.gw.routingkey.trans}")
  private String transientRoutingKey;
  @Value("${app.rabbitmq.gw.queues.input}")

  private String gwFieldQueue;

  @PostConstruct
  public void transientExceptionHandler() {
    log.info("TransientExceptionHandler maxRetryCount :{}", maxRetryCount);
    log.info("TransientExceptionHandler errorExchange :{}", errorExchange);
    log.info("TransientExceptionHandler permanent routing key :{}", permanentRoutingKey);
    log.info("TransientExceptionHandler transient routing key :{}", transientRoutingKey);
  }

  public void handleTransientMessage(Message message, FwmtCommonInstruction instruction) {
    Integer retryCount = message.getMessageProperties().getHeader("retryCount");
    if (retryCount == null) {
      retryCount = 0;
    }
    if (retryCount < maxRetryCount) {
      message.getMessageProperties().setHeader("retryCount", ++retryCount);
      log.warn("Retry number {}", retryCount);
      gatewayRabbitTemplate.convertAndSend(errorExchange, transientRoutingKey, message);
    } else {
      log.error("We've reached our retry limit {}", maxRetryCount);
      handlePermMessage(message, instruction);
    }
  }

  public void handlePermMessage(Message message, FwmtCommonInstruction instruction) {
    gatewayRabbitTemplate.convertAndSend(errorExchange, permanentRoutingKey, message);
    final QuarantinedMessage quarantinedMessage = QuarantinedMessage.builder()
        .messagePayload(message.getBody())
        .caseId(instruction.getCaseId())
        .routingKey("")
        .actionInstruction(instruction.getActionInstruction())
        .addressLevel(instruction.getAddressLevel())
        .addressType(instruction.getAddressType())
        .nc(instruction.isNc())
        .surveyName(instruction.getSurveyName())
        .queue(gwFieldQueue)
        .headers(message.getMessageProperties().getHeaders())
        .build();

    quarantinedMessageRepository.save(quarantinedMessage);
    log.info("Stored perm issue in DB for later processing {}", maxRetryCount);

  }
}
