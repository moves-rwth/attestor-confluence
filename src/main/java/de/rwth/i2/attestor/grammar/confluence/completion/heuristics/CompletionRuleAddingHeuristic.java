package de.rwth.i2.attestor.grammar.confluence.completion.heuristics;

import de.rwth.i2.attestor.grammar.CollapsedHeapConfiguration;
import de.rwth.i2.attestor.grammar.confluence.CriticalPair;
import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.util.Pair;

import java.util.Collection;

public interface CompletionRuleAddingHeuristic extends CompletionHeuristic {

    /**
     * Tries to compute rules that will remove the given critical pair
     * @param criticalPair  The critical pair which should be removed
     * @return null, if the heuristic cannot remove the critical pair, otherwise a list of new rules
     */
    Collection<Pair<Nonterminal, CollapsedHeapConfiguration>> addNewRules(CriticalPair criticalPair);
}
