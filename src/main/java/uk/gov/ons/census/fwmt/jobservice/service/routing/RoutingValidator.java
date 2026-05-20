package uk.gov.ons.census.fwmt.jobservice.service.routing;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;

@Component
public class RoutingValidator {
  private final GatewayEventManager eventManager;

  public RoutingValidator(GatewayEventManager eventManager) {
    this.eventManager = eventManager;
  }

  public void validateResponseCode(ResponseEntity<Void> response, String caseId, String verb, String errorCode,
      String... metadata) throws GatewayException {
    HttpStatusCode status = response.getStatusCode();
    if (!isValidResponse(status)) {
      String code = status.toString();
      String value = Integer.toString(status.value());

      String msg = "Unable to " + verb + " FieldWorkerJobRequest: HTTP_STATUS:" + code + ":" + value;
      eventManager.triggerErrorEvent(this.getClass(), msg, String.valueOf(caseId), errorCode, metadata);
      throw new GatewayException(GatewayException.Fault.SYSTEM_ERROR, msg);
    }
  }

  private static boolean isValidResponse(HttpStatusCode status) {
    return status.is2xxSuccessful()
        || status.isSameCodeAs(HttpStatus.OK)
        || status.isSameCodeAs(HttpStatus.CREATED)
        || status.isSameCodeAs(HttpStatus.ACCEPTED)
        || status.isSameCodeAs(HttpStatus.NO_CONTENT);
  }
}
