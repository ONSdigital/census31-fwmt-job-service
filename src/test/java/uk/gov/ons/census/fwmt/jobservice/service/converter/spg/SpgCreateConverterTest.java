package uk.gov.ons.census.fwmt.jobservice.service.converter.spg;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.gov.ons.census.fwmt.common.data.tm.CaseRequest;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.helper.SpgActionInstructionBuilder;

class SpgCreateConverterTest {
    @Mock
    private GatewayCache gatewayCache;

    @Test
    @DisplayName("Should include careCodes to in description for SPG Site")
    public void shouldIncludeCareCodesInDescriptionForSpgSite() {
        final FwmtActionInstruction actionInstruction = new SpgActionInstructionBuilder().createSpgSite();
        GatewayCache gatewayCache = GatewayCache.builder()
            .caseId("ac623e62-4f4b-11eb-ae93-0242ac130002").careCodes("Mind dog").accessInfo("1234").build();
        CaseRequest spgSite = SpgCreateConverter.convertSite(actionInstruction, gatewayCache);
        String expectedSpecialInstruction = gatewayCache.accessInfo + "\n" + gatewayCache.careCodes + "\n";
        String actualSpecialInstruction = spgSite.getSpecialInstructions();
        Assertions.assertEquals(expectedSpecialInstruction, actualSpecialInstruction);
    }

    @Test
    @DisplayName("Should include careCodes to in description for SPG Unit-F")
    public void shouldIncludeCareCodesInDescriptionForSpgUnitFollowup() {
        final FwmtActionInstruction actionInstruction = new SpgActionInstructionBuilder().createSpgUnitFollowup();
        GatewayCache gatewayCache = GatewayCache.builder()
            .caseId("ac623e62-4f4b-11eb-ae93-0242ac130002").careCodes("Mind dog").accessInfo("1234").build();
        CaseRequest spgSite = SpgCreateConverter.convertSite(actionInstruction, gatewayCache);
        String expectedSpecialInstruction = gatewayCache.accessInfo + "\n" + gatewayCache.careCodes + "\n";
        String actualSpecialInstruction = spgSite.getSpecialInstructions();
        Assertions.assertEquals(expectedSpecialInstruction, actualSpecialInstruction);
    }

}