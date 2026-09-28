package uk.gov.ons.census.fwmt.jobservice.service.converter.ce;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.gov.ons.census.fwmt.common.data.tm.CaseRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstruction;
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
    ActionInstruction ffu = CeRequestBuilder.makeSite();
    CaseRequest cr = CeCreateConverter.convertCeEstabDeliver(ffu, cache);
    System.out.println(cr);
    assertEquals("careCode1\n", cr.getDescription());
    assertEquals("careCode1\n" +
        "this is access info", cr.getSpecialInstructions());
  }

  @Test
  public void test_convertCeEstabDeliverSecure() {
    ActionInstruction ffu = CeRequestBuilder.makeSite();
    CaseRequest cr = CeCreateConverter.convertCeEstabDeliverSecure(ffu, cache);
    System.out.println(cr);
    assertEquals("careCode1\n" + "Secure Establishment", cr.getDescription());
    assertEquals("careCode1\n" +
        "this is access info", cr.getSpecialInstructions());
  }

  @Test
  public void test_convertCeEstabDeliverSecure_noCareCodes() {
    ActionInstruction ffu = CeRequestBuilder.makeSite();
    CaseRequest cr = CeCreateConverter.convertCeEstabDeliverSecure(ffu, cacheWithNoCareCodes);
    System.out.println(cr);
    assertEquals("Secure Establishment", cr.getDescription());
    assertEquals("this is access info", cr.getSpecialInstructions());
  }

  @Test
  public void test_convertCeEstabDeliverSecure_noAccessInfo() {
    ActionInstruction ffu = CeRequestBuilder.makeSite();
    CaseRequest cr = CeCreateConverter.convertCeEstabDeliverSecure(ffu, cacheWithNoAccessInfo);
    System.out.println(cr);
    assertEquals("careCode1\n" + "Secure Establishment", cr.getDescription());
    assertEquals("careCode1\n", cr.getSpecialInstructions());
  }

  @Test
  public void test_convertCeEstabDeliverSecure_noCache() {
    ActionInstruction ffu = CeRequestBuilder.makeSite();
    CaseRequest cr = CeCreateConverter.convertCeEstabDeliverSecure(ffu,
        GatewayCaseRecord.builder().build());
    System.out.println(cr);
    assertEquals("Secure Establishment", cr.getDescription());
    assertEquals("", cr.getSpecialInstructions());
  }

  @Test
  public void test_convertCeEstabDeliver_noCareCodes() {
    ActionInstruction ffu = CeRequestBuilder.makeSite();
    CaseRequest cr = CeCreateConverter.convertCeEstabDeliver(ffu, cacheWithNoCareCodes);
    System.out.println(cr);
    assertEquals("", cr.getDescription());
    assertEquals("this is access info", cr.getSpecialInstructions());
  }

  @Test
  public void test_convertCeEstabDeliver_noAccessInfo() {
    ActionInstruction ffu = CeRequestBuilder.makeSite();
    CaseRequest cr = CeCreateConverter.convertCeEstabDeliver(ffu, cacheWithNoAccessInfo);
    System.out.println(cr);
    assertEquals("careCode1\n", cr.getDescription());
    assertEquals("careCode1\n", cr.getSpecialInstructions());
  }

  @Test
  public void test_convertCeEstabDeliver_noCache() {
    ActionInstruction ffu = CeRequestBuilder.makeSite();
    CaseRequest cr = CeCreateConverter.convertCeEstabDeliver(ffu,
        GatewayCaseRecord.builder().build());
    System.out.println(cr);
    assertEquals("", cr.getDescription());
    assertEquals("", cr.getSpecialInstructions());
  }


  @Test
  public void test_convertCeEstabFollowup() {
    ActionInstruction ffu = CeRequestBuilder.makeSite();
    ffu.setCeExpectedCapacity(100);
    CaseRequest cr = CeCreateConverter.convertCeEstabFollowup(ffu, cache);
    System.out.println(cr);
    assertEquals("careCode1\n", cr.getDescription());
    assertEquals("careCode1\n" +
        "this is access info", cr.getSpecialInstructions());
    assertEquals(CE_EST, cr.getSurveyType());
  }

  @Test
  public void test_convertCeEstabFollowupSecure() {
    ActionInstruction ffu = CeRequestBuilder.makeSite();
    ffu.setCeExpectedCapacity(100);
    CaseRequest cr = CeCreateConverter.convertCeEstabFollowupSecure(ffu, cache);
    assertEquals("careCode1\n" + "Secure Establishment", cr.getDescription());
    assertEquals("careCode1\n" +
        "this is access info", cr.getSpecialInstructions());
    assertEquals(CE_EST, cr.getSurveyType());
  }

  @Test
  public void test_convertCeEstabFollowupWithoutUnits() {
    ActionInstruction ffu = CeRequestBuilder.makeSite();
    ffu.setCeExpectedCapacity(0);
    CaseRequest cr = CeCreateConverter.convertCeEstabFollowup(ffu, cache);
    assertEquals("careCode1\n", cr.getDescription());
    assertEquals("careCode1\n" +
        "this is access info", cr.getSpecialInstructions());
    assertEquals(CE_ESTWU, cr.getSurveyType());
  }

  @Test
  public void test_convertCeEstabFollowupSecureWithoutUnits() {
    ActionInstruction ffu = CeRequestBuilder.makeSite();
    ffu.setCeExpectedCapacity(0);
    CaseRequest cr = CeCreateConverter.convertCeEstabFollowupSecure(ffu, cache);
    System.out.println(cr);
    assertEquals("careCode1\n" + "Secure Establishment", cr.getDescription());
    assertEquals("careCode1\n" +
        "this is access info", cr.getSpecialInstructions());
    assertEquals(CE_ESTWU, cr.getSurveyType());
  }

  @Test
  public void test_convertCeSite() {
    ActionInstruction ffu = CeRequestBuilder.makeSite();
    CaseRequest cr = CeCreateConverter.convertCeSite(ffu, cache);
    System.out.println(cr);
    assertEquals("careCode1\n", cr.getDescription());
    assertEquals("careCode1\n" +
        "this is access info", cr.getSpecialInstructions());
  }

  @Test
  public void test_convertCeSiteSecure() {
    ActionInstruction ffu = CeRequestBuilder.makeSite();
    CaseRequest cr = CeCreateConverter.convertCeSiteSecure(ffu, cache);
    System.out.println(cr);
    assertEquals("careCode1\n" + "Secure Site", cr.getDescription());
    assertEquals("careCode1\n" +
        "this is access info", cr.getSpecialInstructions());
  }

  @Test
  public void test_convertCeUnitDeliver() {
    ActionInstruction ffu = CeRequestBuilder.makeSite();
    CaseRequest cr = CeCreateConverter.convertCeUnitDeliver(ffu, cache);
    System.out.println(cr);
    assertEquals("careCode1\n", cr.getDescription());
    assertEquals("careCode1\n" +
        "this is access info", cr.getSpecialInstructions());
  }

  @Test
  public void test_convertCeUnitDeliverSecure() {
    ActionInstruction ffu = CeRequestBuilder.makeSite();
    CaseRequest cr = CeCreateConverter.convertCeUnitDeliverSecure(ffu, cache);
    System.out.println(cr);
    assertEquals("careCode1\n" + "Secure Unit", cr.getDescription());
    assertEquals("careCode1\n" +
        "this is access info", cr.getSpecialInstructions());
  }

  @Test
  public void test_convertCeUnitFollowup() {
    ActionInstruction ffu = CeRequestBuilder.makeSite();
    CaseRequest cr = CeCreateConverter.convertCeUnitFollowup(ffu, cache);
    System.out.println(cr);
    assertEquals("careCode1\n", cr.getDescription());
    assertEquals("careCode1\n" +
        "this is access info", cr.getSpecialInstructions());
  }

  @Test
  public void test_convertCeUnitFollowupSecure() {
    ActionInstruction ffu = CeRequestBuilder.makeSite();
    CaseRequest cr = CeCreateConverter.convertCeUnitFollowupSecure(ffu, cache);
    System.out.println(cr);
    assertEquals("careCode1\n" + "Secure Unit", cr.getDescription());
    assertEquals("careCode1\n" +
        "this is access info", cr.getSpecialInstructions());
  }


}
