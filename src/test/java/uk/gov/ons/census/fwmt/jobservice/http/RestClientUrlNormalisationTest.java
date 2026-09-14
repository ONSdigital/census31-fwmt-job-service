package uk.gov.ons.census.fwmt.jobservice.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.gov.ons.census.fwmt.common.data.nc.CaseDetailsDTO;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.config.CometConfig;
import uk.gov.ons.census.fwmt.jobservice.health.CometHealthIndicator;
import uk.gov.ons.census.fwmt.jobservice.http.comet.CometPerfRequestInterceptor;
import uk.gov.ons.census.fwmt.jobservice.http.comet.CometRestClient;
import uk.gov.ons.census.fwmt.jobservice.http.rm.RmRestClient;

@ExtendWith(MockitoExtension.class)
class RestClientUrlNormalisationTest {

  @Mock
  private GatewayEventManager gatewayEventManager;

  @Mock
  private RestTemplate restTemplate;

  @Mock
  private RestTemplateBuilder restTemplateBuilder;

  @Test
  void shouldBuildCometCreateUrlWhenBaseUrlHasNoTrailingSlash() throws GatewayException {
    CometConfig cometConfig = new CometConfig(
        "user",
        "password",
        "http://fwmtgatewaytmmock:80",
        "swagger/index.html",
        "cases/",
        "",
        "",
        "",
        "");
    when(restTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(HttpEntity.class), eq(Void.class)))
        .thenReturn(ResponseEntity.ok().build());

    CometRestClient cometRestClient =
        new CometRestClient(cometConfig, gatewayEventManager, restTemplate, new CometPerfRequestInterceptor());

    cometRestClient.sendCreate(null, "case-123");

    ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
    verify(restTemplate).exchange(pathCaptor.capture(), eq(HttpMethod.PUT), any(HttpEntity.class), eq(Void.class));
    assertEquals("http://fwmtgatewaytmmock:80/cases/case-123", pathCaptor.getValue());
  }

  @Test
  void shouldBuildRmLookupUrlWhenBaseUrlHasNoTrailingSlash() throws GatewayException {
    CometConfig cometConfig = new CometConfig(
        "user",
        "password",
        "http://unused",
        "swagger/index.html",
        "cases/",
        "",
        "",
        "",
        "");
    when(restTemplateBuilder.errorHandler(any())).thenReturn(restTemplateBuilder);
    when(restTemplateBuilder.basicAuthentication(anyString(), anyString())).thenReturn(restTemplateBuilder);
    when(restTemplateBuilder.build()).thenReturn(restTemplate);
    when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(CaseDetailsDTO.class)))
        .thenReturn(ResponseEntity.ok(new CaseDetailsDTO()));

    RmRestClient rmRestClient =
        new RmRestClient("http://fwmtgatewaytmmock:80", cometConfig, restTemplateBuilder, gatewayEventManager);

    rmRestClient.getCase("case-123");

    ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
    verify(restTemplate).exchange(pathCaptor.capture(), eq(HttpMethod.GET), any(HttpEntity.class), eq(CaseDetailsDTO.class));
    assertEquals("http://fwmtgatewaytmmock:80/cases/case-details/case-123", pathCaptor.getValue());
  }

  @Test
  void shouldBuildCometHealthUrlWhenBaseUrlHasNoTrailingSlash() {
    CometConfig cometConfig = new CometConfig(
        "user",
        "password",
        "http://fwmtgatewaytmmock:80",
        "swagger/index.html",
        "cases/",
        "",
        "",
        "",
        "");
    when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), eq(null), eq(Void.class)))
        .thenReturn(ResponseEntity.ok().build());

    CometHealthIndicator cometHealthIndicator =
        new CometHealthIndicator(gatewayEventManager, restTemplateBuilder, cometConfig, restTemplate);

    cometHealthIndicator.health();

    ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
    verify(restTemplate).exchange(pathCaptor.capture(), eq(HttpMethod.GET), eq(null), eq(Void.class));
    assertEquals("http://fwmtgatewaytmmock:80/swagger/index.html", pathCaptor.getValue());
  }
}