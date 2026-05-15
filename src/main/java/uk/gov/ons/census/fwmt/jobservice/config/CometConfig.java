package uk.gov.ons.census.fwmt.jobservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CometConfig {

  public final String userName;
  public final String password;
  public final String baseUrl;
  public final String healthCheckPath;
  public final String caseCreatePath;
  // O365 credentials for authentication w/o login prompt
  public final String clientId;
  public final String clientSecret;
  // Azure Directory OAUTH 2.0 AUTHORIZATION ENDPOINT
  public final String resource;
  public final String authority;

  public CometConfig(
      @Value("${totalmobile.username}") String userName,
      @Value("${totalmobile.password}") String password,
      @Value("${totalmobile.baseUrl}") String baseUrl,
      @Value("${totalmobile.healthcheckPath}") String healthCheckPath,
      @Value("${totalmobile.operation.case.create.path}") String caseCreatePath,
      @Value("${totalmobile.comet.clientID}") String clientId,
      @Value("${totalmobile.comet.clientSecret}") String clientSecret,
      @Value("${totalmobile.comet.resource}") String resource,
      @Value("${totalmobile.comet.authority}") String authority) {
    this.userName = userName;
    this.password = password;
    this.baseUrl = baseUrl;
    this.healthCheckPath = healthCheckPath;
    this.caseCreatePath = caseCreatePath;
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.resource = resource;
    this.authority = authority;
  }
}
