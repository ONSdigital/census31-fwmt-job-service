package uk.gov.ons.census.fwmt.jobservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction;
import uk.gov.ons.census.fwmt.jobservice.service.processor.InboundProcessor;
import uk.gov.ons.census.fwmt.jobservice.service.processor.ProcessorRouter;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentMatchers;

@ExtendWith(MockitoExtension.class)
class JobServiceTransitionBaselineTest {

  @InjectMocks
  private UpdateActionOrchestrator updateActionOrchestrator;

  @InjectMocks
  private CancelActionOrchestrator cancelActionOrchestrator;

  @Mock private GatewayCaseRecordService cacheService;
  @Mock private TmDispatchService tmDispatchService;

  @Mock private ProcessorRouter<FwmtActionInstruction> updateRouter;
  @Mock private ProcessorRouter<FwmtCancelActionInstruction> cancelRouter;

  @Test
  void shouldDispatchWithNullHandlerForUpdateWhenNoCache() throws GatewayException {
    FwmtActionInstruction request = buildUpdateRequest();
    Instant messageTime = Instant.now();

    when(cacheService.getById(request.getCaseId())).thenReturn(null);
    when(updateRouter.resolveOptional(any(), any(), isNull())).thenReturn(Optional.empty());

    updateActionOrchestrator.process(request, messageTime);

    verify(tmDispatchService).dispatch(any(FwmtActionInstruction.class),
        ArgumentMatchers.<InboundProcessor<FwmtActionInstruction>>isNull(), isNull(), eq(messageTime));
  }

  @Test
  void shouldDispatchWithNullHandlerForCancelWhenNoCache() throws GatewayException {
    FwmtCancelActionInstruction request = buildCancelRequest();
    Instant messageTime = Instant.now();

    when(cacheService.getByOriginalCaseId(request.getCaseId())).thenReturn(null);
    when(cacheService.getById(request.getCaseId())).thenReturn(null);
    when(cancelRouter.resolveOptional(any(), any(), isNull())).thenReturn(Optional.empty());

    cancelActionOrchestrator.process(request, messageTime);

    verify(tmDispatchService).dispatch(any(FwmtCancelActionInstruction.class),
        ArgumentMatchers.<InboundProcessor<FwmtCancelActionInstruction>>isNull(), isNull(), eq(messageTime));
  }

  private FwmtActionInstruction buildUpdateRequest() {
    FwmtActionInstruction request = new FwmtActionInstruction();
    request.setActionInstruction(ActionInstructionType.UPDATE);
    request.setCaseId("ac623e62-4f4b-11eb-ae93-0242ac130002");
    request.setAddressType("CE");
    request.setAddressLevel("E");
    request.setSurveyName("CENSUS");
    return request;
  }

  private FwmtCancelActionInstruction buildCancelRequest() {
    FwmtCancelActionInstruction request = new FwmtCancelActionInstruction();
    request.setActionInstruction(ActionInstructionType.CANCEL);
    request.setCaseId("ac623e62-4f4b-11eb-ae93-0242ac130002");
    request.setAddressType("CE");
    request.setAddressLevel("E");
    return request;
  }
}
