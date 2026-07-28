package uk.gov.ons.census.fwmt.jobservice.config;

import lombok.Data;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Configuration
@RefreshScope
@ConfigurationProperties(prefix = "feature-flags")
public class FeatureFlagConfig {
  private static final String SURVEY_HH = "HH";
  private static final String SURVEY_CE = "CE";
  private static final String SURVEY_SPG = "SPG";
  private static final String SURVEY_CCS = "CCS";
  private static final String SURVEY_NC = "NC";
  private static final String SURVEY_FEEDBACK = "FEEDBACK";

  private static final Map<String, List<String>> SUPPORTED_ACTIONS_BY_SURVEY = buildSupportedActionsBySurvey();

  private Map<String, Boolean> hh = new HashMap<>();
  private Map<String, Boolean> ce = new HashMap<>();
  private Map<String, Boolean> spg = new HashMap<>();
  private Map<String, Boolean> ccs = new HashMap<>();
  private Map<String, Boolean> nc = new HashMap<>();
  private Map<String, Boolean> feedback = new HashMap<>();
  private final Map<String, Boolean> runtimeOverrides = new ConcurrentHashMap<>();

  public boolean isInstructionEnabled(String survey, String actionInstruction) {
    if (survey == null || actionInstruction == null) {
      return false;
    }

    String normalizedSurvey = normalizeSurveyAlias(survey);
    if (normalizedSurvey == null) {
      return false;
    }

    String normalizedAction = normalizeInstruction(actionInstruction);
    Boolean runtimeOverride = runtimeOverrides.get(buildOverrideKey(normalizedSurvey, normalizedAction));
    if (runtimeOverride != null) {
      return runtimeOverride;
    }

    return getConfiguredMapForSurvey(normalizedSurvey)
        .getOrDefault(normalizedAction, false);
  }

  public boolean isSurveyEnabled(String survey) {
    String normalizedSurvey = normalizeSurveyAlias(survey);
    if (normalizedSurvey == null) {
      return false;
    }

    return SUPPORTED_ACTIONS_BY_SURVEY.getOrDefault(normalizedSurvey, Collections.emptyList())
        .stream()
        .anyMatch(action -> isInstructionEnabled(normalizedSurvey, action));
  }

  public void resetAllFlags(boolean enabled) {
    runtimeOverrides.clear();
    SUPPORTED_ACTIONS_BY_SURVEY.forEach((survey, actions) ->
        actions.forEach(action -> runtimeOverrides.put(buildOverrideKey(survey, action), enabled)));
  }

  public void setInstructionEnabled(String survey, String actionInstruction, boolean enabled) {
    String normalizedSurvey = requireSupportedSurvey(survey);
    String normalizedAction = normalizeInstruction(actionInstruction);
    validateSupportedAction(normalizedSurvey, normalizedAction);
    runtimeOverrides.put(buildOverrideKey(normalizedSurvey, normalizedAction), enabled);
  }

  private Map<String, Boolean> getConfiguredMapForSurvey(String normalizedSurvey) {
    switch (normalizedSurvey) {
      case SURVEY_HH:
        return hh;
      case SURVEY_CE:
        return ce;
      case SURVEY_SPG:
        return spg;
      case SURVEY_CCS:
        return ccs;
      case SURVEY_NC:
        return nc;
      case SURVEY_FEEDBACK:
        return feedback;
      default:
        return Collections.emptyMap();
    }
  }

  private String requireSupportedSurvey(String survey) {
    String normalizedSurvey = normalizeSurveyAlias(survey);
    if (normalizedSurvey == null || !SUPPORTED_ACTIONS_BY_SURVEY.containsKey(normalizedSurvey)) {
      throw new IllegalArgumentException("Unsupported survey '" + survey + "'");
    }
    return normalizedSurvey;
  }

  private void validateSupportedAction(String survey, String actionInstruction) {
    List<String> supportedActions = SUPPORTED_ACTIONS_BY_SURVEY.getOrDefault(survey, Collections.emptyList());
    if (!supportedActions.contains(actionInstruction)) {
      throw new IllegalArgumentException(
          "Unsupported action '" + actionInstruction + "' for survey '" + survey + "'");
    }
  }

  private String normalizeSurveyAlias(String survey) {
    if (survey == null) {
      return null;
    }

    String normalizedSurvey = survey.trim().toUpperCase();
    if (normalizedSurvey.isEmpty()) {
      return null;
    }

    switch (normalizedSurvey) {
      case SURVEY_HH:
      case SURVEY_CE:
      case SURVEY_SPG:
      case SURVEY_NC:
      case SURVEY_FEEDBACK:
        return normalizedSurvey;
      case SURVEY_CCS:
      case "CCS PL":
      case "CCS INT":
        return SURVEY_CCS;
      default:
        return null;
    }
  }

  private String normalizeInstruction(String actionInstruction) {
    return actionInstruction.trim().toLowerCase().replace(' ', '_');
  }

  private String buildOverrideKey(String survey, String actionInstruction) {
    return survey + ":" + actionInstruction;
  }

  private static Map<String, List<String>> buildSupportedActionsBySurvey() {
    Map<String, List<String>> supportedActionsBySurvey = new LinkedHashMap<>();
    List<String> standardActions = Arrays.asList("create", "update", "cancel", "pause", "reactivate");
    supportedActionsBySurvey.put(SURVEY_HH, standardActions);
    supportedActionsBySurvey.put(SURVEY_CE,
        Arrays.asList("create", "update", "cancel", "pause", "reactivate", "switch_ce_type"));
    supportedActionsBySurvey.put(SURVEY_SPG, standardActions);
    supportedActionsBySurvey.put(SURVEY_CCS, standardActions);
    supportedActionsBySurvey.put(SURVEY_NC, standardActions);
    supportedActionsBySurvey.put(SURVEY_FEEDBACK, Collections.singletonList("cancel"));
    return Collections.unmodifiableMap(supportedActionsBySurvey);
  }
}