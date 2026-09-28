package uk.gov.ons.census.fwmt.jobservice.service.routing.ignore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstruction;
import uk.gov.ons.census.fwmt.common.events.component.GatewayEventManager;

import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.IGNORED_UPDATE;

@Service
public class CeUpdateIgnoreProcessor {

  @Autowired
  private GatewayEventManager eventManager;

  public void process(ActionInstruction rmRequest) {
    eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), IGNORED_UPDATE,
        "CE Update does not match any processor and so will be ignored");
  }
}
