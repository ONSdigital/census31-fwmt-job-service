package uk.gov.ons.census.fwmt.jobservice.service.converter.spg;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.gov.ons.census.fwmt.common.data.modelcase.Address;
import uk.gov.ons.census.fwmt.common.data.modelcase.CaseCreateRequest;
import uk.gov.ons.census.fwmt.common.data.modelcase.CaseType;
import uk.gov.ons.census.fwmt.common.data.modelcase.CeCaseExtension;
import uk.gov.ons.census.fwmt.common.data.modelcase.Contact;
import uk.gov.ons.census.fwmt.common.data.modelcase.Geography;
import uk.gov.ons.census.fwmt.common.data.modelcase.Location;
import uk.gov.ons.census.fwmt.common.data.modelcase.SurveyType;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.http.comet.CometRestClient;
import uk.gov.ons.census.fwmt.jobservice.service.GatewayCacheService;
import uk.gov.ons.census.fwmt.jobservice.service.SpgFollowUpSchedulingService;
import uk.gov.ons.census.fwmt.jobservice.service.routing.RoutingValidator;
import uk.gov.ons.census.fwmt.jobservice.service.routing.spg.SpgCancelRouter;
import uk.gov.ons.census.fwmt.jobservice.service.routing.spg.SpgCreateRouter;
import uk.gov.ons.census.fwmt.jobservice.service.routing.spg.SpgUpdateRouter;
import uk.gov.ons.census.fwmt.jobservice.spg.SpgRequestBuilder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpgSiteConverterTest {
  // We use this entry to test routing, starting from the top level
  private final SpgCreateRouter router;

  public SpgSiteConverterTest() {
    // mocks
    CometRestClient cometRestClient = Mockito.mock(CometRestClient.class);
    GatewayCacheService gatewayCacheService = Mockito.mock(GatewayCacheService.class);
    SpgFollowUpSchedulingService spgFollowUpSchedulingService = Mockito.mock(SpgFollowUpSchedulingService.class);
    GatewayEventManager eventManager = Mockito.mock(GatewayEventManager.class);

    // routing equipment
    RoutingValidator routingValidator = new RoutingValidator(eventManager);
    SpgCreateRouter createRouter = new SpgCreateRouter(routingValidator, cometRestClient, eventManager,
        gatewayCacheService, spgFollowUpSchedulingService);
    //SpgUpdateRouter updateRouter = new SpgUpdateRouter(routingValidator, cometRestClient, eventManager, createRouter);
    //SpgCancelRouter cancelRouter = new SpgCancelRouter(routingValidator, cometRestClient, eventManager);

    // core routers
    router = createRouter;
  }

  @Test
  void confirm_valid_spgRequest_can_be_converted() {
    FwmtActionInstruction ffu = SpgRequestBuilder.makeSite();
    GatewayCache cache = GatewayCache.builder().build();
    assertTrue(router.isValid(ffu, cache));
  }

  @Test
  void confirm_spgRequest_with_nulls_returns_false() {
    FwmtActionInstruction ffu = SpgRequestBuilder.makeSite();
    ffu.setActionInstruction(null);
    ffu.setSurveyName(null);
    ffu.setAddressType(null);
    ffu.setAddressLevel(null);
    ffu.setSecureEstablishment(false);
    GatewayCache cache = GatewayCache.builder().build();
    assertFalse(router.isValid(ffu, cache));
  }

  @Test
  void confirm_spgRequest_with_invalid_actionInstruction_returns_false() {
    FwmtActionInstruction ffu = SpgRequestBuilder.makeSite();
    ffu.setActionInstruction(null);
    GatewayCache cache = GatewayCache.builder().build();
    assertFalse(router.isValid(ffu, cache));
  }

  @Test
  void confirm_spgRequest_with_invalid_surveyName_returns_false() {
    FwmtActionInstruction ffu = SpgRequestBuilder.makeSite();
    ffu.setSurveyName("nonsense");
    GatewayCache cache = GatewayCache.builder().build();
    assertFalse(router.isValid(ffu, cache));
  }

  @Test
  void confirm_spgRequest_with_invalid_addressType_returns_false() {
    FwmtActionInstruction ffu = SpgRequestBuilder.makeSite();
    ffu.setAddressType("nonsense");
    GatewayCache cache = GatewayCache.builder().build();
    assertFalse(router.isValid(ffu, cache));
  }

  @Test
  void confirm_spgRequest_with_invalid_addressLevel_returns_false() {
    FwmtActionInstruction ffu = SpgRequestBuilder.makeSite();
    ffu.setAddressLevel("nonsense");
    GatewayCache cache = GatewayCache.builder().build();
    assertFalse(router.isValid(ffu, cache));
  }

  @Test
  void confirm_valid_spgRequest_creates_valid_TM_request() throws GatewayException {
    FwmtActionInstruction ffu = SpgRequestBuilder.makeSite();
    GatewayCache cache = GatewayCache.builder().build();

    CaseCreateRequest actualTmRequest = SpgCreateConverter.convertSite(ffu, cache);

    Contact contact = Contact.builder()
        //.name("Mx exampleForename exampleSurname")
        .organisationName("exampleOrgName")
        //.phone("examplePhoneNumber")
        //.email(null)
        .build();

    Address address = Address.builder()
        .lines(List.of("exampleAddr1", "exampleAddr2", "exampleAddr3"))
        .town("exampleTown")
        .postcode("examplePostcode")
        .geography(Geography.builder().oa("exampleOa").build())
        .build();

    Location location = Location.builder().lat(2.0f)._long(3.0f).build();

    CaseCreateRequest expectedTMRequest = CaseCreateRequest.builder()
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
