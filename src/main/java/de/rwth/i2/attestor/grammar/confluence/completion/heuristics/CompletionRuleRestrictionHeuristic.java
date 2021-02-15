package de.rwth.i2.attestor.grammar.confluence.completion.heuristics;

import de.rwth.i2.attestor.grammar.GrammarRule;
import de.rwth.i2.attestor.grammar.GrammarRuleCollapsed;
import de.rwth.i2.attestor.grammar.ConfluenceWrapperGrammar;
import de.rwth.i2.attestor.grammar.confluence.CriticalPair;
import de.rwth.i2.attestor.grammar.confluence.completion.CompletionState;
import org.json.JSONObject;

import java.util.*;

/**
 * Deactivates a rule so it can only be used for concretization, but not abstraction. Removes critical pairs that involve
 * the deactivated rule, but might introduce other critical pairs.
 * Therefore when a rule restriction heuristic is used ALL critical pairs have to be recomputed
 * (because removing a rule means strongly joinable rule might not be joinable now).
 *
 */
public class CompletionRuleRestrictionHeuristic extends CompletionHeuristic {
    final boolean reactivateRules;
    final boolean preventMainGrammarRuleDeactivation;

    /**
     * @param reactivateRules If set to true rules that have been deactivated can get activated again (does not activate removed rules)
     * @param preventMainGrammarRuleDeactivation If set to true only collapsed rules and generated rules are deactivated
     */
    public CompletionRuleRestrictionHeuristic(boolean reactivateRules, boolean preventMainGrammarRuleDeactivation) {
        this.reactivateRules = reactivateRules;
        this.preventMainGrammarRuleDeactivation = preventMainGrammarRuleDeactivation;
    }

    @Override
    public Iterable<CompletionState> applyHeuristic(CompletionState state) {
        List<CompletionState> result = new ArrayList<>();
        Set<GrammarRule> alreadyFlippedGrammarRules = new HashSet<>();

        // Find problematic rules
        for (CriticalPair criticalPair : state.getCriticalPairs()) {
            // Add new states one with rule 1 disable and one with rule 2 disabled
            flipRuleActivation(state, criticalPair.getR1(), alreadyFlippedGrammarRules, result);
            flipRuleActivation(state, criticalPair.getR2(), alreadyFlippedGrammarRules, result);
        }

        if (reactivateRules) {
            // Reactivate rules that have been disabled
            for (GrammarRule deactivatedRule : state.getGrammar().getInactiveRules()) {
                flipRuleActivation(state, deactivatedRule, alreadyFlippedGrammarRules, result);
            }
        }

        return result;
    }

    private boolean preventFlip(GrammarRule rule) {
        if (rule instanceof GrammarRuleCollapsed) {
            // Never prevent flip of collapsed rule
            return false;
        } else {
            // Prevent a handwritten active rule from being deactivated if the corresponding option is set
            return preventMainGrammarRuleDeactivation && rule.getRuleStatus() == GrammarRule.RuleStatus.ACTIVE;
        }
    }

    private void flipRuleActivation(CompletionState state, GrammarRule ruleToFlip, Set<GrammarRule> alreadyFlippedGrammarRules, List<CompletionState> result) {
        if (!preventFlip(ruleToFlip) && !alreadyFlippedGrammarRules.contains(ruleToFlip)) {
            ConfluenceWrapperGrammar modifiedGrammar = state.getGrammar().getModifiedGrammar(Collections.singleton(ruleToFlip), Collections.emptySet(), state.getGrammar().getAbstractionBlockingHeapConfigurations());
            // Add the new state where all critical pairs are recomputed TODO: If the rule is activated we don't need to recompute everything
            result.add(new CompletionState(modifiedGrammar, state));
            alreadyFlippedGrammarRules.add(ruleToFlip);
        }
    }

    @Override
    public String getIdentifier() {
        return "ruleRestriction";
    }

    @Override
    public JSONObject getSettings() {
        JSONObject settings = new JSONObject();
        settings.put("reactivateRules", reactivateRules);
        settings.put("preventMainGrammarRuleDeactivation", preventMainGrammarRuleDeactivation);
        return settings;
    }
}
