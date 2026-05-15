package uk.gov.ons.census.fwmt.jobservice.service.routing.ce;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.common.data.tm.CaseRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.http.comet.CometRestClient;
import uk.gov.ons.census.fwmt.jobservice.service.GatewayCacheService;
import uk.gov.ons.census.fwmt.jobservice.service.converter.ce.CeCreateConverter;
import uk.gov.ons.census.fwmt.jobservice.service.processor.InboundProcessor;
import uk.gov.ons.census.fwmt.jobservice.service.processor.ProcessorKey;
import uk.gov.ons.census.fwmt.jobservice.service.routing.RoutingValidator;

import java.io.FileWriter;
import java.time.Instant;

import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_CREATE_ACK;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_CREATE_PRE_SENDING;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.FAILED_TO_CREATE_TM_JOB;

@Qualifier("Create")
@Service
public class CeCreateSiteProcessor implements InboundProcessor<FwmtActionInstruction> {

  @Autowired
  private CometRestClient cometRestClient;

  @Autowired
  private GatewayEventManager eventManager;

  @Autowired
  private RoutingValidator routingValidator;

  @Autowired
  private GatewayCacheService cacheService;

  private static ProcessorKey key = ProcessorKey.builder()
      .actionInstruction(ActionInstructionType.CREATE.toString())
      .surveyName("CENSUS")
      .addressType("CE")
      .addressLevel("E")
      .build();

  @Override
  public ProcessorKey getKey() {
    return key;
  }

  //region agent log
  private static String agentJson(Object value) {
    if (value == null) {
      return "null";
    }
    if (value instanceof Number || value instanceof Boolean) {
      return String.valueOf(value);
    }
    return "\"" + String.valueOf(value).replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"";
  }

  private static void agentLog(String hypothesisId, String message, String data) {
    try (FileWriter writer = new FileWriter("/home/simon/dev/workspaces/cursor/census21-workspace/.cursor/debug-28a10e.log", true)) {
      writer.write("{\"sessionId\":\"28a10e\",\"runId\":\"pre-fix\",\"hypothesisId\":");
      writer.write(agentJson(hypothesisId));
      writer.write(",\"location\":\"CeCreateSiteProcessor.java:isValid\",\"message\":");
      writer.write(agentJson(message));
      writer.write(",\"data\":");
      writer.write(data);
      writer.write(",\"timestamp\":");
      writer.write(String.valueOf(System.currentTimeMillis()));
      writer.write("}\n");
    } catch (Exception ignored) {
    }
  }
  //endregion

  @Override
  public boolean isValid(FwmtActionInstruction rmRequest, GatewayCache cache) {
    try {
      boolean estabUprnAndTypeExists = cacheService.doesEstabUprnAndTypeExist(rmRequest.getUprn(), 3);
      boolean result = rmRequest.getActionInstruction() == ActionInstructionType.CREATE
          && rmRequest.getSurveyName().equals("CENSUS")
          && rmRequest.getAddressType().equals("CE")
          && rmRequest.getAddressLevel().equals("E")
          && (cache == null
          || !cache.existsInFwmt)
          && estabUprnAndTypeExists
          && !rmRequest.isNc();
      //region agent log
      agentLog("H2,H3", "CE Site processor validity",
          "{\"caseId\":" + agentJson(rmRequest.getCaseId())
              + ",\"addressType\":" + agentJson(rmRequest.getAddressType())
              + ",\"addressLevel\":" + agentJson(rmRequest.getAddressLevel())
              + ",\"uprn\":" + agentJson(rmRequest.getUprn())
              + ",\"estabUprn\":" + agentJson(rmRequest.getEstabUprn())
              + ",\"handDeliver\":" + agentJson(rmRequest.isHandDeliver())
              + ",\"cache\":" + agentJson(cache)
              + ",\"estabUprnAndTypeExists\":" + estabUprnAndTypeExists
              + ",\"result\":" + result
              + "}");
      //endregion
      return result;
    } catch (NullPointerException e) {
      //region agent log
      agentLog("H2,H3", "CE Site processor validity threw NullPointerException",
          "{\"caseId\":" + agentJson(rmRequest.getCaseId()) + ",\"result\":false}");
      //endregion
      return false;
    }
  }

  @Override
  public void process(FwmtActionInstruction rmRequest, GatewayCache cache, Instant messageReceivedTime) throws GatewayException {
    CaseRequest tmRequest;

    if (rmRequest.isSecureEstablishment()){
      tmRequest = CeCreateConverter.convertCeSiteSecure(rmRequest, cache);
    }else{
      tmRequest = CeCreateConverter.convertCeSite(rmRequest, cache);
    }

    eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), COMET_CREATE_PRE_SENDING, "Case Ref", tmRequest.getReference(), "Survey Type",
        tmRequest.getSurveyType().toString());

    ResponseEntity<Void> response = cometRestClient.sendCreate(tmRequest, rmRequest.getCaseId());
    routingValidator.validateResponseCode(response, rmRequest.getCaseId(), "Create", FAILED_TO_CREATE_TM_JOB, "tmRequest", tmRequest.toString(), "rmRequest", rmRequest.toString(), "cache", (cache!=null)?cache.toString():"");

    GatewayCache newCache = cacheService.getById(rmRequest.getCaseId());
    if (newCache == null) {
      cacheService.save(GatewayCache.builder().caseId(rmRequest.getCaseId()).existsInFwmt(true)
          .uprn(rmRequest.getUprn()).estabUprn(rmRequest.getEstabUprn()).type(2).lastActionInstruction(rmRequest.getActionInstruction().toString())
          .lastActionTime(messageReceivedTime).build());
    } else {
      cacheService.save(newCache.toBuilder().existsInFwmt(true).uprn(rmRequest.getUprn()).estabUprn(rmRequest.getEstabUprn())
          .type(2).lastActionInstruction(rmRequest.getActionInstruction().toString())
          .lastActionTime(messageReceivedTime).build());
    }

    eventManager
        .triggerEvent(String.valueOf(rmRequest.getCaseId()), COMET_CREATE_ACK,
            "Case Ref", rmRequest.getCaseRef(),
            "CE Create Site", tmRequest.toString(),
            "Response Code", response.getStatusCode().name(),
            "Survey Type", tmRequest.getSurveyType().toString());

  }
}
