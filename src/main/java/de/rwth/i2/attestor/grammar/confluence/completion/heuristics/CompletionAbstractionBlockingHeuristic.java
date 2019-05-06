package de.rwth.i2.attestor.grammar.confluence.completion.heuristics;

import de.rwth.i2.attestor.grammar.NamedGrammar;
import de.rwth.i2.attestor.grammar.confluence.CriticalPair;
import de.rwth.i2.attestor.grammar.confluence.completion.CompletionState;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Removes all critical pairs that have a fully concrete joint heap configuration. Those heap configurations are
 * saved so later the abstraction is stopped as soon as a heap configuration is present as a subgraph.
 *
 * Requires that all critical pairs are recomputed
 *
 * Usage notes: This heuristic should be used if there are problematic concrete critical pairs that do not occur during any analysis.
 *
 * TODO: Add option to block only one critical pair in one step
 * TODO: Add option to unblock critical pairs
 * TODO: Add optimization to check if block HC is subgraph of other block HC
 */
public class CompletionAbstractionBlockingHeuristic implements CompletionHeuristic {

    @Override
    public Iterable<CompletionState> applyHeuristic(CompletionState state) {
        Collection<HeapConfiguration> blockedHCs = new ArrayList<>(state.getGrammar().getAbstractionBlockingHeapConfigurations());
        Collection<CriticalPair> criticalPairs = new ArrayList<>();
        for (CriticalPair criticalPair : state.getCriticalPairs()) {
            HeapConfiguration hc = criticalPair.getJointHeapConfiguration().getHeapConfiguration();
            if (hc.countNonterminalEdges() == 0) {
                // Found a fully concrete critical pair
                blockedHCs.add(hc);
            } else {
                // The critical pair remains
                criticalPairs.add(criticalPair);
            }
        }
        NamedGrammar newGrammar = state.getGrammar().getModifiedGrammar(Collections.emptySet(), Collections.emptySet(), blockedHCs);
        CompletionState newState = new CompletionState(newGrammar, criticalPairs, state);
        return Collections.singleton(newState);
    }
}
