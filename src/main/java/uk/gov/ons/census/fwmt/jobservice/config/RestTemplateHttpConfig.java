package uk.gov.ons.census.fwmt.jobservice.config;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties("resthttp")
@Configuration
public class RestTemplateHttpConfig {

    @Getter
    private final ConnectionConfig connection;

    public RestTemplateHttpConfig(ConnectionConfig connection) {
        this.connection = connection;
    }

    @Data
    @Configuration
    public static class ConnectionConfig {
        private Integer requestTimeout;
        private Integer timeout;
        private Integer socketTimeout;

        @Getter
        private final ConnectionPoolConfig pool;

        public ConnectionConfig(ConnectionPoolConfig pool) {
            this.pool = pool;
        }

        @Data
        @Configuration
        public static class ConnectionPoolConfig {
            private Integer maxPerRoute;
            private Integer maxTotal;
        }
    }
}
