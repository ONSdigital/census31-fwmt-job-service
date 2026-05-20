package uk.gov.ons.census.fwmt.jobservice.config;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RestClientHttpPoolConfig {

  private final RestTemplateHttpConfig restTemplateHttpConfig;

  public RestClientHttpPoolConfig(RestTemplateHttpConfig restTemplateHttpConfig) {
    this.restTemplateHttpConfig = restTemplateHttpConfig;
  }

  @Bean
  public PoolingHttpClientConnectionManager poolingHttpClientConnectionManager() {
    PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager();
    poolingHttpClientConnectionManager.setDefaultMaxPerRoute(
        restTemplateHttpConfig.getConnection().getPool().getMaxPerRoute());
    poolingHttpClientConnectionManager.setMaxTotal(
        restTemplateHttpConfig.getConnection().getPool().getMaxTotal());
    return poolingHttpClientConnectionManager;
  }

  @Bean
  public RequestConfig requestConfig() {
    return RequestConfig.custom()
        .setConnectionRequestTimeout(Timeout.ofMilliseconds(
            restTemplateHttpConfig.getConnection().getRequestTimeout()))
        .setConnectTimeout(Timeout.ofMilliseconds(
            restTemplateHttpConfig.getConnection().getTimeout()))
        .setResponseTimeout(Timeout.ofMilliseconds(
            restTemplateHttpConfig.getConnection().getSocketTimeout()))
        .build();
  }

  @Bean
  public CloseableHttpClient httpClient(PoolingHttpClientConnectionManager poolingHttpClientConnectionManager,
      RequestConfig requestConfig) {
    return HttpClientBuilder.create()
        .setConnectionManager(poolingHttpClientConnectionManager)
        .setDefaultRequestConfig(requestConfig)
        .build();
  }
}
