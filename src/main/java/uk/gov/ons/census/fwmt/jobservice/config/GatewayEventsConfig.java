package uk.gov.ons.census.fwmt.jobservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.events.producer.GatewayEventProducer;
import uk.gov.ons.census.fwmt.events.producer.GatewayLoggingEventProducer;
import uk.gov.ons.census.fwmt.events.producer.PubSubGatewayEventProducer;
import uk.gov.ons.census.fwmt.jobservice.Application;

import java.util.Arrays;

@Slf4j
@Configuration
public class GatewayEventsConfig {

  public static final String COMET_CREATE_PRE_SENDING = "COMET_CREATE_PRE_SENDING";
  public static final String COMET_CREATE_ACK = "COMET_CREATE_ACK";
  public static final String COMET_CANCEL_PRE_SENDING = "COMET_CANCEL_PRE_SENDING";
  public static final String COMET_CANCEL_ACK = "COMET_CANCEL_ACK";
  public static final String COMET_CLOSE_PRE_SENDING = "COMET_CLOSE_PRE_SENDING";
  public static final String COMET_CLOSE_ACK = "COMET_CLOSE_ACK";
  public static final String COMET_REOPEN_PRE_SENDING = "COMET_REOPEN_PRE_SENDING";
  public static final String COMET_REOPEN_ACK = "COMET_REOPEN_ACK";
  public static final String COMET_UPDATE_PRE_SENDING = "COMET_UPDATE_PRE_SENDING";
  public static final String COMET_UPDATE_ACK = "COMET_UPDATE_ACK";
  public static final String NO_ACTION_REQUIRED = "NO_ACTION_REQUIRED";
  public static final String MESSAGE_HELD = "MESSAGE_HELD";
  public static final String COMET_DELETE_PRE_SENDING = "COMET_DELETE_PRE_SENDING";
  public static final String COMET_DELETE_ACK = "COMET_DELETE_ACK";
  public static final String IGNORED_UPDATE = "IGNORED_UPDATE";

  public static final String FAILED_TO_CREATE_TM_JOB = "FAILED_TO_CREATE_TM_JOB";
  public static final String FAILED_TO_CANCEL_TM_JOB = "FAILED_TO_CANCEL_TM_JOB";
  public static final String FAILED_TO_CLOSE_TM_JOB = "FAILED_TO_CLOSE_TM_JOB";
  public static final String FAILED_TO_REOPEN_TM_JOB = "FAILED_TO_REOPEN_TM_JOB";
  public static final String FAILED_TO_UPDATE_TM_JOB = "FAILED_TO_UPDATE_TM_JOB";
  public static final String CASE_NOT_FOUND = "CASE_NOT_FOUND";
  public static final String INCORRECT_SWITCH_SURVEY_TYPE = "INCORRECT_SWITCH_SURVEY_TYPE";
  public static final String UNABLE_TO_READ_EVENT_PAYLOAD = "UNABLE_TO_READ_EVENT_PAYLOAD";
  public static final String UNABLE_TO_DECRYPT_NAME = "UNABLE_TO_DECRYPT_NAME";
  public static final String ROUTING_FAILED = "ROUTING_FAILED";
  public static final String CANCEL_ON_A_CANCEL = "CANCEL_ON_A_CANCEL";
  public static final String CASE_DOES_NOT_EXIST = "CASE_DOES_NOT_EXIST";
  public static final String SWITCH_ON_A_CANCEL = "SWITCH_ON_A_CANCEL";
  public static final String UPDATE_ON_A_CANCEL = "UPDATE_ON_A_CANCEL";

  public static final String CONVERT_SPG_UNIT_UPDATE_TO_CREATE = "CONVERT_SPG_UNIT_UPDATE_TO_CREATE";
  public static final String FAILED_TO_ROUTE_REQUEST = "FAILED_TO_ROUTE_REQUEST";

  @Value("${app.testing}")
  private boolean testing;

  public static final String DECRYPTED_HH_NAMES = "DECRYPTED_HH_NAMES";

  @Bean
  public GatewayEventManager gatewayEventManager(
      GatewayLoggingEventProducer gatewayLoggingEventProducer,
      ObjectProvider<PubSubGatewayEventProducer> pubSubGatewayEventProducer) {

    final GatewayEventManager gatewayEventManager;
    if (testing) {
      log.warn("\n\n \t IMPORTANT - Test Mode: ON        \n \t\t Service is initiated in test mode which, this should not occur in production \n\n");
      GatewayEventProducer messagingProducer = pubSubGatewayEventProducer.getIfAvailable();
      if (messagingProducer == null) {
        throw new IllegalStateException("No PubSubGatewayEventProducer available for acceptance testing");
      }
      gatewayEventManager = new GatewayEventManager(Arrays.asList(gatewayLoggingEventProducer, messagingProducer));
    } else {
      log.warn("\n\n \t IMPORTANT - Test Mode: OFF   \n\n");
      gatewayEventManager = new GatewayEventManager(Arrays.asList(gatewayLoggingEventProducer));
    }

    gatewayEventManager.setSource(Application.APPLICATION_NAME);
    return gatewayEventManager;
  }
}
