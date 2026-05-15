package uk.gov.ons.census.fwmt.jobservice.service.routing.hh;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class HHCancelledPauseTest {

  @InjectMocks
  private HHCancelledPause hhCancelledPause;

  @Test
  void shouldCheckisValid() {
    final FwmtActionInstruction request = FwmtActionInstruction.builder()
        .actionInstruction(ActionInstructionType.PAUSE)
        .surveyName("CENSUS")
        .addressType("HH")
        .addressLevel("U")
        .build();

    final GatewayCache cache = GatewayCache.builder()
        .existsInFwmt(true)
        .lastActionInstruction("CANCEL")
        .build();
    assertTrue(hhCancelledPause.isValid(request, cache));
    request.setAddressLevel("");
    assertFalse(hhCancelledPause.isValid(request, cache), "Shouldn't pass as address level not set.");
    request.setAddressLevel("U");
    assertFalse(hhCancelledPause.isValid(request, null),"No cache set should return false");
    request.setActionInstruction(ActionInstructionType.CREATE);
    assertFalse(hhCancelledPause.isValid(request, cache));

  }
}