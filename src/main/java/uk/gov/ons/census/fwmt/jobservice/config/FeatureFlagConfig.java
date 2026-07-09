package uk.gov.ons.census.fwmt.jobservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "feature-flags")
public class FeatureFlagConfig {
  private Map<String, Boolean> hh = new HashMap<>();
  private Map<String, Boolean> ce = new HashMap<>();
  private Map<String, Boolean> spg = new HashMap<>();
  private Map<String, Boolean> ccs = new HashMap<>();
  private Map<String, Boolean> nc = new HashMap<>();

  public boolean isInstructionEnabled(String survey, String actionInstruction) {
    if (survey == null || actionInstruction == null) {
      return false;
    }

    return getMapForSurvey(survey)
        .getOrDefault(normalizeInstruction(actionInstruction), false);
  }

  public boolean isSurveyEnabled(String survey) {
    return getMapForSurvey(survey).values().stream().anyMatch(Boolean::booleanValue);
  }

  private Map<String, Boolean> getMapForSurvey(String survey) {
    String normalizedSurvey = survey.trim().toUpperCase();
    switch (normalizedSurvey) {
      case "HH":
        return hh;
      case "CE":
        return ce;
      case "SPG":
        return spg;
      case "CCS":
      case "CCS PL":
      case "CCS INT":
        return ccs;
      case "NC":
        return nc;
      default:
        return Collections.emptyMap();
    }
  }

  private String normalizeInstruction(String actionInstruction) {
    return actionInstruction.trim().toLowerCase().replace(' ', '_');
  }
}