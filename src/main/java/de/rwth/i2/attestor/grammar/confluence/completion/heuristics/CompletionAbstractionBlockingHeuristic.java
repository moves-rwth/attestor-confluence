package de.rwth.i2.attestor.grammar.confluence.completion.heuristics;

import de.rwth.i2.attestor.grammar.confluence.completion.CompletionState;

/**
 * Removes all critical pairs that have a fully concrete joint heap configuration. Those heap configurations are
 * saved so later the abstraction is stopped as soon as a heap configuration is present as a subgraph.
 *
 * Requires that all critical pairs are recomputed
 */
public class CompletionAbstractionBlockingHeuristic implements CompletionHeuristic {
    boolean blockAllCriticalPairs;

    /**
     * @param blockAllCriticalPairs  If set to true then all concrete critical pairs are added in one step
     */
    public CompletionAbstractionBlockingHeuristic(boolean blockAllCriticalPairs) {
        this.blockAllCriticalPairs = blockAllCriticalPairs;
    }

    @Override
    public Iterable<CompletionState> applyHeuristic(CompletionState state) {


        // TODO
        throw new UnsupportedOperationException();
    }
}
