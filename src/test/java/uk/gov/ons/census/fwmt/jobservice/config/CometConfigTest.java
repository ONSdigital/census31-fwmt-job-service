package uk.gov.ons.census.fwmt.jobservice.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.env.SystemEnvironmentPropertySource;

class CometConfigTest {

  private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
      .withUserConfiguration(CometConfig.class)
      .withPropertyValues(
          "totalmobile.username=user",
          "totalmobile.password=password",
          "totalmobile.baseUrl=http://totalmobile",
          "totalmobile.healthcheckPath=swagger/index.html",
          "totalmobile.operation.case.create.path=cases/",
          "totalmobile.comet.clientID=client-id",
          "totalmobile.comet.clientSecret=client-secret",
          "totalmobile.comet.resource=resource",
          "totalmobile.comet.authority=authority");

  @Test
  void shouldDefaultToTmMock() {
    contextRunner.run(context -> assertThat(context.getBean(CometConfig.class).useTmMock).isTrue());
  }

  @Test
  void shouldUseRealTotalmobileWhenKubernetesEnvironmentVariableIsFalse() {
    contextRunner.withPropertyValues("totalmobile.useTmMock=true")
      .withInitializer(context -> context.getEnvironment().getPropertySources().addFirst(
            new SystemEnvironmentPropertySource("pod-env", Map.of("TOTALMOBILE_USETMMOCK", "false"))))
        .run(context -> assertThat(context.getBean(CometConfig.class).useTmMock).isFalse());
  }
}