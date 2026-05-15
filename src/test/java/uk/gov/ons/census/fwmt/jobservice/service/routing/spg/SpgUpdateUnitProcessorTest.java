package uk.gov.ons.census.fwmt.jobservice.service.routing.spg;

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
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.helper.FwmtUpdateJobRequestBuilder;
import uk.gov.ons.census.fwmt.jobservice.http.comet.CometRestClient;
import uk.gov.ons.census.fwmt.jobservice.service.routing.RoutingValidator;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.UPDATE_ON_A_CANCEL;

@ExtendWith(MockitoExtension.class)
public class SpgUpdateUnitProcessorTest {

  @InjectMocks
  private SpgUpdateUnitProcessor spgUpdateUnitProcessor;

  @Mock
  private CometRestClient cometRestClient;

  @Mock
  private GatewayEventManager eventManager;

  @Mock
  private RoutingValidator routingValidator;

  @Captor
  private ArgumentCaptor<String> spiedEvent;

  @Test
  @DisplayName("Should ignore a SPG Update on a closed case in TM")
  public void shouldIgnoreASpgUpdateOnAClosedCaseinTm() throws GatewayException {
    final FwmtActionInstruction instruction = new FwmtUpdateJobRequestBuilder().createSpgUpdateUnit();
    GatewayCache gatewayCache = GatewayCache.builder()
        .caseId("ac623e62-4f4b-11eb-ae93-0242ac130002").lastActionInstruction("CREATE").build();
    when(cometRestClient.sendClose(any())).thenThrow(new RestClientException("(400 BAD_REQUEST) {“id”:[“Case State must be Open”]}"));
    spgUpdateUnitProcessor.process(instruction, gatewayCache,  Instant.now());
    verify(eventManager, atLeast(2)).triggerEvent(any(), spiedEvent.capture(), any());
    String checkEvent = spiedEvent.getValue();
    Assertions.assertEquals(UPDATE_ON_A_CANCEL, checkEvent);
  }
}