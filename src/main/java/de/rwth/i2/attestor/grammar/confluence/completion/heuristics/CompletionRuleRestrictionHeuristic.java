package de.rwth.i2.attestor.grammar.confluence.completion.heuristics;

import de.rwth.i2.attestor.grammar.GrammarRule;
import de.rwth.i2.attestor.grammar.confluence.CriticalPair;
import de.rwth.i2.attestor.grammar.confluence.completion.CompletionState;

import java.util.Collection;

/**
 * Note: When a rule restriction heuristic is used ALL critical pairs have to be recomputed (because removing a rule means strongly joinable rule might not be joinable now)
 */
public class CompletionRuleRestrictionHeuristic implements CompletionHeuristic {

    @Override
    public Collection<CompletionState> applyHeuristic(CompletionState state) {
        // TODO
        return null;
    }

}