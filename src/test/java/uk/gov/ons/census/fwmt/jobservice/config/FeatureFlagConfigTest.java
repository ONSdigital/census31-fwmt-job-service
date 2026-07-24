package uk.gov.ons.census.fwmt.jobservice.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class FeatureFlagConfigTest {

  @Test
  void resetAllFlags_enablesEverySupportedFlag() {
    FeatureFlagConfig config = new FeatureFlagConfig();

    config.resetAllFlags(true);

    assertThat(config.isInstructionEnabled("HH", "CREATE")).isTrue();
    assertThat(config.isInstructionEnabled("CE", "SWITCH CE TYPE")).isTrue();
    assertThat(config.isInstructionEnabled("NC", "REACTIVATE")).isTrue();
    assertThat(config.isInstructionEnabled("FEEDBACK", "CANCEL")).isTrue();
    assertThat(config.isSurveyEnabled("CCS PL")).isTrue();
  }

  @Test
  void setInstructionEnabled_overridesConfiguredValueForSurveyAlias() {
    FeatureFlagConfig config = new FeatureFlagConfig();
    Map<String, Boolean> ccsFlags = new HashMap<>();
    ccsFlags.put("create", true);
    config.setCcs(ccsFlags);

    config.setInstructionEnabled("CCS INT", "Create", false);

    assertThat(config.isInstructionEnabled("CCS", "CREATE")).isFalse();
    assertThat(config.isInstructionEnabled("CCS PL", "CREATE")).isFalse();
  }

  @Test
  void setInstructionEnabled_rejectsUnsupportedSurvey() {
    FeatureFlagConfig config = new FeatureFlagConfig();

    assertThatThrownBy(() -> config.setInstructionEnabled("XYZ", "CREATE", true))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Unsupported survey");
  }

  @Test
  void setInstructionEnabled_rejectsUnsupportedActionForSurvey() {
    FeatureFlagConfig config = new FeatureFlagConfig();

    assertThatThrownBy(() -> config.setInstructionEnabled("HH", "SWITCH_CE_TYPE", true))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Unsupported action");
  }
}

