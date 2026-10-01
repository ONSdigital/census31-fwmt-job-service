package uk.gov.ons.census.fwmt.jobservice.service.converter.spg;

import org.junit.jupiter.api.Test;
import uk.gov.ons.census.fwmt.common.data.tm.Address;
import uk.gov.ons.census.fwmt.common.data.tm.CaseRequest;
import uk.gov.ons.census.fwmt.common.data.tm.CaseType;
import uk.gov.ons.census.fwmt.common.data.tm.CeCaseExtension;
import uk.gov.ons.census.fwmt.common.data.tm.Contact;
import uk.gov.ons.census.fwmt.common.data.tm.Geography;
import uk.gov.ons.census.fwmt.common.data.tm.Location;
import uk.gov.ons.census.fwmt.common.data.tm.SurveyType;
import uk.gov.ons.census.fwmt.common.dto.rm.ActionInstruction;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCaseRecord;
import uk.gov.ons.census.fwmt.jobservice.service.routing.spg.SpgCreateSiteProcessor;
import uk.gov.ons.census.fwmt.jobservice.spg.SpgRequestBuilder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpgSiteConverterTest {
  private final SpgCreateSiteProcessor router = new SpgCreateSiteProcessor();

  @Test
  void confirm_valid_spgRequest_can_be_converted() {
    ActionInstruction actionInstruction = SpgRequestBuilder.makeSite();
    GatewayCaseRecord cache = GatewayCaseRecord.builder().build();
    assertTrue(router.isValid(actionInstruction, cache));
  }

  @Test
  void confirm_spgRequest_with_nulls_returns_false() {
    ActionInstruction actionInstruction = SpgRequestBuilder.makeSite();
    actionInstruction.setActionInstruction(null);
    actionInstruction.setSurveyName(null);
    actionInstruction.setAddressType(null);
    actionInstruction.setAddressLevel(null);
    actionInstruction.setSecureEstablishment(false);
    GatewayCaseRecord cache = GatewayCaseRecord.builder().build();
    assertFalse(router.isValid(actionInstruction, cache));
  }

  @Test
  void confirm_spgRequest_with_invalid_actionInstruction_returns_false() {
    ActionInstruction actionInstruction = SpgRequestBuilder.makeSite();
    actionInstruction.setActionInstruction(null);
    GatewayCaseRecord cache = GatewayCaseRecord.builder().build();
    assertFalse(router.isValid(actionInstruction, cache));
  }

  @Test
  void confirm_spgRequest_with_invalid_surveyName_returns_false() {
    ActionInstruction actionInstruction = SpgRequestBuilder.makeSite();
    actionInstruction.setSurveyName("nonsense");
    GatewayCaseRecord cache = GatewayCaseRecord.builder().build();
    assertFalse(router.isValid(actionInstruction, cache));
  }

  @Test
  void confirm_spgRequest_with_invalid_addressType_returns_false() {
    ActionInstruction actionInstruction = SpgRequestBuilder.makeSite();
    actionInstruction.setAddressType("nonsense");
    GatewayCaseRecord cache = GatewayCaseRecord.builder().build();
    assertFalse(router.isValid(actionInstruction, cache));
  }

  @Test
  void confirm_spgRequest_with_invalid_addressLevel_returns_false() {
    ActionInstruction actionInstruction = SpgRequestBuilder.makeSite();
    actionInstruction.setAddressLevel("nonsense");
    GatewayCaseRecord cache = GatewayCaseRecord.builder().build();
    assertFalse(router.isValid(actionInstruction, cache));
  }

  @Test
  void confirm_valid_spgRequest_creates_valid_TM_request() {
    ActionInstruction actionInstruction = SpgRequestBuilder.makeSite();
    GatewayCaseRecord cache = GatewayCaseRecord.builder().build();

    CaseRequest actualTmRequest = SpgCreateConverter.convertSite(actionInstruction, cache);

    Contact contact = Contact.builder()
        .organisationName("exampleOrgName")
        .build();

    Address address = Address.builder()
        .lines(List.of("exampleAddr1", "exampleAddr2", "exampleAddr3"))
        .town("exampleTown")
        .postcode("examplePostcode")
        .geography(Geography.builder().oa("exampleOa").build())
        .build();

    Location location = Location.builder().lat(2d)._long(3d).build();

    CaseRequest expectedTMRequest = CaseRequest.builder()
        .reference("exampleCaseRef")
        .type(CaseType.CE)
        .surveyType(SurveyType.SPG_Site)
        .category("Not applicable")
        .estabType("exampleEstabType")
        .requiredOfficer("exampleOfficerId")
        .coordCode("exampleCoordinatorId")
        .contact(contact)
        .address(address)
        .location(location)
      .description("")
      .specialInstructions("")
        .ce(CeCaseExtension.builder()
            .ce1Complete(false)
            .deliveryRequired(false)
            .expectedResponses(0)
            .actualResponses(0)
            .build())
        .build();

    assertEquals(expectedTMRequest, actualTmRequest);
  }
}
