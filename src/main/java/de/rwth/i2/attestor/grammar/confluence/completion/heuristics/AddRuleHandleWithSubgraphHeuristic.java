package de.rwth.i2.attestor.grammar.confluence.completion.heuristics;

import de.rwth.i2.attestor.grammar.CollapsedHeapConfiguration;
import de.rwth.i2.attestor.grammar.confluence.CriticalPair;
import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.util.Pair;

import java.util.Collection;

/**
 * If one of the fully abstracted heap configurations of the critical pair contains a handle and a subgraph that is
 * also contained in the other fully abstracted heap configuration then we add a new rule mapping the handle to a heap
 * configuration without this subgraph to make the critical pair strongly joinable if possible.
 */
public class AddRuleHandleWithSubgraphHeuristic extends CompletionRuleAddingHeuristic {
    @Override
    Collection<Pair<Nonterminal, CollapsedHeapConfiguration>> addNewRules(CriticalPair criticalPair) {
        // TODO
        throw new UnsupportedOperationException();
    }
}
