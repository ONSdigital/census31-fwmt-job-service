package uk.gov.ons.census.fwmt.jobservice.controller;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.ons.census.fwmt.jobservice.config.FeatureFlagConfig;

@RestController
@RequestMapping("/test-support/feature-flags")
@ConditionalOnProperty(name = "APP_TESTING", havingValue = "true")
public class FeatureFlagTestSupportController {

  private final FeatureFlagConfig featureFlagConfig;

  public FeatureFlagTestSupportController(FeatureFlagConfig featureFlagConfig) {
    this.featureFlagConfig = featureFlagConfig;
  }

  @PostMapping("/reset")
  public ResponseEntity<Void> resetAllFeatureFlags(@RequestBody ResetFeatureFlagsRequest request) {
    featureFlagConfig.resetAllFlags(requireEnabled(request));
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/job")
  public ResponseEntity<Void> setJobFeatureFlag(@RequestBody JobFeatureFlagRequest request) {
    if (request == null) {
      throw badRequest("Request body must not be empty");
    }

    String survey = requireText(request.survey(), "survey");
    String action = requireText(request.action(), "action");
    boolean enabled = requireEnabled(request.enabled());
    try {
      featureFlagConfig.setInstructionEnabled(survey, action, enabled);
    } catch (IllegalArgumentException error) {
      throw badRequest(error.getMessage());
    }
    return ResponseEntity.noContent().build();
  }

  private static boolean requireEnabled(ResetFeatureFlagsRequest request) {
    if (request == null) {
      throw badRequest("Request body must not be empty");
    }
    return requireEnabled(request.enabled());
  }

  private static boolean requireEnabled(Boolean enabled) {
    if (enabled == null) {
      throw badRequest("enabled must not be null");
    }
    return enabled;
  }

  private static String requireText(String value, String fieldName) {
    if (value == null || value.trim().isEmpty()) {
      throw badRequest(fieldName + " must not be blank");
    }
    return value;
  }

  private static ResponseStatusException badRequest(String message) {
    return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
  }

  public record ResetFeatureFlagsRequest(Boolean enabled) {
  }

  public record JobFeatureFlagRequest(String survey, String action, Boolean enabled) {
  }
}

