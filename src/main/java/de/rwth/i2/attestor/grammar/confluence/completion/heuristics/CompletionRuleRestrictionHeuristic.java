package de.rwth.i2.attestor.grammar.confluence.completion.heuristics;

import de.rwth.i2.attestor.grammar.GrammarRule;
import de.rwth.i2.attestor.grammar.NamedGrammar;
import de.rwth.i2.attestor.grammar.confluence.CriticalPair;
import de.rwth.i2.attestor.grammar.confluence.completion.CompletionState;

import java.util.*;

/**
 * Deactivates a rule so it can only be used for concretization, but not abstraction. Removes critical pairs that involve
 * the deactivated rule, but might introduce other critical pairs.
 * Therefore when a rule restriction heuristic is used ALL critical pairs have to be recomputed
 * (because removing a rule means strongly joinable rule might not be joinable now).
 *
 */
public class CompletionRuleRestrictionHeuristic implements CompletionHeuristic {
    final boolean reactivateRules;

    /**
     * @param reactivateRules If set to true rules that have been deactivated can get activated again (does not activate removed rules)
     */
    public CompletionRuleRestrictionHeuristic(boolean reactivateRules) {
        this.reactivateRules = reactivateRules;
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

    private void flipRuleActivation(CompletionState state, GrammarRule ruleToFlip, Set<GrammarRule> alreadyFlippedGrammarRules, List<CompletionState> result) {
        if (!alreadyFlippedGrammarRules.contains(ruleToFlip)) {
            NamedGrammar modifiedGrammar = state.getGrammar().getModifiedGrammar(Collections.singleton(ruleToFlip), Collections.emptySet(), Collections.emptySet());
            // Add the new state where all critical pairs are recomputed TODO: If the rule is activated we don't need to recompute everything
            result.add(new CompletionState(modifiedGrammar));
            alreadyFlippedGrammarRules.add(ruleToFlip);
        }
    }

}
