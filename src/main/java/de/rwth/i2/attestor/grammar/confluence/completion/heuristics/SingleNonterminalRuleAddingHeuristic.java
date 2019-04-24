package de.rwth.i2.attestor.grammar.confluence.completion.heuristics;

import de.rwth.i2.attestor.grammar.confluence.CriticalPair;
import de.rwth.i2.attestor.grammar.util.SimpleIterator;
import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import de.rwth.i2.attestor.graph.heap.HeapConfigurationBuilder;
import de.rwth.i2.attestor.util.Combinations;
import de.rwth.i2.attestor.util.Pair;
import gnu.trove.list.array.TIntArrayList;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * If one critical hc consists of a single uncollapsed heap configuration -> Add rules that map this nonterminal to the other hc
 */
public class SingleNonterminalRuleAddingHeuristic extends CompletionRuleAddingHeuristic {
    @Override
    Iterable<Collection<Pair<Nonterminal, HeapConfiguration>>> addNewRules(CriticalPair criticalPair) {
        HeapConfiguration hc1 = criticalPair.getCanonical1();
        HeapConfiguration hc2 = criticalPair.getCanonical2();
        Nonterminal nt1 = getNonterminal(hc1);
        Nonterminal nt2 = getNonterminal(hc2);
        if (nt1 == null && nt2 == null) {
            // Neither hc1 or hc2 is a handle -> This heuristic does not apply
            return Collections.emptySet();
        } else if (nt1 == null) { // && nt2 != null
            return getExternalNodeCombinationsRules(nt2, hc1);
        } else if (nt2 == null) { // && nt1 != null
            return getExternalNodeCombinationsRules(nt1, hc2);
        } else {  // nt1 != null && nt2 != null
            // Both are handles -> Check if a growing rule is still possible ()
            if (nt1.getRank() > nt2.getRank()) {
                return getExternalNodeCombinationsRules(nt2, hc1);
            } else if (nt2.getRank() > nt1.getRank()) {
                return getExternalNodeCombinationsRules(nt1, hc2);
            } else { // nt1.getRank() == nt2.getRank()
                // It is not possible to add a growing rule
                return Collections.emptySet();
            }
        }
    }

    /**
     * Return all rules that contain different getCombinations of external nodes
     * @param nt The LHS of the new rules
     * @param hc A HeapConfiguration that does not contain any external nodes
     * @return
     */
    private static Iterable<Collection<Pair<Nonterminal, HeapConfiguration>>> getExternalNodeCombinationsRules(Nonterminal nt, HeapConfiguration hc) {
        if (hc.nodes().size() < nt.getRank()) {
            // We would need to add a collapsed rule without a base rule (currently not supported)
            return Collections.emptySet();
        } else {
            return new Iterable<Collection<Pair<Nonterminal, HeapConfiguration>>>() {
                @Override
                public Iterator<Collection<Pair<Nonterminal, HeapConfiguration>>> iterator() {
                    return new SimpleIterator<Collection<Pair<Nonterminal, HeapConfiguration>>>() {
                        Iterator<List<Integer>> externalNodeCombinations = Combinations.getCombinations(hc.countNodes(), nt.getRank()).iterator();
                        TIntArrayList hcNodes = hc.nodes();

                        @Override
                        public Collection<Pair<Nonterminal, HeapConfiguration>> computeNext() {
                            if (externalNodeCombinations.hasNext()) {
                                HeapConfigurationBuilder builder = hc.clone().builder();
                                for (int nodeIdx : externalNodeCombinations.next()) {
                                    builder.setExternal(hcNodes.get(nodeIdx));
                                }
                                return Collections.singleton(new Pair<>(nt, builder.build()));
                            } else {
                                return null;
                            }
                        }
                    };
                }
            };
        }
    }

    private static Nonterminal getNonterminal(HeapConfiguration hc) {
        if (hc.nonterminalEdges().size() != 1) {
            return null;
        }

        // Check that there are no selectors
        TIntArrayList nodes = hc.nodes();
        for (int i = 0; i < nodes.size(); i++) {
            if (hc.selectorLabelsOf(nodes.get(i)).size() > 0) {
                return null;
            }
        }

        // Check that the number of nodes matches the nonterminal rank
        Nonterminal nt = hc.labelOf(hc.nonterminalEdges().get(0));
        if (nodes.size() == nt.getRank()) {
            return nt;
        } else {
            return null;
        }
    }
}
