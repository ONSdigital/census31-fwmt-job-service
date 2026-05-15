package uk.gov.ons.census.fwmt.jobservice.http.rm;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.gov.ons.census.fwmt.common.data.nc.CaseDetailsDTO;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.config.CometConfig;
import uk.gov.ons.census.fwmt.jobservice.http.comet.CometRestClientResponseErrorHandler;

import java.net.MalformedURLException;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Component
public class RmRestClient {

  private final RestTemplate restTemplate;
  private final GatewayEventManager gatewayEventManager;

  private final CometConfig cometConfig;

  // temporary store for authentication result
  private AuthenticationResult auth;

  // derived values
  private final transient String basePath;

  public static final String FAILED_TM_AUTHENTICATION = "FAILED_TM_AUTHENTICATION";


  public RmRestClient(
      @Value("${rmapi.baseUrl}") String baseUrl,
      CometConfig cometConfig,
      RestTemplateBuilder restTemplateBuilder,
      GatewayEventManager gatewayEventManager) {
    this.cometConfig = cometConfig;
    this.restTemplate = restTemplateBuilder.errorHandler(new CometRestClientResponseErrorHandler())
        .basicAuthentication(cometConfig.userName, cometConfig.password).build();
    this.gatewayEventManager = gatewayEventManager;
    this.basePath = baseUrl + "cases/case-details/";
    this.auth = null;
  }

  private boolean isAuthed() {
    return this.auth != null;
  }

  private boolean isExpired() {
    return !auth.getExpiresOnDate().after(new Date());
  }

  private void auth() throws GatewayException {
    ExecutorService service = Executors.newFixedThreadPool(1);
    try {
      AuthenticationContext context = new AuthenticationContext(cometConfig.authority, false, service);
      ClientCredential cc = new ClientCredential(cometConfig.clientId, cometConfig.clientSecret);

      Future<AuthenticationResult> future = context.acquireToken(cometConfig.resource, cc, null);
      this.auth = future.get();
    } catch (MalformedURLException | InterruptedException | ExecutionException e) {
      String errorMsg = "Failed to Authenticate with RM Case API";
      gatewayEventManager
          .triggerErrorEvent(this.getClass(), errorMsg, "<N/A_CASE_ID>", FAILED_TM_AUTHENTICATION);
      throw new GatewayException(GatewayException.Fault.SYSTEM_ERROR, errorMsg, e);
    } finally {
      service.shutdown();
    }
  }

  public CaseDetailsDTO getCase(String caseId) throws GatewayException {
    String basePathway = basePath + caseId;
    if ((!isAuthed() || isExpired()) && !cometConfig.clientId.isEmpty() && !cometConfig.clientSecret.isEmpty())
      auth();
    HttpHeaders httpHeaders = new HttpHeaders();
    if (isAuthed())
      httpHeaders.setBearerAuth(auth.getAccessToken());

    HttpEntity<?> body = new HttpEntity<>(httpHeaders);
    ResponseEntity<CaseDetailsDTO> request = restTemplate.exchange(basePathway, HttpMethod.GET, body, CaseDetailsDTO.class);

    return request.getBody();
  }
}
