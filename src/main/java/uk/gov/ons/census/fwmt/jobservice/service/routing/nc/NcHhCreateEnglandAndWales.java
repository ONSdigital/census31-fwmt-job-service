package uk.gov.ons.census.fwmt.jobservice.service.routing.nc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.common.data.nc.CaseDetailsDTO;
import uk.gov.ons.census.fwmt.common.data.nc.RefusalTypeDTO;
import uk.gov.ons.census.fwmt.common.data.tm.CaseRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.http.comet.CometRestClient;
import uk.gov.ons.census.fwmt.jobservice.http.rm.RmRestClient;
import uk.gov.ons.census.fwmt.jobservice.nc.utils.NamedHouseholderRetrieval;
import uk.gov.ons.census.fwmt.jobservice.service.GatewayCacheService;
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
public class NcHhCreateEnglandAndWales implements InboundProcessor<FwmtActionInstruction> {

  private static final ProcessorKey key = ProcessorKey.builder()
      .actionInstruction(ActionInstructionType.CREATE.toString())
      .surveyName("CENSUS")
      .addressType("HH")
      .addressLevel("U")
      .build();

  @Autowired
  private CometRestClient cometRestClient;

  @Autowired
  private GatewayEventManager eventManager;

  @Autowired
  private RmRestClient rmRestClient;

  @Autowired
  private RoutingValidator routingValidator;

  @Autowired
  private GatewayCacheService cacheService;

  @Autowired
  private NamedHouseholderRetrieval namedHouseholderRetrieval;

  @Override
  public ProcessorKey getKey() {
    return key;
  }

  @Override
  public boolean isValid(FwmtActionInstruction rmRequest, GatewayCache cache) {
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
  public void process(FwmtActionInstruction rmRequest, GatewayCache cache, Instant messageReceivedTime)
      throws GatewayException {
    CaseDetailsDTO houseHolderDetails;
    String ncCaseId = rmRequest.getCaseId();
    String originalCaseId = rmRequest.getOldCaseId();
    GatewayCache originalCache = cacheService.getById(originalCaseId);
    if (originalCache == null) {
      eventManager.triggerErrorEvent(this.getClass(), NOT_EXIST_WITHIN_CACHE, originalCaseId, NC_CSV_LOAD_FAILURE);
      throw new GatewayException(GatewayException.Fault.SYSTEM_ERROR, NOT_EXIST_WITHIN_CACHE);
    }

    try {
      houseHolderDetails = rmRestClient.getCase(originalCaseId);
    } catch (RuntimeException e) {
      eventManager.triggerEvent(originalCaseId, "NO_HOUSEHOLDER_DETAILS",
          "ncCaseId", ncCaseId,
          "Type", "NC");
      houseHolderDetails = null;
    }

    String householder = "";
    if (houseHolderDetails != null && houseHolderDetails.getRefusalReceived() != null
        && houseHolderDetails.getRefusalReceived().equals(RefusalTypeDTO.HARD_REFUSAL)) {
      householder = namedHouseholderRetrieval.getAndSortRmRefusalCases(ncCaseId, houseHolderDetails);
    }

    CaseRequest tmRequest = NcCreateConverter.convertHhNcEnglandAndWales(rmRequest, cache, householder, originalCache);

    eventManager.triggerEvent(ncCaseId, COMET_CREATE_PRE_SENDING,
        "Case Ref", tmRequest.getReference(),
        "Original case id", originalCaseId,
        "Survey Type", tmRequest.getSurveyType().toString());

    ResponseEntity<Void> response = cometRestClient.sendCreate(tmRequest, ncCaseId);
    routingValidator.validateResponseCode(response, ncCaseId,
        "Create", FAILED_TO_CREATE_TM_JOB,
        "tmRequest", tmRequest.toString(),
        "rmRequest", rmRequest.toString(),
        "cache", (cache != null) ? cache.toString() : "");

    cacheService.save(GatewayCache
        .builder()
        .caseId(ncCaseId)
        .originalCaseId(originalCaseId)
        .existsInFwmt(true)
        .type(10)
        .careCodes(originalCache.getCareCodes())
        .accessInfo(originalCache.getAccessInfo())
        .lastActionInstruction(rmRequest.getActionInstruction().toString())
        .lastActionTime(messageReceivedTime)
        .build());

    eventManager.triggerEvent(ncCaseId, COMET_CREATE_ACK,
        "Original case id", originalCaseId,
        "Case Ref", rmRequest.getCaseRef(),
        "Response Code", response.getStatusCode().name(),
        "Survey Type", tmRequest.getSurveyType().toString(),
        "NC HH Create England And Wales", tmRequest.toString());
  }
}
