package uk.gov.ons.census.fwmt.jobservice.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import com.microsoft.aad.adal4j.AuthenticationResult;
import java.util.Date;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.ons.census.fwmt.common.data.nc.CaseDetailsDTO;
import uk.gov.ons.census.fwmt.common.data.tm.Case;
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
        "",
        true);
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
    void shouldSkipCometAuthenticationForMockEvenWithCredentials() throws GatewayException {
        CometConfig cometConfig = new CometConfig(
                "user", "password", "http://fwmtgatewaytmmock:80", "swagger/index.html", "cases/",
                "client-id", "client-secret", "resource", "authority", true);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(ResponseEntity.ok().build());
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Case.class)))
                .thenReturn(ResponseEntity.ok(new Case()));

        CometRestClient cometRestClient =
                new CometRestClient(cometConfig, gatewayEventManager, restTemplate, new CometPerfRequestInterceptor());

        cometRestClient.sendCreate(null, "case-123");
        cometRestClient.getCase("case-123");

        ArgumentCaptor<HttpEntity> bodyCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.PUT), bodyCaptor.capture(), eq(Void.class));
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.GET), bodyCaptor.capture(), eq(Case.class));
        for (HttpEntity<?> body : bodyCaptor.getAllValues()) {
            assertNull(body.getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
        }
    }

    @Test
    void shouldRejectMissingCometCredentialsWhenNotUsingMock() {
        CometConfig cometConfig = new CometConfig(
                "user", "password", "http://totalmobile", "swagger/index.html", "cases/",
                "", "", "resource", "authority", false);
        CometRestClient cometRestClient =
                new CometRestClient(cometConfig, gatewayEventManager, restTemplate, new CometPerfRequestInterceptor());

        assertThrows(GatewayException.class, () -> cometRestClient.sendCreate(null, "case-123"));
        assertThrows(GatewayException.class, () -> cometRestClient.getCase("case-123"));
        verify(restTemplate, never()).exchange(anyString(), eq(HttpMethod.PUT), any(HttpEntity.class), eq(Void.class));
        verify(restTemplate, never()).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Case.class));
    }

    @Test
    void shouldSendBearerTokenForRealComet() throws GatewayException {
        CometConfig cometConfig = new CometConfig(
                "user", "password", "http://totalmobile", "swagger/index.html", "cases/",
                "client-id", "client-secret", "resource", "authority", false);
        CometRestClient cometRestClient =
                new CometRestClient(cometConfig, gatewayEventManager, restTemplate, new CometPerfRequestInterceptor());
        AuthenticationResult authenticationResult = org.mockito.Mockito.mock(AuthenticationResult.class);
        when(authenticationResult.getExpiresOnDate()).thenReturn(new Date(System.currentTimeMillis() + 60_000));
        when(authenticationResult.getAccessToken()).thenReturn("token");
        ReflectionTestUtils.setField(cometRestClient, "auth", authenticationResult);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Case.class)))
                .thenReturn(ResponseEntity.ok(new Case()));

        cometRestClient.getCase("case-123");

        ArgumentCaptor<HttpEntity> bodyCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.GET), bodyCaptor.capture(), eq(Case.class));
        assertEquals("Bearer token", bodyCaptor.getValue().getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
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
        "",
        true);
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
        "",
        true);
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