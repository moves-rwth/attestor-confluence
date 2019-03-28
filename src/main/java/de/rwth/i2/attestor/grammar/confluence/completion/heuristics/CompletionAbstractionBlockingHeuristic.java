package de.rwth.i2.attestor.grammar.confluence.completion.heuristics;

import de.rwth.i2.attestor.grammar.confluence.completion.CompletionState;

/**
 * Removes all critical pairs that have a fully concrete joint heap configuration. Those heap configurations are
 * saved so later the abstraction is stopped as soon as a heap configuration is present as a subgraph.
 */
public class CompletionAbstractionBlockingHeuristic implements CompletionHeuristic {
    @Override
    public Iterable<CompletionState> applyHeuristic(CompletionState state) {
        // TODO
        throw new UnsupportedOperationException();
    }
}
