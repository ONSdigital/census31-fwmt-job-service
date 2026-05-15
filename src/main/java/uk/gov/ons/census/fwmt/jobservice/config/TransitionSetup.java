package uk.gov.ons.census.fwmt.jobservice.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.jobservice.enums.TransitionAction;
import uk.gov.ons.census.fwmt.jobservice.enums.TransitionRequestAction;
import uk.gov.ons.census.fwmt.jobservice.service.converter.TransitionRule;
import uk.gov.ons.census.fwmt.jobservice.service.converter.TransitionRulesLookup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static java.nio.charset.StandardCharsets.UTF_8;

@Configuration
public class TransitionSetup {
  @Autowired
  ResourceLoader resourceLoader;

  @Value(value = "${jobservice.transitionRules.path}")
  private String transitionRules;

  @Bean
  public TransitionRulesLookup buildTransitionRuleLookup() throws GatewayException {
    String transitionLine;
    Resource resource = resourceLoader.getResource(transitionRules);

    TransitionRulesLookup transitionRulesLookup = new TransitionRulesLookup();

    try (BufferedReader in = new BufferedReader(new InputStreamReader(resource.getInputStream(), UTF_8))) {
      while ((transitionLine = in.readLine()) != null) {
        String action = null;
        String requestAction = "NONE";
        String[] lookup = transitionLine.toUpperCase().split(",");
        String transitionSelector = lookup[0];

        if (lookup.length > 2) {
          action = lookup[2];
          requestAction = lookup[3];
        }

        TransitionRule transitionRule = TransitionRule.builder()
            .action(TransitionAction.valueOf(lookup[1]))
            .cacheType(action)
            .requestAction(TransitionRequestAction.valueOf(requestAction))
            .build();
        transitionRulesLookup.add(transitionSelector, transitionRule);
      }
    }catch (IOException e) {
      throw new GatewayException(GatewayException.Fault.SYSTEM_ERROR, e, "Cannot process transition rule lookup");
    }
    return transitionRulesLookup;
  }
}
