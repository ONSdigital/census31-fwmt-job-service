package uk.gov.ons.census.fwmt.jobservice.service.routing.ce;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.helper.FwmtCreateJobRequestBuilder;
import uk.gov.ons.census.fwmt.jobservice.service.GatewayCacheService;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CeCreateProcessorTest {

  @InjectMocks
  private CeCreateEstabDeliverProcessor ceCreateEstabDeliverProcessor;

  @InjectMocks
  private CeCreateEstabFollowupProcessor ceCreateEstabFollowupProcessor;

  @InjectMocks
  private CeCreateSiteProcessor ceCreateSiteProcessor;

  @Mock
  private GatewayCacheService cacheService;

  @Test
  @DisplayName("Should not select CE Estab Deliver Create processor")
  public void shouldNotSelectCeEstabDeliverCreateProcessor() {
    final FwmtActionInstruction instruction = new FwmtCreateJobRequestBuilder().createCeEstabDeliver();
    GatewayCache gatewayCache = GatewayCache.builder()
        .caseId("ac623e62-4f4b-11eb-ae93-0242ac130002").estabUprn("4321").existsInFwmt(false).type(3).lastActionInstruction("CREATE").build();
    when(cacheService.doesEstabUprnAndTypeExist(instruction.getUprn(), 3)).thenReturn(true);
    Assertions.assertFalse(ceCreateEstabDeliverProcessor.isValid(instruction, gatewayCache));
  }

  @Test
  @DisplayName("Should select CE Estab Deliver Create processor")
  public void shouldSelectCeEstabDeliverCreateProcessor() {
    final FwmtActionInstruction instruction = new FwmtCreateJobRequestBuilder().createCeEstabDeliver();
    GatewayCache gatewayCache = GatewayCache.builder()
        .caseId("ac623e62-4f4b-11eb-ae93-0242ac130002").estabUprn("4321").existsInFwmt(false).type(1).lastActionInstruction("CREATE").build();
    Assertions.assertTrue(ceCreateEstabDeliverProcessor.isValid(instruction, gatewayCache));
  }

  @Test
  @DisplayName("Should not select CE Estab Followup Create processor")
  public void shouldNotSelectCeEstabFollowupCreateProcessor() {
    final FwmtActionInstruction instruction = new FwmtCreateJobRequestBuilder().createCeEstabFollowup();
    GatewayCache gatewayCache = GatewayCache.builder()
        .caseId("ac623e62-4f4b-11eb-ae93-0242ac130002").estabUprn("4321").existsInFwmt(true).type(3).lastActionInstruction("CREATE").build();
    when(cacheService.doesEstabUprnAndTypeExist(instruction.getUprn(), 3)).thenReturn(true);
    Assertions.assertFalse(ceCreateEstabFollowupProcessor.isValid(instruction, gatewayCache));
  }

  @Test
  @DisplayName("Should select CE Estab Followup Create processor")
  public void shouldSelectCeEstabFollowupCreateProcessor() {
    final FwmtActionInstruction instruction = new FwmtCreateJobRequestBuilder().createCeEstabFollowup();
    GatewayCache gatewayCache = GatewayCache.builder()
        .caseId("ac623e62-4f4b-11eb-ae93-0242ac130002").estabUprn("4321").existsInFwmt(true).type(1).lastActionInstruction("CREATE").build();
    Assertions.assertTrue(ceCreateEstabFollowupProcessor.isValid(instruction, gatewayCache));
  }

  @Test
  @DisplayName("Should not select CE Site Create processor")
  public void shouldNotSelectCeSiteCreateProcessor() {
    final FwmtActionInstruction instruction = new FwmtCreateJobRequestBuilder().createCeSite();
    GatewayCache gatewayCache = GatewayCache.builder()
        .caseId("ac623e62-4f4b-11eb-ae93-0242ac130002").estabUprn("1234").existsInFwmt(false).type(10).lastActionInstruction("CREATE").build();
    Assertions.assertFalse(ceCreateSiteProcessor.isValid(instruction, gatewayCache));
  }

  @Test
  @DisplayName("Should select CE Site Create processor")
  public void shouldSelectCeSiteCreateProcessor() {
    final FwmtActionInstruction instruction = new FwmtCreateJobRequestBuilder().createCeSite();
    GatewayCache gatewayCache = GatewayCache.builder()
        .caseId("ac623e62-4f4b-11eb-ae93-0242ac130002").estabUprn("1234").existsInFwmt(false).type(3).lastActionInstruction("CREATE").build();
    when(cacheService.doesEstabUprnAndTypeExist(instruction.getUprn(), 3)).thenReturn(true);
    Assertions.assertTrue(ceCreateSiteProcessor.isValid(instruction, gatewayCache));
  }
}