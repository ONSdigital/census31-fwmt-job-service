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
import org.springframework.web.client.RestClientException;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.helper.NcActionInstructionBuilder;
import uk.gov.ons.census.fwmt.jobservice.http.comet.CometRestClient;
import uk.gov.ons.census.fwmt.jobservice.http.rm.RmRestClient;
import uk.gov.ons.census.fwmt.jobservice.service.GatewayCacheService;
import uk.gov.ons.census.fwmt.jobservice.service.routing.RoutingValidator;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.CANCEL_ON_A_CANCEL;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_CANCEL_ACK;

@ExtendWith(MockitoExtension.class)
public class NcCancelProcessorTest {

  @InjectMocks
  private NcHhCancel ncHhCancel;

  @InjectMocks
  private NcCeCancel ncCeCancel;

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

  @Mock
  private ResponseEntity<Void> responseEntity;

  @Captor
  private ArgumentCaptor<GatewayCache> spiedCache;

  @Captor
  private ArgumentCaptor<String> spiedEvent;

  @Test
  @DisplayName("Should send cancel NC HH caseId to TM")
  public void shouldHandleNCHHCancel() throws GatewayException {
    final FwmtCancelActionInstruction instruction = new NcActionInstructionBuilder().createNcHhCancelInstruction();
    GatewayCache gatewayCache = GatewayCache.builder()
        .caseId("c66c995e-571d-11eb-ae93-0242ac130002").careCodes("Mind dog").accessInfo("1234")
        .originalCaseId("ac623e62-4f4b-11eb-ae93-0242ac130002").lastActionInstruction("CREATED").build();
    ResponseEntity<Void> responseEntity = ResponseEntity.ok().build();
    when(cometRestClient.sendClose(gatewayCache.caseId)).thenReturn(responseEntity);
    ncHhCancel.process(instruction, gatewayCache, Instant.now());
    verify(cacheService).save(spiedCache.capture());
    String caseId = spiedCache.getValue().caseId;
    String lastAction = spiedCache.getValue().lastActionInstruction;
    Assertions.assertEquals("c66c995e-571d-11eb-ae93-0242ac130002", caseId);
    Assertions.assertEquals("CANCEL", lastAction);
    verify(eventManager, atLeast(2)).triggerEvent(any(), spiedEvent.capture(), any());
    String checkEvent = spiedEvent.getValue();
    Assertions.assertEquals(COMET_CANCEL_ACK, checkEvent);
  }

  @Test
  @DisplayName("Should send cancel NC CE caseId to TM")
  public void shouldHandleNCCECancel() throws GatewayException {
    final FwmtCancelActionInstruction instruction = new NcActionInstructionBuilder().createNcCeCancelInstruction();
    GatewayCache gatewayCache = GatewayCache.builder()
        .caseId("c66c995e-571d-11eb-ae93-0242ac130002").careCodes("Mind dog").accessInfo("1234")
        .originalCaseId("ac623e62-4f4b-11eb-ae93-0242ac130002").lastActionInstruction("CREATED").build();
    ResponseEntity<Void> responseEntity = ResponseEntity.ok().build();
    when(cometRestClient.sendClose(gatewayCache.caseId)).thenReturn(responseEntity);
    ncCeCancel.process(instruction, gatewayCache, Instant.now());
    verify(cacheService).save(spiedCache.capture());
    String caseId = spiedCache.getValue().caseId;
    String lastAction = spiedCache.getValue().lastActionInstruction;
    Assertions.assertEquals("c66c995e-571d-11eb-ae93-0242ac130002", caseId);
    Assertions.assertEquals("CANCEL", lastAction);
    verify(eventManager, atLeast(2)).triggerEvent(any(), spiedEvent.capture(), any());
    String checkEvent = spiedEvent.getValue();
    Assertions.assertEquals(COMET_CANCEL_ACK, checkEvent);
  }

  @Test
  @DisplayName("Should ignore a NC HH cancel on a cancel")
  public void shouldIgnoreANcHHCancelOnCancel() throws GatewayException {
    final FwmtCancelActionInstruction instruction = new NcActionInstructionBuilder().createNcHhCancelInstruction();
    GatewayCache gatewayCache = GatewayCache.builder()
        .caseId("ac623e62-4f4b-11eb-ae93-0242ac130002").lastActionInstruction("CREATE").build();
    when(cometRestClient.sendClose(any())).thenThrow(new RestClientException("(400 BAD_REQUEST) {“id”:[“Case State must be Open”]}"));
    ncHhCancel.process(instruction, gatewayCache,  Instant.now());
    verify(cacheService).save(spiedCache.capture());
    String lastActionInstruction = spiedCache.getValue().lastActionInstruction;
    Assertions.assertEquals(instruction.getActionInstruction().toString(), lastActionInstruction);
    verify(eventManager, atLeast(2)).triggerEvent(any(), spiedEvent.capture(), any());
    String checkEvent = spiedEvent.getValue();
    Assertions.assertEquals(CANCEL_ON_A_CANCEL, checkEvent);
  }

  @Test
  @DisplayName("Should ignore a NC CE cancel on a cancel")
  public void shouldIgnoreANcCeCancelOnCancel() throws GatewayException {
    final FwmtCancelActionInstruction instruction = new NcActionInstructionBuilder().createNcCeCancelInstruction();
    GatewayCache gatewayCache = GatewayCache.builder()
        .caseId("ac623e62-4f4b-11eb-ae93-0242ac130002").lastActionInstruction("CREATE").build();
    when(cometRestClient.sendClose(any())).thenThrow(new RestClientException("(400 BAD_REQUEST) {“id”:[“Case State must be Open”]}"));
    ncCeCancel.process(instruction, gatewayCache,  Instant.now());
    verify(cacheService).save(spiedCache.capture());
    String lastActionInstruction = spiedCache.getValue().lastActionInstruction;
    Assertions.assertEquals(instruction.getActionInstruction().toString(), lastActionInstruction);
    verify(eventManager, atLeast(2)).triggerEvent(any(), spiedEvent.capture(), any());
    String checkEvent = spiedEvent.getValue();
    Assertions.assertEquals(CANCEL_ON_A_CANCEL, checkEvent);
  }
}