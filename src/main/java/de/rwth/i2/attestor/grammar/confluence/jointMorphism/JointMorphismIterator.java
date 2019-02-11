package de.rwth.i2.attestor.grammar.confluence.jointMorphism;

import de.rwth.i2.attestor.util.Pair;

import java.util.*;

public class JointMorphismIterator implements Iterator<JointMorphism> {
    private final Queue<JointMorphism> compatibleJointMorphisms;
    private final JointMorphismCompatibilityChecker jmChecker;

    JointMorphismIterator(Collection<GraphElement> l1, Collection<GraphElement> l2,
                          JointMorphismCompatibilityChecker jmChecker) {
        this.jmChecker = jmChecker;
        compatibleJointMorphisms = new ArrayDeque<>();
        compatibleJointMorphisms.add(new JointMorphism(l1, l2));
    }

    @Override
    public boolean hasNext() {
        return !compatibleJointMorphisms.isEmpty();
    }

    @Override
    public JointMorphism next() {
        // The hasNext() method guarantees that compatibleJointMorphisms is not empty
        JointMorphism next = compatibleJointMorphisms.remove();
        for (Pair<GraphElement, GraphElement> nextPair : next.getAllNextEquivalences()) {
            if (jmChecker.isNewPairCompatibile(next, nextPair)) {
                compatibleJointMorphisms.add(new JointMorphism(next, nextPair));
            }
        }
        return next;
    }
}
