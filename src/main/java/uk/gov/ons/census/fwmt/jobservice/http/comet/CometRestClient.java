package uk.gov.ons.census.fwmt.jobservice.http.comet;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.gov.ons.census.fwmt.common.data.tm.*;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.config.CometConfig;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Component
public class CometRestClient {

  private final RestTemplate restTemplate;
  private final GatewayEventManager gatewayEventManager;

  // cached endpoint, to avoid repeated string concats of baseUrl + caseCreatePath
  private final String cometUrl;

  private final CometConfig cometConfig;

  // temporary store for authentication result
  private AuthenticationResult auth;

  // derived values
  private final transient String basePath;
  private final transient String createPath;
  private final transient String closePath;
  private final transient String deletePath;
  private final transient String pausePath;
  private final transient String reopenPath;
  private final transient String patchCeDetails;

  public static final String FAILED_TM_AUTHENTICATION = "FAILED_TM_AUTHENTICATION";

  public CometRestClient(
      CometConfig cometConfig,
      GatewayEventManager gatewayEventManager,
      RestTemplate restTemplate,
      CometPerfRequestInterceptor cometPerfRequestInterceptor) {
    this.cometConfig = cometConfig;
    this.gatewayEventManager = gatewayEventManager;
    this.restTemplate = restTemplate;
    this.restTemplate.setInterceptors(Arrays.asList(cometPerfRequestInterceptor));
    this.cometUrl = cometConfig.baseUrl + cometConfig.caseCreatePath;
    this.auth = null;

    this.basePath = cometUrl + "{}";
    this.createPath = cometUrl + "{}";
    this.closePath = cometUrl + "{}/close";
    this.deletePath = cometUrl + "{}/delete";
    this.patchCeDetails = cometUrl + "{}/cedetails";
    this.pausePath = cometUrl + "{}/pause";
    this.reopenPath = cometUrl + "{}/reopen";
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
      String errorMsg = "Failed to Authenticate with Totalmobile";
      gatewayEventManager
          .triggerErrorEvent(this.getClass(), errorMsg, "<N/A_CASE_ID>", FAILED_TM_AUTHENTICATION);
      throw new GatewayException(GatewayException.Fault.SYSTEM_ERROR, errorMsg, e);
    } finally {
      service.shutdown();
    }
  }

  private HttpHeaders makeAuthHeader() throws GatewayException {
    if ((!isAuthed() || isExpired()) && !cometConfig.clientId.isEmpty() && !cometConfig.clientSecret.isEmpty())
      auth();
    HttpHeaders httpHeaders = new HttpHeaders();
    if (isAuthed()) {
      httpHeaders.setBearerAuth(auth.getAccessToken());
    }
    return httpHeaders;
  }

  public ResponseEntity<Void> sendCreate(CaseRequest request, String caseId) throws GatewayException {
    HttpHeaders httpHeaders = makeAuthHeader();
    HttpEntity<CaseRequest> body = new HttpEntity<>(request, httpHeaders);
    String path = createPath.replace("{}", caseId);
    return restTemplate.exchange(path, HttpMethod.PUT, body, Void.class);
  }

  public ResponseEntity<Void> sendPause(CasePauseRequest request, String caseId) throws GatewayException {
    HttpHeaders httpHeaders = makeAuthHeader();
    HttpEntity<CasePauseRequest> body = new HttpEntity<>(request, httpHeaders);
    String path = pausePath.replace("{}", caseId);
    return restTemplate.exchange(path, HttpMethod.PUT, body, Void.class);
  }

  public ResponseEntity<Void> sendReopen(ReopenCaseRequest request, String caseId) throws GatewayException {
    HttpHeaders httpHeaders = makeAuthHeader();
    HttpEntity<ReopenCaseRequest> body = new HttpEntity<>(request, httpHeaders);
    System.out.println(body.toString());
    String path = reopenPath.replace("{}", caseId);
    return restTemplate.exchange(path, HttpMethod.POST, body, Void.class);
  }

  public ResponseEntity<Void> sendClose(String caseId) throws GatewayException {
    HttpHeaders httpHeaders = makeAuthHeader();
    HttpEntity<Void> body = new HttpEntity<>(httpHeaders);
    String path = closePath.replace("{}", caseId);
    return restTemplate.exchange(path, HttpMethod.POST, body, Void.class);
  }

  public ResponseEntity<Void> sendCeDetails(CeCasePatchRequest ceCasePatchRequest, String caseId) throws GatewayException {
    HttpHeaders httpHeaders = makeAuthHeader();
    HttpEntity<CeCasePatchRequest> body = new HttpEntity<>(ceCasePatchRequest, httpHeaders);
    String path = patchCeDetails.replace("{}", caseId);
    return restTemplate.exchange(path, HttpMethod.PATCH, body, Void.class);
  }

  public ResponseEntity<Void> sendDeletePause(String caseId) throws GatewayException {
    HttpHeaders httpHeaders = makeAuthHeader();
    HttpEntity<Void> body = new HttpEntity<>(httpHeaders);
    String path = pausePath.replace("{}", caseId);
    return restTemplate.exchange(path, HttpMethod.DELETE, body, Void.class);
  }

  public Case getCase(String caseId) throws GatewayException {
    String basePathway = cometUrl + caseId;
    if ((!isAuthed() || isExpired()) && !cometConfig.clientId.isEmpty() && !cometConfig.clientSecret.isEmpty())
      auth();
    HttpHeaders httpHeaders = new HttpHeaders();
    if (isAuthed())
      httpHeaders.setBearerAuth(auth.getAccessToken());

    HttpEntity<?> body = new HttpEntity<>(httpHeaders);
    ResponseEntity<Case> request = restTemplate.exchange(basePathway, HttpMethod.GET, body, Case.class);

    return request.getBody();
  }
}
