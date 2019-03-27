package de.rwth.i2.attestor.grammar.confluence.completion.heuristics;

import de.rwth.i2.attestor.grammar.CollapsedHeapConfiguration;
import de.rwth.i2.attestor.grammar.confluence.CriticalPair;
import de.rwth.i2.attestor.grammar.confluence.completion.CompletionState;
import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.util.Pair;

import java.util.Collection;

public abstract class CompletionRuleAddingHeuristic implements CompletionHeuristic {

    @Override
    public Iterable<CompletionState> applyHeuristic(CompletionState state) {
        // TODO: Implement
        throw new UnsupportedOperationException();
    }

    /**
     * Tries to compute rules that will remove the given critical pair
     * @param criticalPair  The critical pair which should be removed
     * @return null, if the heuristic cannot remove the critical pair, otherwise a list of new rules
     */
    abstract Collection<Pair<Nonterminal, CollapsedHeapConfiguration>> addNewRules(CriticalPair criticalPair);
}
