package uk.gov.ons.census.fwmt.jobservice.service.processor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstruction;
import uk.gov.ons.census.fwmt.common.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCaseRecord;
import uk.gov.ons.census.fwmt.jobservice.service.JobService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.ROUTING_FAILED;

@ExtendWith(MockitoExtension.class)
class ProcessorRouterTest {

  private static final String CASE_ID = "case-id";

  private static final ProcessorKey KEY = ProcessorKey.builder()
      .actionInstruction("CREATE")
      .surveyName("CENSUS")
      .addressType("HH")
      .build();

  @Mock
  private GatewayEventManager eventManager;

  @Mock
  private InboundProcessor<ActionInstruction> processorA;

  @Mock
  private InboundProcessor<ActionInstruction> processorB;

  @Test
  void resolveExactlyOne_returnsSingleValidProcessor() throws GatewayException {
    ActionInstruction request = buildRequest();
    ProcessorRouter<ActionInstruction> router = new ProcessorRouter<>(
        Map.of(KEY, List.of(processorA)), eventManager, "CREATE", JobService.class);

    when(processorA.isValid(request, null)).thenReturn(true);

    InboundProcessor<ActionInstruction> result = router.resolveExactlyOne(KEY, request, null);

    assertSame(processorA, result);
  }

  @Test
  void resolveExactlyOne_filtersOutInvalidProcessors() throws GatewayException {
    ActionInstruction request = buildRequest();
    ProcessorRouter<ActionInstruction> router = new ProcessorRouter<>(
        Map.of(KEY, List.of(processorA, processorB)), eventManager, "CREATE", JobService.class);

    when(processorA.isValid(request, null)).thenReturn(false);
    when(processorB.isValid(request, null)).thenReturn(true);

    InboundProcessor<ActionInstruction> result = router.resolveExactlyOne(KEY, request, null);

    assertSame(processorB, result);
  }

  @Test
  void resolveExactlyOne_noMatchingProcessors_throwsAndTriggersEvent() {
    ActionInstruction request = buildRequest();
    ProcessorRouter<ActionInstruction> router = new ProcessorRouter<>(
        Map.of(KEY, List.of(processorA)), eventManager, "CREATE", JobService.class);
    GatewayCaseRecord cache = GatewayCaseRecord.builder().caseId(CASE_ID).existsInFwmt(true).build();

    when(processorA.isValid(request, cache)).thenReturn(false);

    GatewayException exception = assertThrows(GatewayException.class,
        () -> router.resolveExactlyOne(KEY, request, cache));

    assertEquals(GatewayException.Fault.VALIDATION_FAILED, exception.getFault());
    assertEquals("Could not find a CREATE processor for request from RM", exception.getMessage());
    verify(eventManager).triggerErrorEvent(eq(JobService.class),
        contains("Could not find a CREATE processor"), eq(CASE_ID), eq(ROUTING_FAILED), any(String[].class));
  }

  @Test
  void resolveExactlyOne_multipleMatchingProcessors_throwsAndTriggersEvent() {
    ActionInstruction request = buildRequest();
    ProcessorRouter<ActionInstruction> router = new ProcessorRouter<>(
        Map.of(KEY, List.of(processorA, processorB)), eventManager, "CREATE", JobService.class);
    GatewayCaseRecord cache = GatewayCaseRecord.builder().caseId(CASE_ID).delivered(true).build();

    when(processorA.isValid(request, cache)).thenReturn(true);
    when(processorB.isValid(request, cache)).thenReturn(true);

    GatewayException exception = assertThrows(GatewayException.class,
        () -> router.resolveExactlyOne(KEY, request, cache));

    assertEquals(GatewayException.Fault.VALIDATION_FAILED, exception.getFault());
    assertEquals("Found multiple CREATE processors for request from RM", exception.getMessage());
    verify(eventManager).triggerErrorEvent(eq(JobService.class),
        contains("Found multiple CREATE processors"), eq(CASE_ID), eq(ROUTING_FAILED), any(String[].class));
  }

  @Test
  void resolveOptional_returnsEmptyWhenNoMatchingProcessors() throws GatewayException {
    ActionInstruction request = buildRequest();
    ProcessorRouter<ActionInstruction> router = new ProcessorRouter<>(
        Map.of(KEY, List.of(processorA)), eventManager, "UPDATE", JobService.class);

    when(processorA.isValid(request, null)).thenReturn(false);

    Optional<InboundProcessor<ActionInstruction>> result = router.resolveOptional(KEY, request, null);

    assertTrue(result.isEmpty());
  }

  @Test
  void resolveOptional_returnsSingleProcessorWhenFound() throws GatewayException {
    ActionInstruction request = buildRequest();
    ProcessorRouter<ActionInstruction> router = new ProcessorRouter<>(
        Map.of(KEY, List.of(processorA)), eventManager, "UPDATE", JobService.class);

    when(processorA.isValid(request, null)).thenReturn(true);

    Optional<InboundProcessor<ActionInstruction>> result = router.resolveOptional(KEY, request, null);

    assertTrue(result.isPresent());
    assertSame(processorA, result.get());
  }

  @Test
  void resolveOptional_multipleMatchingProcessors_throwsAndTriggersEvent() {
    ActionInstruction request = buildRequest();
    ProcessorRouter<ActionInstruction> router = new ProcessorRouter<>(
        Map.of(KEY, List.of(processorA, processorB)), eventManager, "UPDATE", JobService.class);

    when(processorA.isValid(request, null)).thenReturn(true);
    when(processorB.isValid(request, null)).thenReturn(true);

    GatewayException exception = assertThrows(GatewayException.class,
        () -> router.resolveOptional(KEY, request, null));

    assertEquals(GatewayException.Fault.VALIDATION_FAILED, exception.getFault());
    assertEquals("Found multiple UPDATE processors for request from RM", exception.getMessage());
    verify(eventManager).triggerErrorEvent(eq(JobService.class),
        contains("Found multiple UPDATE processors"), eq(CASE_ID), eq(ROUTING_FAILED), any(String[].class));
  }

  @Test
  void fromProcessors_groupsProcessorsByKey() throws GatewayException {
    ActionInstruction request = buildRequest();
    when(processorA.getKey()).thenReturn(KEY);
    when(processorB.getKey()).thenReturn(KEY);
    when(processorA.isValid(request, null)).thenReturn(false);
    when(processorB.isValid(request, null)).thenReturn(true);

    ProcessorRouter<ActionInstruction> router = ProcessorRouter.fromProcessors(
        List.of(processorA, processorB), eventManager, "CREATE", JobService.class);

    InboundProcessor<ActionInstruction> result = router.resolveExactlyOne(KEY, request, null);

    assertSame(processorB, result);
  }

  @Test
  void fromProcessors_handlesEmptyProcessorList() throws GatewayException {
    ActionInstruction request = buildRequest();
    ProcessorRouter<ActionInstruction> router = ProcessorRouter.fromProcessors(
        Collections.<InboundProcessor<ActionInstruction>>emptyList(), eventManager, "PAUSE", JobService.class);

    Optional<InboundProcessor<ActionInstruction>> result = router.resolveOptional(KEY, request, null);

    assertTrue(result.isEmpty());
  }

  @Test
  void buildHHKey_withNoAddressLevel_appliesDefault() {
    ActionInstruction instruction = ActionInstruction.builder()
        .actionInstruction(ActionInstructionType.CREATE)
        .surveyName("CENSUS")
        .addressType("HH")
        .build();

    ProcessorKey key = ProcessorKey.buildKey(instruction);

    assertEquals("U", key.getAddressLevel());
  }

  @Test
  void buildCEKey_withAddressLevel_keepsProvidedAddressLevel() {
    ActionInstruction instruction = ActionInstruction.builder()
        .actionInstruction(ActionInstructionType.CREATE)
        .surveyName("CENSUS")
        .addressType("CE")
        .addressLevel("E")
        .build();

    ProcessorKey key = ProcessorKey.buildKey(instruction);

    assertEquals("E", key.getAddressLevel());
  }

  @Test
  void buildNonCREATEKey_withNoAddressLevel_keepsNullAddressLevel() {
    ActionInstruction instruction = ActionInstruction.builder()
        .actionInstruction(ActionInstructionType.SWITCH_CE_TYPE)
        .surveyName("CENSUS")
        .addressType("CE")
        .build();

    ProcessorKey key = ProcessorKey.buildKey(instruction);

    assertNull(key.getAddressLevel());
  }

  private ActionInstruction buildRequest() {
    ActionInstruction request = new ActionInstruction();
    request.setActionInstruction(ActionInstructionType.CREATE);
    request.setSurveyName("CENSUS");
    request.setAddressType("HH");
    request.setCaseId(CASE_ID);
    return request;
  }
}


