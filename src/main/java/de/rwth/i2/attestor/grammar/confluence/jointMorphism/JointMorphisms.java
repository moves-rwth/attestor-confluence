package de.rwth.i2.attestor.grammar.confluence.jointMorphism;

import java.util.Collection;
import java.util.Iterator;

public class JointMorphisms implements Iterable<JointMorphism> {
    private final Collection<GraphElement> l1, l2;
    private final JointMorphismCompatibilityChecker jmChecker;

    public JointMorphisms(Collection<GraphElement> l1, Collection<GraphElement> l2, JointMorphismCompatibilityChecker jmChecker) {
        this.jmChecker = jmChecker;
        this.l1 = l1;
        this.l2 = l2;
    }

    @Override
    public Iterator<JointMorphism> iterator() {
        return new JointMorphismIterator(l1, l2, jmChecker);
    }
}
