package uk.gov.ons.census.fwmt.jobservice.service.routing.nc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.ons.census.fwmt.common.data.tm.CaseRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.helper.NcActionInstructionBuilder;
import uk.gov.ons.census.fwmt.jobservice.http.comet.CometRestClient;
import uk.gov.ons.census.fwmt.jobservice.service.converter.nc.NcCreateConverter;


@ExtendWith(MockitoExtension.class)
public class NcCreateConverterTest {

  @InjectMocks
  private NcCreateConverter ncCreateConverter;

  @Mock
  private CometRestClient cometRestClient;

  @Mock
  private GatewayCache gatewayCache;

  @Test
  @DisplayName("Should retrieve care codes and special instructions from old record")
  public void shouldRetrieveOldCareCodesAndSpecialInstructions() throws GatewayException {
    final FwmtActionInstruction ncInstruction = new NcActionInstructionBuilder().createNcActionInstruction();
    GatewayCache gatewayCache = GatewayCache.builder()
        .caseId("ac623e62-4f4b-11eb-ae93-0242ac130002").careCodes("Mind dog").accessInfo("1234").build();
    CaseRequest caseRequest = NcCreateConverter.convertHhNcEnglandAndWales(ncInstruction, null, "", gatewayCache);
    String expectedSpecialInstructions = gatewayCache.careCodes + "\n" + gatewayCache.accessInfo + "\n";
    Assertions.assertEquals(gatewayCache.careCodes + "\n", caseRequest.getDescription());
    Assertions.assertEquals(expectedSpecialInstructions, caseRequest.getSpecialInstructions());
  }

  @Test
  @DisplayName("Should send estabType, coordCode, location, uaa and blankFormReturned")
  public void shouldSendEstabTypeCoordCodeLoaction() throws GatewayException {
    final FwmtActionInstruction ncInstruction = new NcActionInstructionBuilder().createNcActionInstruction();
    CaseRequest caseRequest = NcCreateConverter.convertHhNcEnglandAndWales(ncInstruction, null, "", gatewayCache);
    Assertions.assertEquals(ncInstruction.getEstabType(), caseRequest.getEstabType());
    Assertions.assertEquals(ncInstruction.getFieldCoordinatorId(), caseRequest.getCoordCode());
    Assertions.assertEquals(ncInstruction.getLatitude(), caseRequest.getLocation().getLat());
    Assertions.assertEquals(ncInstruction.getLongitude(), caseRequest.getLocation().get_long());
    Assertions.assertEquals(ncInstruction.isUndeliveredAsAddress(), caseRequest.isUaa());
    Assertions.assertEquals(ncInstruction.isBlankFormReturned(), caseRequest.isBlankFormReturned());
  }
}