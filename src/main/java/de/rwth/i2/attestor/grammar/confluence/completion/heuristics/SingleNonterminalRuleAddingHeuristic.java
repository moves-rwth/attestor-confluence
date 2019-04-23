package de.rwth.i2.attestor.grammar.confluence.completion.heuristics;

import de.rwth.i2.attestor.grammar.confluence.CriticalPair;
import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import de.rwth.i2.attestor.util.Pair;

import java.util.Collection;

/**
 * If one critical hc consists of a single uncollapsed heap configuration -> Add rules that map this nonterminal to the other hc
 */
public class SingleNonterminalRuleAddingHeuristic extends CompletionRuleAddingHeuristic {
    @Override
    Iterable<Collection<Pair<Nonterminal, HeapConfiguration>>> addNewRules(CriticalPair criticalPair) {
        throw new IllegalStateException();
    }
}
