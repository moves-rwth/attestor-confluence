package de.rwth.i2.attestor.grammar.confluence.completion.heuristics;

import de.rwth.i2.attestor.grammar.confluence.completion.CompletionState;

import java.util.Collection;

/**
 * Deactivates a rule so it can only be used for concretization, but not abstraction. Removes critical pairs that involve
 * the deactivated rule, but might introduce other critical pairs.
 * Therefore when a rule restriction heuristic is used ALL critical pairs have to be recomputed
 * (because removing a rule means strongly joinable rule might not be joinable now).
 *
 * TODO: Should we also try to reactivate rules? Maybe a possible parameter to the heuristic?
 */
public class CompletionRuleRestrictionHeuristic implements CompletionHeuristic {

    @Override
    public Iterable<CompletionState> applyHeuristic(CompletionState state) {
        // TODO
        throw new UnsupportedOperationException();
    }

}
