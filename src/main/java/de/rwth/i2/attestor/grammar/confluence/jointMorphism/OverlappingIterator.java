package de.rwth.i2.attestor.grammar.confluence.jointMorphism;

import java.util.*;

public class OverlappingIterator implements Iterator<Overlapping> {
    private final Queue<Overlapping> remainingOverlappings;

    OverlappingIterator(Overlapping baseOverlapping) {
        remainingOverlappings = new ArrayDeque<>();
        remainingOverlappings.add(baseOverlapping);
    }

    @Override
    public boolean hasNext() {
        return !remainingOverlappings.isEmpty();
    }

    @Override
    public Overlapping next() {
        Overlapping next = remainingOverlappings.remove();
        remainingOverlappings.addAll(next.getAllNextEquivalences());
        return next;
    }
}
