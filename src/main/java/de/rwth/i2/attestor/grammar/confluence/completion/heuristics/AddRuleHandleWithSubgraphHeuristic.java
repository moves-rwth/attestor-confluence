package de.rwth.i2.attestor.grammar.confluence.completion.heuristics;

import de.rwth.i2.attestor.grammar.confluence.CriticalPair;
import de.rwth.i2.attestor.grammar.util.SimpleIterator;
import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import de.rwth.i2.attestor.util.Pair;
import gnu.trove.iterator.TIntIterator;

import java.util.Collection;
import java.util.Iterator;

/**
 * If one of the fully abstracted heap configurations of the critical pair contains a handle and a subgraph that is
 * also contained in the other fully abstracted heap configuration then we add a new rule mapping the handle to a heap
 * configuration without this subgraph to make the critical pair strongly joinable if possible.
 *
 * The algorithm for a given critical pair works the following:
 * For all nonterminals N in HC1:
 *   1: Compute G1 = HC1 \ N
 *   2: Compute G2 = embedding(HC2, G1)  (if no embedding try next nonterminal)
 *   3: Compute RHS = HC2 \ G2     (N => RHS is new rule)
 *
 * Repeat with HC1 and HC2 switched
 *
 *
 */
public class AddRuleHandleWithSubgraphHeuristic extends CompletionRuleAddingHeuristic {
    @Override
    Iterable<Collection<Pair<Nonterminal, HeapConfiguration>>> addNewRules(CriticalPair criticalPair) {
        HeapConfiguration hc1 = criticalPair.getCanonical1();
        HeapConfiguration hc2 = criticalPair.getCanonical2();
        return new Iterable<Collection<Pair<Nonterminal, HeapConfiguration>>>() {
            @Override
            public Iterator<Collection<Pair<Nonterminal, HeapConfiguration>>> iterator() {
                TIntIterator hc1Nonterminals = hc1.nonterminalEdges().iterator();
                TIntIterator hc2Nonterminals = hc2.nonterminalEdges().iterator();
                return new SimpleIterator<Collection<Pair<Nonterminal, HeapConfiguration>>>() {
                    @Override
                    public Collection<Pair<Nonterminal, HeapConfiguration>> computeNext() {
                        Collection<Pair<Nonterminal, HeapConfiguration>> result = null;
                        while (result == null) {
                            if (hc1Nonterminals.hasNext()) {
                                // Choose rule left hand side from hc1
                                result = getRule(hc1, hc2, hc1Nonterminals.next());
                            } else if (hc2Nonterminals.hasNext()) {
                                // Choose rule left hand side from hc2 (switch hc1 & hc2)
                                result = getRule(hc2, hc1, hc2Nonterminals.next());
                            } else {
                                // No more possible rules
                                return null;
                            }
                        }
                        return result;
                    }
                };
            }
        };
    }

    private static Collection<Pair<Nonterminal, HeapConfiguration>> getRule(HeapConfiguration hc1, HeapConfiguration hc2, int hc1Nonterminal) {
        // 1. Compute G1 = HC1 \ N
        

        // 2. Compute G2 = embedding(HC2, G1)

        // 3. Compute RHS = HC2 \ G2
    }
}
