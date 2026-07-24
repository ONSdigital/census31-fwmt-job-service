package uk.gov.ons.census.fwmt.jobservice.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.ons.census.fwmt.jobservice.config.FeatureFlagConfig;

class FeatureFlagTestSupportControllerTest {

  private final FeatureFlagConfig featureFlagConfig = new FeatureFlagConfig();
  private final FeatureFlagTestSupportController controller =
      new FeatureFlagTestSupportController(featureFlagConfig);

  @Test
  void resetAllFeatureFlags_updatesAllFlags() {
    ResponseEntity<Void> response =
        controller.resetAllFeatureFlags(new FeatureFlagTestSupportController.ResetFeatureFlagsRequest(true));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    assertThat(featureFlagConfig.isInstructionEnabled("HH", "CREATE")).isTrue();
    assertThat(featureFlagConfig.isInstructionEnabled("CE", "SWITCH CE TYPE")).isTrue();
    assertThat(featureFlagConfig.isInstructionEnabled("FEEDBACK", "CANCEL")).isTrue();
  }

  @Test
  void setJobFeatureFlag_updatesOnlyRequestedFlag() {
    controller.resetAllFeatureFlags(new FeatureFlagTestSupportController.ResetFeatureFlagsRequest(true));

    ResponseEntity<Void> response = controller.setJobFeatureFlag(
        new FeatureFlagTestSupportController.JobFeatureFlagRequest("HH", "CREATE", false));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    assertThat(featureFlagConfig.isInstructionEnabled("HH", "CREATE")).isFalse();
    assertThat(featureFlagConfig.isInstructionEnabled("HH", "UPDATE")).isTrue();
  }

  @Test
  void setJobFeatureFlag_rejectsInvalidRequests() {
    assertThatThrownBy(() -> controller.setJobFeatureFlag(
        new FeatureFlagTestSupportController.JobFeatureFlagRequest(" ", "CREATE", true)))
        .isInstanceOfSatisfying(ResponseStatusException.class,
            error -> assertThat(error.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));

    assertThatThrownBy(() -> controller.resetAllFeatureFlags(
        new FeatureFlagTestSupportController.ResetFeatureFlagsRequest(null)))
        .isInstanceOfSatisfying(ResponseStatusException.class,
            error -> assertThat(error.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
  }
}

