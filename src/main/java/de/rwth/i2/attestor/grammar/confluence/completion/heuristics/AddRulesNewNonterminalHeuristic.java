package de.rwth.i2.attestor.grammar.confluence.completion.heuristics;

import de.rwth.i2.attestor.grammar.confluence.CriticalPair;
import de.rwth.i2.attestor.grammar.util.SimpleIterator;
import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import de.rwth.i2.attestor.graph.heap.HeapConfigurationBuilder;
import de.rwth.i2.attestor.util.Combinations;
import de.rwth.i2.attestor.util.Pair;
import gnu.trove.list.array.TIntArrayList;

import java.util.*;

/**
 * For a critical pair this method adds two new rules that introduce a new nonterminal.
 * The newly added nonterminals have the label "X{number}". The grammar should not include nonterminals of this format.
 *
 * Use as few external nodes as possible to discourage rule application during canonicalization.
 *
 * TODO: Should we return different rule getCombinations for every combination on how to choose external nodes
 *
 *
 */
public class AddRulesNewNonterminalHeuristic extends CompletionRuleAddingHeuristic {
    private static final int UNLIMITED_NUMBER_EXTERNALS = -1;

    // Scene used for getting new nonterminals TODO: Alternatively we could create a new nonterminal factory (might even allow naming overlap...)
    private final int minNumberExternals;
    private final int maxNumberExternals;
    private int numberNonterminals; // Keep track of the number of nonterminals to give unique names

    public AddRulesNewNonterminalHeuristic() {
        this(1, 1);
    }

    public AddRulesNewNonterminalHeuristic(int minNumberExternals, int maxNumberExternals) {
        this.minNumberExternals = minNumberExternals;
        this.maxNumberExternals = maxNumberExternals;
        this.numberNonterminals = 0;
    }

    @Override
    Iterable<Collection<Pair<Nonterminal, HeapConfiguration>>> addNewRules(CriticalPair criticalPair) {
        HeapConfiguration hc1 = criticalPair.getCanonical1();
        HeapConfiguration hc2 = criticalPair.getCanonical2();


        // Calculate maximum of selected external nodes
        final int maxNumberExternalsHere;
        int minNumberNodes = Math.min(hc1.countNodes(), hc2.countNodes());
        if (this.maxNumberExternals == UNLIMITED_NUMBER_EXTERNALS) {
            maxNumberExternalsHere = minNumberNodes;
        } else {
            maxNumberExternalsHere = Math.min(minNumberNodes, maxNumberExternals);
        }

        if (maxNumberExternalsHere < minNumberExternals) {
            // No combination possible
            return Collections.emptyList();
        }

        return new Iterable<Collection<Pair<Nonterminal, HeapConfiguration>>>() {

            @Override
            public Iterator<Collection<Pair<Nonterminal, HeapConfiguration>>> iterator() {
                if (maxNumberExternalsHere < minNumberExternals) {
                    return Collections.emptyIterator();
                }
                return new SimpleIterator<Collection<Pair<Nonterminal, HeapConfiguration>>>() {
                    int currentNumberExternalNodes = minNumberExternals - 1;  // Is increased by one in first computeNext call
                    List<Integer> currentHc1ExternalNodeCombination = null;
                    Iterator<List<Integer>> hc1ExternalNodeCombinations = Collections.emptyIterator();
                    Iterator<List<Integer>> hc2ExternalNodeCombinations = Collections.emptyIterator();

                    @Override
                    public Collection<Pair<Nonterminal, HeapConfiguration>> computeNext() {
                        if (!hc2ExternalNodeCombinations.hasNext()) {
                            // Try a different external node combination for hc1
                            if (!hc1ExternalNodeCombinations.hasNext()) {
                                // Try using more external nodes
                                currentNumberExternalNodes++;
                                if (currentNumberExternalNodes > maxNumberExternalsHere) {
                                    // No further possible getCombinations
                                    return null;
                                }
                                hc1ExternalNodeCombinations = Combinations.getCombinations(hc1.countNodes(), currentNumberExternalNodes).iterator();
                            }
                            currentHc1ExternalNodeCombination = hc1ExternalNodeCombinations.next();
                            hc2ExternalNodeCombinations = Combinations.getCombinations(hc2.countNodes(), currentNumberExternalNodes).iterator();
                        }
                        List<Integer> currentHc2ExternalNodeCombination = hc2ExternalNodeCombinations.next();
                        return getRules(hc1, hc2, currentHc1ExternalNodeCombination, currentHc2ExternalNodeCombination);
                    }
                };
            }
        };
    }

    private Collection<Pair<Nonterminal, HeapConfiguration>> getRules(HeapConfiguration hc1, HeapConfiguration hc2, List<Integer> hc1Externals, List<Integer> hc2Externals) {
        numberNonterminals++;
        boolean[] reductionTentacles = new boolean[hc1Externals.size()];
        for (int i = 0; i < reductionTentacles.length; i++) {
            reductionTentacles[i] = false;  // TODO: Calculate the correct value
        }
        Nonterminal newNonterminal = new GeneratedNonterminal(hc1Externals.size(), reductionTentacles, numberNonterminals);
        Collection<Pair<Nonterminal, HeapConfiguration>> newRules = new ArrayList<>();
        newRules.add(new Pair<>(newNonterminal, makeNodesExternal(hc1, hc1Externals)));
        newRules.add(new Pair<>(newNonterminal, makeNodesExternal(hc2, hc2Externals)));
        return newRules;
    }

    private static HeapConfiguration makeNodesExternal(HeapConfiguration hc, List<Integer> externalIdices) {
        TIntArrayList nodes = hc.nodes();
        HeapConfigurationBuilder builder = hc.clone().builder();
        for (int nodeIdx : externalIdices) {
            builder.setExternal(nodes.get(nodeIdx));
        }
        return builder.build();
    }
}
