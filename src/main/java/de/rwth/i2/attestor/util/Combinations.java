package de.rwth.i2.attestor.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Combinations {

    /**
     * Returns all subsets of length k of integers in the interval [0, n-1]
     * The iterator does not compute all getCombinations at once.
     */
    public static Iterable<List<Integer>> getCombinations(int n, int k) {
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
                                combinationsWithNewElement = getCombinations(n-1, k - 1).iterator();
                            }
                            return combinationsWithNewElement;
                        }

                        private Iterator<List<Integer>> getCombinationsWithoutNewElement() {
                            if (combinationsWithoutNewElement == null) {
                                combinationsWithoutNewElement = getCombinations(n-1, k).iterator();
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
