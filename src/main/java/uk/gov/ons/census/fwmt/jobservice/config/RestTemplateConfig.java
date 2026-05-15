package uk.gov.ons.census.fwmt.jobservice.config;

import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import uk.gov.ons.census.fwmt.jobservice.http.comet.CometRestClientResponseErrorHandler;

@Configuration
public class RestTemplateConfig {
    private final CloseableHttpClient httpClient;
    private final CometConfig cometConfig;

    public RestTemplateConfig(CloseableHttpClient httpClient, CometConfig cometConfig) {
        this.httpClient = httpClient;
        this.cometConfig = cometConfig;
    }

    @Bean
    public HttpComponentsClientHttpRequestFactory clientHttpRequestFactory() {
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setHttpClient(httpClient);
        return clientHttpRequestFactory;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplateBuilder()
                .requestFactory(this::clientHttpRequestFactory)
                .errorHandler(new CometRestClientResponseErrorHandler())
                .basicAuthentication(cometConfig.userName, cometConfig.password)
                .build();
    }
}
