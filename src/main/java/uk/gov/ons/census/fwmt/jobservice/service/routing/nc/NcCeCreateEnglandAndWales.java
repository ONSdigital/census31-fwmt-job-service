package uk.gov.ons.census.fwmt.jobservice.service.routing.nc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.common.data.tm.CaseRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCaseRecord;
import uk.gov.ons.census.fwmt.jobservice.http.comet.CometRestClient;
import uk.gov.ons.census.fwmt.jobservice.service.GatewayCaseRecordService;
import uk.gov.ons.census.fwmt.jobservice.service.converter.nc.NcCreateConverter;
import uk.gov.ons.census.fwmt.jobservice.service.processor.InboundProcessor;
import uk.gov.ons.census.fwmt.jobservice.service.processor.ProcessorKey;
import uk.gov.ons.census.fwmt.jobservice.service.routing.RoutingValidator;

import java.time.Instant;

import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.*;
import static uk.gov.ons.census.fwmt.jobservice.service.routing.nc.NcEventValues.NC_CSV_LOAD_FAILURE;
import static uk.gov.ons.census.fwmt.jobservice.service.routing.nc.NcEventValues.NOT_EXIST_WITHIN_CACHE;

@Qualifier("Create")
@Service
public class NcCeCreateEnglandAndWales implements InboundProcessor<FwmtActionInstruction> {

  private static final ProcessorKey key = ProcessorKey.builder()
      .actionInstruction(ActionInstructionType.CREATE.toString())
      .surveyName("CENSUS")
      .addressType("CE")
      .addressLevel("E")
      .build();

  @Autowired
  private CometRestClient cometRestClient;

  @Autowired
  private GatewayEventManager eventManager;

  @Autowired
  private RoutingValidator routingValidator;

  @Autowired
  private GatewayCaseRecordService cacheService;

  @Override
  public ProcessorKey getKey() {
    return key;
  }

  @Override
  public boolean isValid(FwmtActionInstruction rmRequest, GatewayCaseRecord cache) {
    try {
      return rmRequest.getActionInstruction() == ActionInstructionType.CREATE
          && rmRequest.getSurveyName().equals("CENSUS")
          && !rmRequest.getOa().startsWith("N")
          && rmRequest.isNc();
    } catch (NullPointerException e) {
      return false;
    }
  }

  @Override
  public void process(FwmtActionInstruction rmRequest, GatewayCaseRecord cache, Instant messageReceivedTime)
      throws GatewayException {
    String ncCaseId = rmRequest.getCaseId();
    String originalCaseId = rmRequest.getOldCaseId();
    GatewayCaseRecord originalCache = cacheService.getById(originalCaseId);
    if (originalCache == null) {
      eventManager.triggerErrorEvent(this.getClass(), NOT_EXIST_WITHIN_CACHE, originalCaseId, NC_CSV_LOAD_FAILURE);
      throw new GatewayException(GatewayException.Fault.SYSTEM_ERROR, NOT_EXIST_WITHIN_CACHE);
    }

    CaseRequest tmRequest = NcCreateConverter.convertCeNcEnglandAndWales(rmRequest, cache, null, originalCache);

    eventManager.triggerEvent(ncCaseId, COMET_CREATE_PRE_SENDING,
        "Original case id", originalCaseId,
        "Case Ref", tmRequest.getReference(),
        "Survey Type", tmRequest.getSurveyType().toString());

    ResponseEntity<Void> response = cometRestClient.sendCreate(tmRequest, ncCaseId);
    routingValidator.validateResponseCode(response, ncCaseId,
        "Create", FAILED_TO_CREATE_TM_JOB,
        "tmRequest", tmRequest.toString(),
        "rmRequest", rmRequest.toString(),
        "cache", (cache != null) ? cache.toString() : "");

    cacheService.save(GatewayCaseRecord
        .builder()
        .caseId(ncCaseId)
        .originalCaseId(originalCaseId)
        .existsInFwmt(true)
        .careCodes(originalCache.getCareCodes())
        .accessInfo(originalCache.getAccessInfo())
        .type(1)
        .lastActionInstruction(rmRequest.getActionInstruction().toString())
        .lastActionTime(messageReceivedTime)
        .build());

    eventManager.triggerEvent(ncCaseId, COMET_CREATE_ACK,
        "Original case id", originalCaseId,
        "Case Ref", rmRequest.getCaseRef(),
        "Response Code", response.getStatusCode().toString(),
        "Survey Type", tmRequest.getSurveyType().toString(),
        "NC CE Create England And Wales", tmRequest.toString());
  }
}
