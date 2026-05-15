package uk.gov.ons.census.fwmt.jobservice.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.config.CometConfig;

@Slf4j
@Component
public class CometHealthIndicator extends AbstractHealthIndicator {

  public static final String TM_SERVICE_UP = "TM_SERVICE_UP";
  public static final String TM_SERVICE_DOWN = "TM_SERVICE_DOWN";
  private final GatewayEventManager gatewayEventManager;
  private final String swaggerUrl;
  private final RestTemplate restTemplate;

  public CometHealthIndicator(GatewayEventManager gatewayEventManager,
                              RestTemplateBuilder restTemplateBuilder,
                              CometConfig cometConfig,
                              RestTemplate restTemplate) {
    this.gatewayEventManager = gatewayEventManager;
    this.swaggerUrl = cometConfig.baseUrl + cometConfig.healthCheckPath;
    this.restTemplate = restTemplate;
  }

  @Override protected void doHealthCheck(Health.Builder builder) {
    try {
      HttpStatus responseCode = this.restTemplate.exchange(swaggerUrl, HttpMethod.GET, null, Void.class).getStatusCode();

      if (responseCode.is2xxSuccessful()) {
        builder.up().withDetail(responseCode.toString(), String.class).build();
        gatewayEventManager.triggerEvent("<N/A>", TM_SERVICE_UP, "response code", responseCode.toString());
      } else {
        builder.down().build();
        gatewayEventManager.triggerErrorEvent(this.getClass(), (Exception) null, "Cannot reach TM", "<NA>",
            TM_SERVICE_DOWN, "url", swaggerUrl, "Response Code", responseCode.toString());
      }

    } catch (Exception e) {
      builder.down().withDetail(e.getMessage(), e.getClass()).build();
      gatewayEventManager.triggerErrorEvent(this.getClass(), e, "Cannot reach TM", "<NA>", TM_SERVICE_DOWN,
          "url", swaggerUrl);
    }
  }
}
