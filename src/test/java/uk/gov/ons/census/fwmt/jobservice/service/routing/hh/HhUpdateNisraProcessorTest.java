package uk.gov.ons.census.fwmt.jobservice.service.routing.hh;

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
import uk.gov.ons.census.fwmt.common.data.tm.CaseRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.hh.HhRequestBuilder;
import uk.gov.ons.census.fwmt.jobservice.http.comet.CometRestClient;
import uk.gov.ons.census.fwmt.jobservice.service.GatewayCacheService;
import uk.gov.ons.census.fwmt.jobservice.service.routing.RoutingValidator;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HhUpdateNisraProcessorTest {

  @InjectMocks
  private HhUpdateNisra hhpause;

  @Mock
  private CometRestClient cometRestClient;

  @Mock
  private GatewayCacheService cacheService;

  @Mock
  private GatewayCache gatewayCache;

  @Mock
  private GatewayEventManager eventManager;

  @Mock
  private RoutingValidator routingValidator;

  @Captor
  private ArgumentCaptor<GatewayCache> spiedCache;

  @Captor
  private ArgumentCaptor<CaseRequest> tmRequest;

  @Test
  @DisplayName("Should send NISRA required officer to TM as update ")
  public void shouldSendNisraRequiredOfficerToTmAsUpdate() throws GatewayException {
    final FwmtActionInstruction instruction = HhRequestBuilder.updateActionInstruction();
    GatewayCache gatewayCache = GatewayCache.builder()
        .caseId("ac623e62-4f4b-11eb-ae93-0242ac130002").build();
    when(cacheService.getById(anyString())).thenReturn(gatewayCache);
    ResponseEntity<Void> responseEntity = ResponseEntity.ok().build();
    when(cometRestClient.sendCreate(any(CaseRequest.class), eq(instruction.getCaseId()))).thenReturn(responseEntity);
    hhpause.process(instruction, gatewayCache, Instant.now());
    verify(cometRestClient).sendCreate(tmRequest.capture(), any());
    CaseRequest caseRequest = tmRequest.getValue();
    verify(cacheService).save(spiedCache.capture());
    String lastActionInstruction = spiedCache.getValue().lastActionInstruction;
    Assertions.assertEquals(instruction.getFieldOfficerId(), caseRequest.getRequiredOfficer());
    Assertions.assertEquals("UPDATE", lastActionInstruction);
  }
}