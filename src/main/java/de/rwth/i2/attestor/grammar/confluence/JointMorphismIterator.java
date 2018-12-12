package de.rwth.i2.attestor.grammar.confluence;

import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import de.rwth.i2.attestor.graph.morphism.Morphism;
import de.rwth.i2.attestor.util.Pair;

import java.util.Iterator;
import com.google.common.collect.Sets;

/**
 * A class that iterates over all possible jointly surjective morphisms g1, g2 such that (g1: l1 -> S, g2: l2 -> S)
 * for two given graphs l1 and l2.
 * The iterator does not return the morphism g1 explicitly because the node indices are preserved (g1 is identity).
 * The value returned by the iterator is a pair of S and g2.
 */


public class JointMorphismIterator implements Iterator<Pair<HeapConfiguration, Morphism>> {
    HeapConfiguration l1, l2;
    int numberOverlappingNodes;

    public JointMorphismIterator(HeapConfiguration l1, HeapConfiguration l2) {
        this.l1 = l1;
        this.l2 = l2;
        numberOverlappingNodes = 1; // Start with single overlapping node

    }

    @Override
    public boolean hasNext() {
        Set
        return false;
    }

    @Override
    public Pair<HeapConfiguration, Morphism> next() {
        return null;
    }
}
