package uk.gov.ons.census.fwmt.jobservice.service.converter.ce;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.gov.ons.census.fwmt.common.data.tm.CaseRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.dto.rm.ActionInstruction;
import uk.gov.ons.census.fwmt.common.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.ce.CeRequestBuilder;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCaseRecord;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.ons.census.fwmt.common.data.tm.SurveyType.CE_EST;
import static uk.gov.ons.census.fwmt.common.data.tm.SurveyType.CE_ESTWU;

class CeCreateConverterTest {

  private GatewayCaseRecord cache;
  private GatewayCaseRecord cacheWithNoCareCodes;
  private GatewayCaseRecord cacheWithNoAccessInfo;

  public CeCreateConverterTest() {
    cache = GatewayCaseRecord.builder()
        .careCodes("careCode1")
        .accessInfo("this is access info")
        .build();
    cacheWithNoCareCodes = GatewayCaseRecord.builder()
        .accessInfo("this is access info")
        .build();
    cacheWithNoAccessInfo = GatewayCaseRecord.builder()
        .careCodes("careCode1")
        .build();
  }

  @Test
  public void test_convertCeEstabDeliver() {
    ActionInstruction actionInstruction = CeRequestBuilder.makeSite();
    CaseRequest cr = CeCreateConverter.convertCeEstabDeliver(actionInstruction, cache);
    System.out.println(cr);
    assertEquals("careCode1\n", cr.getDescription());
    assertEquals("careCode1\n" +
        "this is access info", cr.getSpecialInstructions());
  }

  @Test
  public void test_convertCeEstabDeliverSecure() {
    ActionInstruction actionInstruction = CeRequestBuilder.makeSite();
    CaseRequest cr = CeCreateConverter.convertCeEstabDeliverSecure(actionInstruction, cache);
    System.out.println(cr);
    assertEquals("careCode1\n" + "Secure Establishment", cr.getDescription());
    assertEquals("careCode1\n" +
        "this is access info", cr.getSpecialInstructions());
  }

  @Test
  public void test_convertCeEstabDeliverSecure_noCareCodes() {
    ActionInstruction actionInstruction = CeRequestBuilder.makeSite();
    CaseRequest cr = CeCreateConverter.convertCeEstabDeliverSecure(actionInstruction, cacheWithNoCareCodes);
    System.out.println(cr);
    assertEquals("Secure Establishment", cr.getDescription());
    assertEquals("this is access info", cr.getSpecialInstructions());
  }

  @Test
  public void test_convertCeEstabDeliverSecure_noAccessInfo() {
    ActionInstruction actionInstruction = CeRequestBuilder.makeSite();
    CaseRequest cr = CeCreateConverter.convertCeEstabDeliverSecure(actionInstruction, cacheWithNoAccessInfo);
    System.out.println(cr);
    assertEquals("careCode1\n" + "Secure Establishment", cr.getDescription());
    assertEquals("careCode1\n", cr.getSpecialInstructions());
  }

  @Test
  public void test_convertCeEstabDeliverSecure_noCache() {
    ActionInstruction actionInstruction = CeRequestBuilder.makeSite();
    CaseRequest cr = CeCreateConverter.convertCeEstabDeliverSecure(actionInstruction,
        GatewayCaseRecord.builder().build());
    System.out.println(cr);
    assertEquals("Secure Establishment", cr.getDescription());
    assertEquals("", cr.getSpecialInstructions());
  }

  @Test
  public void test_convertCeEstabDeliver_noCareCodes() {
    ActionInstruction actionInstruction = CeRequestBuilder.makeSite();
    CaseRequest cr = CeCreateConverter.convertCeEstabDeliver(actionInstruction, cacheWithNoCareCodes);
    System.out.println(cr);
    assertEquals("", cr.getDescription());
    assertEquals("this is access info", cr.getSpecialInstructions());
  }

  @Test
  public void test_convertCeEstabDeliver_noAccessInfo() {
    ActionInstruction actionInstruction = CeRequestBuilder.makeSite();
    CaseRequest cr = CeCreateConverter.convertCeEstabDeliver(actionInstruction, cacheWithNoAccessInfo);
    System.out.println(cr);
    assertEquals("careCode1\n", cr.getDescription());
    assertEquals("careCode1\n", cr.getSpecialInstructions());
  }

  @Test
  public void test_convertCeEstabDeliver_noCache() {
    ActionInstruction actionInstruction = CeRequestBuilder.makeSite();
    CaseRequest cr = CeCreateConverter.convertCeEstabDeliver(actionInstruction,
        GatewayCaseRecord.builder().build());
    System.out.println(cr);
    assertEquals("", cr.getDescription());
    assertEquals("", cr.getSpecialInstructions());
  }


  @Test
  public void test_convertCeEstabFollowup() {
    ActionInstruction actionInstruction = CeRequestBuilder.makeSite();
    actionInstruction.setCeExpectedCapacity(100);
    CaseRequest cr = CeCreateConverter.convertCeEstabFollowup(actionInstruction, cache);
    System.out.println(cr);
    assertEquals("careCode1\n", cr.getDescription());
    assertEquals("careCode1\n" +
        "this is access info", cr.getSpecialInstructions());
    assertEquals(CE_EST, cr.getSurveyType());
  }

  @Test
  public void test_convertCeEstabFollowupSecure() {
    ActionInstruction actionInstruction = CeRequestBuilder.makeSite();
    actionInstruction.setCeExpectedCapacity(100);
    CaseRequest cr = CeCreateConverter.convertCeEstabFollowupSecure(actionInstruction, cache);
    assertEquals("careCode1\n" + "Secure Establishment", cr.getDescription());
    assertEquals("careCode1\n" +
        "this is access info", cr.getSpecialInstructions());
    assertEquals(CE_EST, cr.getSurveyType());
  }

  @Test
  public void test_convertCeEstabFollowupWithoutUnits() {
    ActionInstruction actionInstruction = CeRequestBuilder.makeSite();
    actionInstruction.setCeExpectedCapacity(0);
    CaseRequest cr = CeCreateConverter.convertCeEstabFollowup(actionInstruction, cache);
    assertEquals("careCode1\n", cr.getDescription());
    assertEquals("careCode1\n" +
        "this is access info", cr.getSpecialInstructions());
    assertEquals(CE_ESTWU, cr.getSurveyType());
  }

  @Test
  public void test_convertCeEstabFollowupSecureWithoutUnits() {
    ActionInstruction actionInstruction = CeRequestBuilder.makeSite();
    actionInstruction.setCeExpectedCapacity(0);
    CaseRequest cr = CeCreateConverter.convertCeEstabFollowupSecure(actionInstruction, cache);
    System.out.println(cr);
    assertEquals("careCode1\n" + "Secure Establishment", cr.getDescription());
    assertEquals("careCode1\n" +
        "this is access info", cr.getSpecialInstructions());
    assertEquals(CE_ESTWU, cr.getSurveyType());
  }

  @Test
  public void test_convertCeSite() {
    ActionInstruction actionInstruction = CeRequestBuilder.makeSite();
    CaseRequest cr = CeCreateConverter.convertCeSite(actionInstruction, cache);
    System.out.println(cr);
    assertEquals("careCode1\n", cr.getDescription());
    assertEquals("careCode1\n" +
        "this is access info", cr.getSpecialInstructions());
  }

  @Test
  public void test_convertCeSiteSecure() {
    ActionInstruction actionInstruction = CeRequestBuilder.makeSite();
    CaseRequest cr = CeCreateConverter.convertCeSiteSecure(actionInstruction, cache);
    System.out.println(cr);
    assertEquals("careCode1\n" + "Secure Site", cr.getDescription());
    assertEquals("careCode1\n" +
        "this is access info", cr.getSpecialInstructions());
  }

  @Test
  public void test_convertCeUnitDeliver() {
    ActionInstruction actionInstruction = CeRequestBuilder.makeSite();
    CaseRequest cr = CeCreateConverter.convertCeUnitDeliver(actionInstruction, cache);
    System.out.println(cr);
    assertEquals("careCode1\n", cr.getDescription());
    assertEquals("careCode1\n" +
        "this is access info", cr.getSpecialInstructions());
  }

  @Test
  public void test_convertCeUnitDeliverSecure() {
    ActionInstruction actionInstruction = CeRequestBuilder.makeSite();
    CaseRequest cr = CeCreateConverter.convertCeUnitDeliverSecure(actionInstruction, cache);
    System.out.println(cr);
    assertEquals("careCode1\n" + "Secure Unit", cr.getDescription());
    assertEquals("careCode1\n" +
        "this is access info", cr.getSpecialInstructions());
  }

  @Test
  public void test_convertCeUnitFollowup() {
    ActionInstruction actionInstruction = CeRequestBuilder.makeSite();
    CaseRequest cr = CeCreateConverter.convertCeUnitFollowup(actionInstruction, cache);
    System.out.println(cr);
    assertEquals("careCode1\n", cr.getDescription());
    assertEquals("careCode1\n" +
        "this is access info", cr.getSpecialInstructions());
  }

  @Test
  public void test_convertCeUnitFollowupSecure() {
    ActionInstruction actionInstruction = CeRequestBuilder.makeSite();
    CaseRequest cr = CeCreateConverter.convertCeUnitFollowupSecure(actionInstruction, cache);
    System.out.println(cr);
    assertEquals("careCode1\n" + "Secure Unit", cr.getDescription());
    assertEquals("careCode1\n" +
        "this is access info", cr.getSpecialInstructions());
  }


}
