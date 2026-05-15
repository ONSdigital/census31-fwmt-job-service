package uk.gov.ons.census.fwmt.jobservice.service.routing.internal;

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
import uk.gov.ons.census.fwmt.jobservice.helper.FwmtCancelJobRequestBuilder;
import uk.gov.ons.census.fwmt.jobservice.http.comet.CometRestClient;
import uk.gov.ons.census.fwmt.jobservice.service.GatewayCacheService;
import uk.gov.ons.census.fwmt.jobservice.service.routing.RoutingValidator;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.CANCEL_ON_A_CANCEL;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_CANCEL_ACK;

@ExtendWith(MockitoExtension.class)
public class FeedbackCancelProcessorTest {

  @InjectMocks
  private FeedbackCancel feedbackCancel;

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

  @Test
  @DisplayName("Should send a Feedback cancel")
  public void shouldSendAFeedbackCancel() throws GatewayException {
    final FwmtCancelActionInstruction instruction = new FwmtCancelJobRequestBuilder().cancelFeedbackActionInstruction();
    GatewayCache gatewayCache = GatewayCache.builder()
        .caseId("ac623e62-4f4b-11eb-ae93-0242ac130002").lastActionInstruction("CREATE").build();
    ResponseEntity<Void> responseEntity = ResponseEntity.ok().build();
    when(cometRestClient.sendClose(any())).thenReturn(responseEntity);
    when(cacheService.getById(anyString())).thenReturn(gatewayCache);
    feedbackCancel.process(instruction, gatewayCache,  Instant.now());
    verify(cacheService).save(spiedCache.capture());
    String lastActionInstruction = spiedCache.getValue().lastActionInstruction;
    Assertions.assertEquals(instruction.getActionInstruction().toString(), lastActionInstruction);
    verify(eventManager, atLeast(2)).triggerEvent(any(), spiedEvent.capture(), any());
    String checkEvent = spiedEvent.getValue();
    Assertions.assertEquals(COMET_CANCEL_ACK, checkEvent);
  }

  @Test
  @DisplayName("Should ignore a Feedback cancel on a cancel")
  public void shouldIgnoreAFeedbackCancelOnCancel() throws GatewayException {
    final FwmtCancelActionInstruction instruction = new FwmtCancelJobRequestBuilder().cancelFeedbackActionInstruction();
    GatewayCache gatewayCache = GatewayCache.builder()
        .caseId("ac623e62-4f4b-11eb-ae93-0242ac130002").lastActionInstruction("CREATE").build();
    when(cometRestClient.sendClose(any())).thenThrow(new RestClientException("(400 BAD_REQUEST) {“id”:[“Case State must be Open”]}"));
    when(cacheService.getById(anyString())).thenReturn(gatewayCache);
    feedbackCancel.process(instruction, gatewayCache,  Instant.now());
    verify(cacheService).save(spiedCache.capture());
    String lastActionInstruction = spiedCache.getValue().lastActionInstruction;
    Assertions.assertEquals(instruction.getActionInstruction().toString(), lastActionInstruction);
    verify(eventManager, atLeast(2)).triggerEvent(any(), spiedEvent.capture(), any());
    String checkEvent = spiedEvent.getValue();
    Assertions.assertEquals(CANCEL_ON_A_CANCEL, checkEvent);
  }
}