package de.rwth.i2.attestor.grammar.confluence.completion.heuristics;

import de.rwth.i2.attestor.grammar.confluence.CriticalPair;
import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import de.rwth.i2.attestor.main.scene.DefaultScene;
import de.rwth.i2.attestor.main.scene.Scene;
import de.rwth.i2.attestor.util.Pair;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * For a critical pair this method adds two new rules that introduce a new nonterminal.
 * The newly added nonterminals have the label "X{number}". The grammar should not include nonterminals of this format.
 *
 * TODO: Should we return different rule combinations for every combination on how to choose external nodes
 *
 */
public class AddRulesNewNonterminalHeuristic extends CompletionRuleAddingHeuristic {
    // Scene used for getting new nonterminals TODO: Alternatively we could create a new nonterminal factory
    private final Scene scene;
    private int numberNonterminals;

    public AddRulesNewNonterminalHeuristic() {
        this.scene = new DefaultScene();
        this.numberNonterminals = 0;
    }

    @Override
    Iterable<Collection<Pair<Nonterminal, HeapConfiguration>>> addNewRules(CriticalPair criticalPair) {
        // TODO: What to do with external nodes? How many, which nodes should be external? All combinations?
        HeapConfiguration hc1 = criticalPair.getCanonical1();
        HeapConfiguration hc2 = criticalPair.getCanonical2();

        if (isHandle(hc1) || isHandle(hc2)) {
            // TODO: Is there is problem, if both hc1 and hc2 are handles? Is it still a terminating HRG?
            throw new UnsupportedOperationException("Handles are not yet supported");
        } else {
            // Introduce a new nonterminal
            numberNonterminals++;
            Nonterminal newNonterminal = scene.createNonterminal("X" + numberNonterminals, 1, new boolean[] {true});
            Collection<Pair<Nonterminal, HeapConfiguration>> newRules = new ArrayList<>();
            HeapConfiguration hc1_copy = hc1.clone().builder().setExternal(hc1.nodes().get(0)).build();
            HeapConfiguration hc2_copy = hc2.clone().builder().setExternal(hc2.nodes().get(0)).build();
            newRules.add(new Pair<>(newNonterminal, hc1_copy));
            newRules.add(new Pair<>(newNonterminal, hc2_copy));
            return Collections.singleton(newRules);
        }
    }

    /**
     * Returns all subsets of length k of integers in the interval [0, n-1]
     * The iterator does not compute all combinations at once.
     *
     * TODO: Maybe put this in some util package
     *
     */
    private static Iterable<ArrayList<Integer>> combinations(int n, int k) {
        if (k == 0) {
            return Collections.singleton(new ArrayList<>());
        } else if (k == n) {
            return Collections.singleton(IntStream.range(0, n).boxed().collect(Collectors.toCollection(ArrayList::new)));
        } else if (k > n) {
            throw new IllegalArgumentException("k must be smaller than n");
        } else {
            return new Iterable<ArrayList<Integer>>() {
                @Override
                public Iterator<ArrayList<Integer>> iterator() {
                    return new Iterator<ArrayList<Integer>>() {
                        private Iterator<ArrayList<Integer>> combinationsWithNewElement = null;
                        private Iterator<ArrayList<Integer>> combinationsWithoutNewElement = null;

                        private Iterator<ArrayList<Integer>> getCombinationsWithNewElement() {
                            if (combinationsWithNewElement == null) {
                                combinationsWithNewElement = combinations(n-1, k - 1).iterator();
                            }
                            return combinationsWithNewElement;
                        }

                        private Iterator<ArrayList<Integer>> getCombinationsWithoutNewElement() {
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
                        public ArrayList<Integer> next() {
                            if (getCombinationsWithoutNewElement().hasNext()) {
                                return getCombinationsWithoutNewElement().next();
                            } else {
                                ArrayList<Integer> currrentCombination = getCombinationsWithNewElement().next();
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
