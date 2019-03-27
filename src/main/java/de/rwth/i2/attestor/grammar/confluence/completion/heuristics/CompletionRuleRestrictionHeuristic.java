package de.rwth.i2.attestor.grammar.confluence.completion.heuristics;

import de.rwth.i2.attestor.grammar.GrammarRule;
import de.rwth.i2.attestor.grammar.confluence.CriticalPair;

import java.util.Collection;

/**
 * Note: When a rule restriction heuristic is used ALL critical pairs have to be recomputed (because removing a rule means strongly joinable rule might not be joinable now)
 */
public interface CompletionRuleRestrictionHeuristic extends CompletionHeuristic {

    /**
     * Returns an estimation on how helpful it would probably be to disable the given rule
     * A value of 0 means not helpful, the rule should not be disabled.
     * A value greater than 0 indicates that disabling this rule might help.
     */
    int getRuleRestrictionScore(Collection<CriticalPair> criticalPairs, GrammarRule rule);

}
