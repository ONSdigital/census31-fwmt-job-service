package uk.gov.ons.census.fwmt.jobservice.service.routing.nc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.ons.census.fwmt.common.data.nc.CaseDetailsDTO;
import uk.gov.ons.census.fwmt.common.data.tm.CaseRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.helper.NcActionInstructionBuilder;
import uk.gov.ons.census.fwmt.jobservice.http.comet.CometRestClient;
import uk.gov.ons.census.fwmt.jobservice.http.rm.RmRestClient;
import uk.gov.ons.census.fwmt.jobservice.service.GatewayCacheService;
import uk.gov.ons.census.fwmt.jobservice.service.routing.RoutingValidator;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NcCreateProcessorTest {

  @InjectMocks
  private NcHhCreateEnglandAndWales ncHhCreateEnglandAndWales;

  @Mock
  private CometRestClient cometRestClient;

  @Mock
  private GatewayCacheService cacheService;

  @Mock
  private GatewayCache gatewayCache;

  @Mock
  private GatewayEventManager eventManager;

  @Mock
  private RmRestClient rmRestClient;

  @Mock
  private RoutingValidator routingValidator;

  @Captor
  private ArgumentCaptor<GatewayCache> spiedCache;

  @Test
  @DisplayName("Should save the original case id")
  public void shouldHandleIncorrectSurveyTypeCE() throws GatewayException {
    final FwmtActionInstruction instruction = new NcActionInstructionBuilder().createNcActionInstruction();
    final GatewayCache originalCache = GatewayCache.builder()
        .caseId("ac623e62-4f4b-11eb-ae93-0242ac130002").careCodes("Mind dog").accessInfo("1234").build();

    ResponseEntity<Void> responseEntity = ResponseEntity.ok().build();
    when(cacheService.getById(anyString())).thenReturn(originalCache);
    when(cometRestClient.sendCreate(any(CaseRequest.class), eq(instruction.getCaseId()))).thenReturn(responseEntity);
    ncHhCreateEnglandAndWales.process(instruction, null, Instant.now());

    verify(cacheService).save(spiedCache.capture());
    String originalCaseId = spiedCache.getValue().originalCaseId;
    Assertions.assertEquals(instruction.getOldCaseId(), originalCaseId);
  }

  @Test
  @DisplayName("Should not error if a null refusal value is present")
  public void shouldHandleNullRefusalValue() throws GatewayException {
    final FwmtActionInstruction instruction = new NcActionInstructionBuilder().createNcActionInstruction();
    final CaseDetailsDTO caseDetailsDTO = new CaseDetailsDTO();
    final GatewayCache originalCache = new GatewayCache();

    when(cacheService.getById(anyString())).thenReturn(originalCache);
    when(rmRestClient.getCase(instruction.getOldCaseId())).thenReturn(caseDetailsDTO);
    ResponseEntity<Void> responseEntity = ResponseEntity.ok().build();
    when(cometRestClient.sendCreate(any(CaseRequest.class), eq(instruction.getCaseId()))).thenReturn(responseEntity);

    Assertions.assertDoesNotThrow(() -> {
      ncHhCreateEnglandAndWales.process(instruction, null, Instant.now());
    });
  }

  @Test
  @DisplayName("Should error if original case not in cache")
  public void shouldErrorIfOriginalCaseDoesntExistInCache() {
    final FwmtActionInstruction instruction = new NcActionInstructionBuilder().createNcActionInstruction();

    GatewayException exception = assertThrows(GatewayException.class, () -> {
      ncHhCreateEnglandAndWales.process(instruction, null, Instant.now());
    });

    GatewayException.Fault expectedFault = GatewayException.Fault.SYSTEM_ERROR;
    GatewayException.Fault actualFault = exception.getFault();
    String expectedErrorMessage = "Original case does not exist within cache";
    String actualErrorMessage = exception.getMessage();

    assertEquals(expectedFault, actualFault);
    assertEquals(expectedErrorMessage, actualErrorMessage);
  }
}