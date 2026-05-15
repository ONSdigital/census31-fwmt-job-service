package uk.gov.ons.census.fwmt.jobservice.service.routing.ce;

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
import uk.gov.ons.census.fwmt.common.data.tm.SurveyType;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.http.comet.CometRestClient;
import uk.gov.ons.census.fwmt.jobservice.service.GatewayCacheService;
import uk.gov.ons.census.fwmt.jobservice.service.routing.RoutingValidator;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.SWITCH_ON_A_CANCEL;

@ExtendWith(MockitoExtension.class)
public class CeSwitchCreateProcessorTest {

  @InjectMocks
  private CeSwitchCreateProcessor ceSwitchCreateProcessor;

  @Mock
  private CometRestClient cometRestClient;

  @Mock
  private GatewayEventManager eventManager;

  @Mock
  private RoutingValidator routingValidator;

  @Mock
  private GatewayCacheService cacheService;

  @Captor
  private ArgumentCaptor<GatewayCache> spiedCache;

  @Captor
  private ArgumentCaptor<String> spiedEvent;

  private FwmtActionInstruction createInstruction() {
    return FwmtActionInstruction.builder().caseRef("345").build();
  }

  private GatewayCache createGatewayCache(String caseId, int type, int usualResidents) {
    return GatewayCache.builder().caseId(caseId).type(type).usualResidents(usualResidents).build();
  }

  @Test
  @DisplayName("Should throw Gateway Exception and trigger event for invalid survey type")
  public void shouldHandleIncorrectSurveyTypeCE() {
    final FwmtActionInstruction instruction = createInstruction();
    instruction.setSurveyType(SurveyType.AC);
    instruction.setCaseId("1234");
    Assertions.assertThrows(GatewayException.class, () -> {
      ceSwitchCreateProcessor.process(instruction, createGatewayCache("",0,0), Instant.now());
    });
  }

  @Test
  @DisplayName("Should set usualResident count to 0 when a valid CE_SITE is received")
  public void shouldHandleCE() throws GatewayException {
    final FwmtActionInstruction instruction = createInstruction();
    instruction.setSurveyType(SurveyType.CE_SITE);
    instruction.setCaseId("1234");
    GatewayCache cache = createGatewayCache("1234", 1, 10);
    ResponseEntity<Void> responseEntity = ResponseEntity.ok().build();
    when(cometRestClient.sendClose(any())).thenReturn(responseEntity);
    ceSwitchCreateProcessor.process(instruction, cache, Instant.now());
    verify(cacheService).save(spiedCache.capture());
    int usualResidents = spiedCache.getValue().usualResidents;
    Assertions.assertEquals(0, usualResidents);
  }

  @Test
  @DisplayName("Should ignore a CE Switch on a closed case in TM")
  public void shouldIgnoreACeSwitchOnAClosedCaseinTm() throws GatewayException {
    final FwmtActionInstruction instruction = createInstruction();
    instruction.setSurveyType(SurveyType.CE_SITE);
    instruction.setCaseId("1234");
    GatewayCache cache = createGatewayCache("1234", 1, 10);
    when(cometRestClient.sendClose(any())).thenThrow(new RestClientException("(400 BAD_REQUEST) {“id”:[“Case State must be Open”]}"));
    ceSwitchCreateProcessor.process(instruction, cache,  Instant.now());
    verify(eventManager, atLeast(2)).triggerEvent(any(), spiedEvent.capture(), any());
    String checkEvent = spiedEvent.getValue();
    Assertions.assertEquals(SWITCH_ON_A_CANCEL, checkEvent);
  }
}