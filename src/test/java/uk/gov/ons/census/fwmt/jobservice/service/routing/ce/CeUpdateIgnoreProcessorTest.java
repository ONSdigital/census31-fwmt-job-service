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
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.ce.CeRequestBuilder;
import uk.gov.ons.census.fwmt.jobservice.service.routing.ignore.CeUpdateIgnoreProcessor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.IGNORED_UPDATE;

@ExtendWith(MockitoExtension.class)
public class CeUpdateIgnoreProcessorTest {

  @InjectMocks
  private CeUpdateIgnoreProcessor ceUpdateIgnoreProcessor;

  @Mock
  private GatewayEventManager eventManager;

  @Captor
  private ArgumentCaptor<String> spiedEvent;

  @Test
  @DisplayName("Should log CE Update and ignore it")
  public void shouldLogCCeUpdateAndIgnoreIt() {
    final FwmtActionInstruction instruction = CeRequestBuilder.ceUpdateInstruction();
    ceUpdateIgnoreProcessor.process(instruction);
    verify(eventManager).triggerEvent(any(), spiedEvent.capture(), any());
    String checkEvent = spiedEvent.getValue();
    Assertions.assertEquals(IGNORED_UPDATE, checkEvent);
  }
}