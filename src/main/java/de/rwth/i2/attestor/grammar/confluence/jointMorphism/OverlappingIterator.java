package de.rwth.i2.attestor.grammar.confluence.jointMorphism;

import java.util.*;

public class OverlappingIterator<Element extends GraphElement> implements Iterator<Overlapping<Element>> {
    private final Queue<Overlapping<Element>> remainingOverlappings;

    OverlappingIterator(Overlapping<Element> baseOverlapping) {
        remainingOverlappings = new ArrayDeque<>();
        remainingOverlappings.add(baseOverlapping);
    }

    @Override
    public boolean hasNext() {
        return !remainingOverlappings.isEmpty();
    }

    @Override
    public Overlapping<Element> next() {
        Overlapping<Element> next = remainingOverlappings.remove();
        remainingOverlappings.addAll(next.getAllNextEquivalences());
        return next;
    }
}
