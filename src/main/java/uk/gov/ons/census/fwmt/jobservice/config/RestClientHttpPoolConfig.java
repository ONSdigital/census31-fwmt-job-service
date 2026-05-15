package uk.gov.ons.census.fwmt.jobservice.config;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
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
        poolingHttpClientConnectionManager.setDefaultMaxPerRoute(restTemplateHttpConfig.getConnection().getPool().getMaxPerRoute());
        poolingHttpClientConnectionManager.setMaxTotal(restTemplateHttpConfig.getConnection().getPool().getMaxTotal());
        return poolingHttpClientConnectionManager;
    }

    @Bean
    public RequestConfig requestConfig() {
        return RequestConfig.custom()
                .setConnectionRequestTimeout(restTemplateHttpConfig.getConnection().getRequestTimeout())
                .setConnectTimeout(restTemplateHttpConfig.getConnection().getTimeout())
                .setSocketTimeout(restTemplateHttpConfig.getConnection().getSocketTimeout())
                .build();
    }

    @Bean
    public CloseableHttpClient httpClient(PoolingHttpClientConnectionManager poolingHttpClientConnectionManager,
                                          RequestConfig requestConfig) {
        return HttpClientBuilder
                .create()
                .setConnectionManager(poolingHttpClientConnectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build();
    }
}
