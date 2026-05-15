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
import org.springframework.web.client.RestClientException;
import uk.gov.ons.census.fwmt.common.data.tm.CasePauseRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.hh.HhRequestBuilder;
import uk.gov.ons.census.fwmt.jobservice.http.comet.CometRestClient;
import uk.gov.ons.census.fwmt.jobservice.service.routing.RoutingValidator;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.CASE_DOES_NOT_EXIST;

@ExtendWith(MockitoExtension.class)
class HHCancelProcessorTest {

  @InjectMocks
  private HhCancel hhCancel;

  @Mock
  private CometRestClient cometRestClient;

  @Mock
  private GatewayEventManager eventManager;

  @Mock
  private RoutingValidator routingValidator;

  @Captor
  private ArgumentCaptor<String> spiedEvent;

  @Test
  @DisplayName("Should ignore a HH cancel on a closed case in TM")
  public void shouldIgnoreAHhCancelOnAClosedCaseinTm() throws GatewayException {
    final FwmtCancelActionInstruction instruction = HhRequestBuilder.cancelActionInstruction();
    GatewayCache gatewayCache = GatewayCache.builder()
        .caseId("ac623e62-4f4b-11eb-ae93-0242ac130002").lastActionInstruction("CREATE").build();
    when(cometRestClient.sendPause(any(CasePauseRequest.class), eq(instruction.getCaseId())))
        .thenThrow(new RestClientException("(404 BAD_REQUEST) {“id”:[“Unable to find Case”]}"));
    hhCancel.process(instruction, gatewayCache,  Instant.now());
    verify(eventManager, atLeast(2)).triggerEvent(any(), spiedEvent.capture(), any());
    String checkEvent = spiedEvent.getValue();
    Assertions.assertEquals(CASE_DOES_NOT_EXIST, checkEvent);
  }
}