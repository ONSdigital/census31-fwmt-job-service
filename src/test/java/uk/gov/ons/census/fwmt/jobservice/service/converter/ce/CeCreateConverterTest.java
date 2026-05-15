package uk.gov.ons.census.fwmt.jobservice.service.converter.ce;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.gov.ons.census.fwmt.common.data.tm.CaseRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.ce.CeRequestBuilder;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CeCreateConverterTest {
    private GatewayCache cache;
    private GatewayCache cacheWithNoCareCodes;
    private GatewayCache cacheWithNoAccessInfo;

    public CeCreateConverterTest() {
        cache = GatewayCache.builder()
                .careCodes("careCode1")
                .accessInfo("this is access info")
                .build();
        cacheWithNoCareCodes = GatewayCache.builder()
                .accessInfo("this is access info")
                .build();
        cacheWithNoAccessInfo = GatewayCache.builder()
                .careCodes("careCode1")
                .build();


    }



    @Test
    public void test_convertCeEstabDeliver() {
        FwmtActionInstruction ffu = CeRequestBuilder.makeSite();
        CaseRequest cr = CeCreateConverter.convertCeEstabDeliver(ffu, cache);
        System.out.println(cr);
        assertEquals("careCode1\n", cr.getDescription());
        assertEquals("careCode1\n" +
                "this is access info", cr.getSpecialInstructions());
    }

    @Test
    public void test_convertCeEstabDeliverSecure() {
        FwmtActionInstruction ffu = CeRequestBuilder.makeSite();
        CaseRequest cr = CeCreateConverter.convertCeEstabDeliverSecure(ffu, cache);
        System.out.println(cr);
        assertEquals("careCode1\n" + "Secure Establishment", cr.getDescription());
        assertEquals("careCode1\n" +
                "this is access info", cr.getSpecialInstructions());
    }

    @Test
    public void test_convertCeEstabDeliverSecure_noCareCodes() {
        FwmtActionInstruction ffu = CeRequestBuilder.makeSite();
        CaseRequest cr = CeCreateConverter.convertCeEstabDeliverSecure(ffu, cacheWithNoCareCodes);
        System.out.println(cr);
        assertEquals( "Secure Establishment", cr.getDescription());
        assertEquals("this is access info", cr.getSpecialInstructions());
    }

    @Test
    public void test_convertCeEstabDeliverSecure_noAccessInfo() {
        FwmtActionInstruction ffu = CeRequestBuilder.makeSite();
        CaseRequest cr = CeCreateConverter.convertCeEstabDeliverSecure(ffu, cacheWithNoAccessInfo);
        System.out.println(cr);
        assertEquals( "careCode1\n" + "Secure Establishment", cr.getDescription());
        assertEquals("careCode1\n" , cr.getSpecialInstructions());
    }

    @Test
    public void test_convertCeEstabDeliverSecure_noCache() {
        FwmtActionInstruction ffu = CeRequestBuilder.makeSite();
        CaseRequest cr = CeCreateConverter.convertCeEstabDeliverSecure(ffu, GatewayCache.builder().build());
        System.out.println(cr);
        assertEquals( "Secure Establishment", cr.getDescription());
        assertEquals("" , cr.getSpecialInstructions());
    }

    @Test
    public void test_convertCeEstabDeliver_noCareCodes() {
        FwmtActionInstruction ffu = CeRequestBuilder.makeSite();
        CaseRequest cr = CeCreateConverter.convertCeEstabDeliver(ffu, cacheWithNoCareCodes);
        System.out.println(cr);
        assertEquals( "", cr.getDescription());
        assertEquals("this is access info", cr.getSpecialInstructions());
    }

    @Test
    public void test_convertCeEstabDeliver_noAccessInfo() {
        FwmtActionInstruction ffu = CeRequestBuilder.makeSite();
        CaseRequest cr = CeCreateConverter.convertCeEstabDeliver(ffu, cacheWithNoAccessInfo);
        System.out.println(cr);
        assertEquals( "careCode1\n" , cr.getDescription());
        assertEquals("careCode1\n" , cr.getSpecialInstructions());
    }

    @Test
    public void test_convertCeEstabDeliver_noCache() {
        FwmtActionInstruction ffu = CeRequestBuilder.makeSite();
        CaseRequest cr = CeCreateConverter.convertCeEstabDeliver(ffu, GatewayCache.builder().build());
        System.out.println(cr);
        assertEquals( "", cr.getDescription());
        assertEquals("" , cr.getSpecialInstructions());
    }


    @Test
    public void test_convertCeEstabFollowup() {
        FwmtActionInstruction ffu = CeRequestBuilder.makeSite();
        CaseRequest cr = CeCreateConverter.convertCeEstabFollowup(ffu, cache);
        System.out.println(cr);
        assertEquals("careCode1\n" , cr.getDescription());
        assertEquals("careCode1\n" +
                "this is access info", cr.getSpecialInstructions());
    }

    @Test
    public void test_convertCeEstabFollowupSecure() {
        FwmtActionInstruction ffu = CeRequestBuilder.makeSite();
        CaseRequest cr = CeCreateConverter.convertCeEstabFollowupSecure(ffu, cache);
        System.out.println(cr);
        assertEquals("careCode1\n" + "Secure Establishment" , cr.getDescription());
        assertEquals("careCode1\n" +
                "this is access info", cr.getSpecialInstructions());
    }

    @Test
    public void test_convertCeSite() {
        FwmtActionInstruction ffu = CeRequestBuilder.makeSite();
        CaseRequest cr = CeCreateConverter.convertCeSite(ffu, cache);
        System.out.println(cr);
        assertEquals("careCode1\n" , cr.getDescription());
        assertEquals("careCode1\n" +
                "this is access info", cr.getSpecialInstructions());
    }

    @Test
    public void test_convertCeSiteSecure() {
        FwmtActionInstruction ffu = CeRequestBuilder.makeSite();
        CaseRequest cr = CeCreateConverter.convertCeSiteSecure(ffu, cache);
        System.out.println(cr);
        assertEquals("careCode1\n" + "Secure Site" , cr.getDescription());
        assertEquals("careCode1\n" +
                "this is access info", cr.getSpecialInstructions());
    }

    @Test
    public void test_convertCeUnitDeliver() {
        FwmtActionInstruction ffu = CeRequestBuilder.makeSite();
        CaseRequest cr = CeCreateConverter.convertCeUnitDeliver(ffu, cache);
        System.out.println(cr);
        assertEquals("careCode1\n" , cr.getDescription());
        assertEquals("careCode1\n" +
                "this is access info", cr.getSpecialInstructions());
    }

    @Test
    public void test_convertCeUnitDeliverSecure() {
        FwmtActionInstruction ffu = CeRequestBuilder.makeSite();
        CaseRequest cr = CeCreateConverter.convertCeUnitDeliverSecure(ffu, cache);
        System.out.println(cr);
        assertEquals("careCode1\n" + "Secure Unit" , cr.getDescription());
        assertEquals("careCode1\n" +
                "this is access info", cr.getSpecialInstructions());
    }

    @Test
    public void test_convertCeUnitFollowup() {
        FwmtActionInstruction ffu = CeRequestBuilder.makeSite();
        CaseRequest cr = CeCreateConverter.convertCeUnitFollowup(ffu, cache);
        System.out.println(cr);
        assertEquals("careCode1\n" , cr.getDescription());
        assertEquals("careCode1\n" +
                "this is access info", cr.getSpecialInstructions());
    }

    @Test
    public void test_convertCeUnitFollowupSecure() {
        FwmtActionInstruction ffu = CeRequestBuilder.makeSite();
        CaseRequest cr = CeCreateConverter.convertCeUnitFollowupSecure(ffu, cache);
        System.out.println(cr);
        assertEquals("careCode1\n" + "Secure Unit" , cr.getDescription());
        assertEquals("careCode1\n" +
                "this is access info", cr.getSpecialInstructions());
    }


}