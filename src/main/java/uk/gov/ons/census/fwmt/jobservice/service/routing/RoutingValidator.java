package uk.gov.ons.census.fwmt.jobservice.service.routing;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;

import java.util.List;

@Component
public class RoutingValidator {
  private static final List<HttpStatus> validResponses = List
      .of(HttpStatus.OK, HttpStatus.CREATED, HttpStatus.ACCEPTED, HttpStatus.NO_CONTENT);
  private final GatewayEventManager eventManager;

  public RoutingValidator(GatewayEventManager eventManager) {
    this.eventManager = eventManager;
  }

  public void validateResponseCode(ResponseEntity<Void> response, String caseId, String verb, String errorCode, String... metadata)
      throws GatewayException {
    if (!validResponses.contains(response.getStatusCode())) {
      String code = response.getStatusCode().toString();
      String value = Integer.toString(response.getStatusCodeValue());
      
      String msg = "Unable to " + verb + " FieldWorkerJobRequest: HTTP_STATUS:" + code + ":" + value;
      eventManager.triggerErrorEvent(this.getClass(), msg, String.valueOf(caseId), errorCode, metadata);
      throw new GatewayException(GatewayException.Fault.SYSTEM_ERROR, msg);
    }
  }
}
