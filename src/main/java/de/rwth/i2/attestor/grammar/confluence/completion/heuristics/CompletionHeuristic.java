package de.rwth.i2.attestor.grammar.confluence.completion.heuristics;

import de.rwth.i2.attestor.grammar.confluence.completion.CompletionState;

import java.util.Collection;

public interface CompletionHeuristic {

    /**
     * @param state The state on which the heuristic should be applied
     * @return A collection of all possible immediate successors the the input state according to the heuristic
     */
    Iterable<CompletionState> applyHeuristic(CompletionState state);
}
