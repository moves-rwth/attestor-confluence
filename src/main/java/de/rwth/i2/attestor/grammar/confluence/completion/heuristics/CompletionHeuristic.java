package de.rwth.i2.attestor.grammar.confluence.completion.heuristics;

import de.rwth.i2.attestor.grammar.confluence.completion.CompletionState;
import de.rwth.i2.attestor.grammar.confluence.completion.validity.GrammarValidity;

import java.util.Iterator;
import java.util.List;

/**
 * Interface for all completion heuristics. A completion heuristic returns all possible ways the heuristic can be applied
 * in the current state.
 * A heuristic tries to modify the grammar so it is "closer" to being confluent.
 *
 * A heuristic is not allowed to remove and add rules in the same step. If rules are added their original rule idx must
 * be larger than the other original rule indices.
 */
public interface CompletionHeuristic {

    /**
     * @param state The state on which the heuristic should be applied
     * @return All possible immediate successors of the input state according to the heuristic
     */
    Iterable<CompletionState> applyHeuristic(CompletionState state);
}
