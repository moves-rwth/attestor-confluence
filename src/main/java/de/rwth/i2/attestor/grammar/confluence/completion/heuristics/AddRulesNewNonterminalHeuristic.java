package de.rwth.i2.attestor.grammar.confluence.completion.heuristics;

import de.rwth.i2.attestor.grammar.confluence.CriticalPair;
import de.rwth.i2.attestor.grammar.util.SimpleIterator;
import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import de.rwth.i2.attestor.graph.heap.HeapConfigurationBuilder;
import de.rwth.i2.attestor.main.scene.DefaultScene;
import de.rwth.i2.attestor.main.scene.Scene;
import de.rwth.i2.attestor.util.Pair;
import gnu.trove.list.array.TIntArrayList;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * For a critical pair this method adds two new rules that introduce a new nonterminal.
 * The newly added nonterminals have the label "X{number}". The grammar should not include nonterminals of this format.
 *
 * Use as few external nodes as possible to discourage rule application during canonicalization.
 *
 * TODO: Should we return different rule combinations for every combination on how to choose external nodes
 *
 * TODO: What to do when one side is a handle? If we only add one rule from the handle to the other graph then we have more external nodes
 *
 */
public class AddRulesNewNonterminalHeuristic extends CompletionRuleAddingHeuristic {
    private static final int UNLIMITED_NUMBER_EXTERNALS = -1;

    // Scene used for getting new nonterminals TODO: Alternatively we could create a new nonterminal factory (might even allow naming overlap...)
    private final Scene scene;
    private final int minNumberExternals;
    private final int maxNumberExternals;
    private int numberNonterminals; // Keep track of the number of nonterminals to give unique names

    public AddRulesNewNonterminalHeuristic() {
        this.scene = new DefaultScene();
        this.minNumberExternals = 1;
        this.maxNumberExternals = UNLIMITED_NUMBER_EXTERNALS;
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
                                    // No further possible combinations
                                    return null;
                                }
                                hc1ExternalNodeCombinations = combinations(hc1.countNodes(), currentNumberExternalNodes).iterator();
                            }
                            currentHc1ExternalNodeCombination = hc1ExternalNodeCombinations.next();
                            hc2ExternalNodeCombinations = combinations(hc2.countNodes(), currentNumberExternalNodes).iterator();
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
        Nonterminal newNonterminal = scene.createNonterminal("X" + numberNonterminals, 1, new boolean[] {true});
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

    /**
     * Returns all subsets of length k of integers in the interval [0, n-1]
     * The iterator does not compute all combinations at once.
     *
     * TODO: Maybe put this in some util package
     *
     */
    private static Iterable<List<Integer>> combinations(int n, int k) {
        if (k == 0) {
            return Collections.singleton(new ArrayList<>());
        } else if (k == n) {
            return Collections.singleton(IntStream.range(0, n).boxed().collect(Collectors.toCollection(ArrayList::new)));
        } else if (k > n) {
            throw new IllegalArgumentException("k must be smaller than n");
        } else {
            return new Iterable<List<Integer>>() {
                @Override
                public Iterator<List<Integer>> iterator() {
                    return new Iterator<List<Integer>>() {
                        private Iterator<List<Integer>> combinationsWithNewElement = null;
                        private Iterator<List<Integer>> combinationsWithoutNewElement = null;

                        private Iterator<List<Integer>> getCombinationsWithNewElement() {
                            if (combinationsWithNewElement == null) {
                                combinationsWithNewElement = combinations(n-1, k - 1).iterator();
                            }
                            return combinationsWithNewElement;
                        }

                        private Iterator<List<Integer>> getCombinationsWithoutNewElement() {
                            if (combinationsWithoutNewElement == null) {
                                combinationsWithoutNewElement = combinations(n-1, k).iterator();
                            }
                            return combinationsWithoutNewElement;
                        }

                        @Override
                        public boolean hasNext() {
                            return getCombinationsWithoutNewElement().hasNext() || getCombinationsWithNewElement().hasNext();
                        }

                        @Override
                        public List<Integer> next() {
                            if (getCombinationsWithoutNewElement().hasNext()) {
                                return getCombinationsWithoutNewElement().next();
                            } else {
                                List<Integer> currrentCombination = getCombinationsWithNewElement().next();
                                // Add new element
                                currrentCombination.add(n-1);
                                return currrentCombination;
                            }
                        }
                    };
                }
            };
        }
    }
}
