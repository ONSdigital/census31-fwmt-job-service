package uk.gov.ons.census.fwmt.jobservice.service.processor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.dto.rm.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.dto.rm.ActionInstruction;
import uk.gov.ons.census.fwmt.common.dto.rm.CancelActionInstruction;
import uk.gov.ons.census.fwmt.common.events.component.GatewayEventManager;

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
  private InboundProcessor<ActionInstruction> actionProcessorA;

  @Mock
  private InboundProcessor<ActionInstruction> actionProcessorB;

  @Mock
  private InboundProcessor<CancelActionInstruction> cancelProcessor;

  @Test
  void buildCreateProcessorRouter_groupsProcessorsByKey() throws GatewayException {
    ActionInstruction request = buildCreateRequest();
    when(actionProcessorA.getKey()).thenReturn(ACTION_KEY);
    when(actionProcessorB.getKey()).thenReturn(ACTION_KEY);
    when(actionProcessorA.isValid(request, null)).thenReturn(false);
    when(actionProcessorB.isValid(request, null)).thenReturn(true);

    ProcessorRouter<ActionInstruction> router = config.buildCreateProcessorRouter(
        List.of(actionProcessorA, actionProcessorB), eventManager);

    InboundProcessor<ActionInstruction> resolved = router.resolveExactlyOne(ACTION_KEY, request, null);

    assertSame(actionProcessorB, resolved);
  }

  @Test
  void buildCancelProcessorRouter_supportsCancelProcessorType() throws GatewayException {
    CancelActionInstruction request = buildCancelRequest();
    when(cancelProcessor.getKey()).thenReturn(CANCEL_KEY);
    when(cancelProcessor.isValid(request, null)).thenReturn(true);

    ProcessorRouter<CancelActionInstruction> router = config.buildCancelProcessorRouter(
        List.of(cancelProcessor), eventManager);

    InboundProcessor<CancelActionInstruction> resolved = router.resolveExactlyOne(CANCEL_KEY, request, null);

    assertSame(cancelProcessor, resolved);
  }

  @Test
  void buildCreateProcessorRouter_groupsBothProcessorsUnderSameKey() throws GatewayException {
    ActionInstruction request = buildCreateRequest();
    when(actionProcessorA.getKey()).thenReturn(ACTION_KEY);
    when(actionProcessorB.getKey()).thenReturn(ACTION_KEY);
    // First processor invalid, second valid — router must see both and return the valid one
    when(actionProcessorA.isValid(request, null)).thenReturn(false);
    when(actionProcessorB.isValid(request, null)).thenReturn(true);

    ProcessorRouter<ActionInstruction> router = config.buildCreateProcessorRouter(
        List.of(actionProcessorA, actionProcessorB), eventManager);

    InboundProcessor<ActionInstruction> resolved = router.resolveExactlyOne(ACTION_KEY, request, null);

    assertSame(actionProcessorB, resolved);
  }

  @Test
  void buildPauseProcessorRouter_emptyProcessors_resolveOptionalReturnsEmpty() throws GatewayException {
    ActionInstruction request = buildCreateRequest();
    ProcessorRouter<ActionInstruction> router = config.buildPauseProcessorRouter(List.of(), eventManager);

    Optional<InboundProcessor<ActionInstruction>> result = router.resolveOptional(ACTION_KEY, request, null);

    assertTrue(result.isEmpty());
  }

  private ActionInstruction buildCreateRequest() {
    ActionInstruction request = new ActionInstruction();
    request.setActionInstruction(ActionInstructionType.CREATE);
    request.setSurveyName("CENSUS");
    request.setAddressType("HH");
    request.setCaseId("case-id");
    return request;
  }

  private CancelActionInstruction buildCancelRequest() {
    CancelActionInstruction request = new CancelActionInstruction();
    request.setActionInstruction(ActionInstructionType.CANCEL);
    request.setSurveyName("CENSUS");
    request.setAddressType("HH");
    request.setAddressLevel("U");
    request.setCaseId("case-id");
    return request;
  }
}



