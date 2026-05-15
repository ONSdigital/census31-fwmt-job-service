package uk.gov.ons.census.fwmt.jobservice.service.converter;

import java.util.HashMap;
import java.util.Map;

public class TransitionRulesLookup {

  private final Map<String, TransitionRule> transitionRulesMap = new HashMap<>();

  public TransitionRule getLookup(String cacheType, String rmRequest, String recordAge) {
    String requiredLookup = cacheType + "|" + rmRequest + "|" + recordAge;
    return transitionRulesMap.get(requiredLookup);
  }

  public void add (String transitionRuleSelector, TransitionRule transitionRule) {
    transitionRulesMap.put(transitionRuleSelector, transitionRule);
  }
}
