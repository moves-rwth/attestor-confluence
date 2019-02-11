package de.rwth.i2.attestor.grammar.confluence.jointMorphism;

import de.rwth.i2.attestor.util.Pair;

import java.util.*;

public class JointMorphismIterator implements Iterator<JointMorphism> {
    private final Queue<JointMorphism> remainingJointMorphisms;

    JointMorphismIterator(JointMorphism baseMorphism) {
        remainingJointMorphisms = new ArrayDeque<>();
        remainingJointMorphisms.add(baseMorphism);
    }

    @Override
    public boolean hasNext() {
        return !remainingJointMorphisms.isEmpty();
    }

    @Override
    public JointMorphism next() {
        // The hasNext() method guarantees that compatibleJointMorphisms is not empty
        JointMorphism next = remainingJointMorphisms.remove();
        remainingJointMorphisms.addAll(next.getAllNextEquivalences());
        return next;
    }
}
