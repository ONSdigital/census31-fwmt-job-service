package uk.gov.ons.census.fwmt.jobservice.service.processor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InboundProcessorConfigTest {

  private static final ProcessorKey ACTION_KEY = ProcessorKey.builder()
      .actionInstruction("CREATE")
      .surveyName("CENSUS")
      .addressType("HH")
      .addressLevel("U")
      .build();

  private static final ProcessorKey CANCEL_KEY = ProcessorKey.builder()
      .actionInstruction("CANCEL")
      .surveyName("CENSUS")
      .addressType("HH")
      .addressLevel("U")
      .build();

  private final InboundProcessorConfig config = new InboundProcessorConfig();

  @Mock
  private GatewayEventManager eventManager;

  @Mock
  private InboundProcessor<FwmtActionInstruction> actionProcessorA;

  @Mock
  private InboundProcessor<FwmtActionInstruction> actionProcessorB;

  @Mock
  private InboundProcessor<FwmtCancelActionInstruction> cancelProcessor;

  @Test
  void buildCreateProcessorRouter_groupsProcessorsByKey() throws GatewayException {
    FwmtActionInstruction request = buildCreateRequest();
    when(actionProcessorA.getKey()).thenReturn(ACTION_KEY);
    when(actionProcessorB.getKey()).thenReturn(ACTION_KEY);
    when(actionProcessorA.isValid(request, null)).thenReturn(false);
    when(actionProcessorB.isValid(request, null)).thenReturn(true);

    ProcessorRouter<FwmtActionInstruction> router = config.buildCreateProcessorRouter(
        List.of(actionProcessorA, actionProcessorB), eventManager);

    InboundProcessor<FwmtActionInstruction> resolved = router.resolveExactlyOne(ACTION_KEY, request, null);

    assertSame(actionProcessorB, resolved);
  }

  @Test
  void buildCancelProcessorRouter_supportsCancelProcessorType() throws GatewayException {
    FwmtCancelActionInstruction request = buildCancelRequest();
    when(cancelProcessor.getKey()).thenReturn(CANCEL_KEY);
    when(cancelProcessor.isValid(request, null)).thenReturn(true);

    ProcessorRouter<FwmtCancelActionInstruction> router = config.buildCancelProcessorRouter(
        List.of(cancelProcessor), eventManager);

    InboundProcessor<FwmtCancelActionInstruction> resolved = router.resolveExactlyOne(CANCEL_KEY, request, null);

    assertSame(cancelProcessor, resolved);
  }

  @Test
  void buildCreateProcessorRouter_groupsBothProcessorsUnderSameKey() throws GatewayException {
    FwmtActionInstruction request = buildCreateRequest();
    when(actionProcessorA.getKey()).thenReturn(ACTION_KEY);
    when(actionProcessorB.getKey()).thenReturn(ACTION_KEY);
    // First processor invalid, second valid — router must see both and return the valid one
    when(actionProcessorA.isValid(request, null)).thenReturn(false);
    when(actionProcessorB.isValid(request, null)).thenReturn(true);

    ProcessorRouter<FwmtActionInstruction> router = config.buildCreateProcessorRouter(
        List.of(actionProcessorA, actionProcessorB), eventManager);

    InboundProcessor<FwmtActionInstruction> resolved = router.resolveExactlyOne(ACTION_KEY, request, null);

    assertSame(actionProcessorB, resolved);
  }

  @Test
  void buildPauseProcessorRouter_emptyProcessors_resolveOptionalReturnsEmpty() throws GatewayException {
    FwmtActionInstruction request = buildCreateRequest();
    ProcessorRouter<FwmtActionInstruction> router = config.buildPauseProcessorRouter(List.of(), eventManager);

    Optional<InboundProcessor<FwmtActionInstruction>> result = router.resolveOptional(ACTION_KEY, request, null);

    assertTrue(result.isEmpty());
  }

  private FwmtActionInstruction buildCreateRequest() {
    FwmtActionInstruction request = new FwmtActionInstruction();
    request.setActionInstruction(ActionInstructionType.CREATE);
    request.setSurveyName("CENSUS");
    request.setAddressType("HH");
    request.setAddressLevel("U");
    request.setCaseId("case-id");
    return request;
  }

  private FwmtCancelActionInstruction buildCancelRequest() {
    FwmtCancelActionInstruction request = new FwmtCancelActionInstruction();
    request.setActionInstruction(ActionInstructionType.CANCEL);
    request.setSurveyName("CENSUS");
    request.setAddressType("HH");
    request.setAddressLevel("U");
    request.setCaseId("case-id");
    return request;
  }
}



