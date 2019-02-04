package de.rwth.i2.attestor.grammar.confluence.jointMorphism;

import gnu.trove.list.array.TIntArrayList;

import java.util.Iterator;

public class JointMorphisms implements Iterable<JointMorphism> {
    private final TIntArrayList l1, l2;
    private final JointMorphismCompatibilityChecker jmChecker;

    public JointMorphisms(TIntArrayList l1, TIntArrayList l2, JointMorphismCompatibilityChecker jmChecker) {
        this.jmChecker = jmChecker;
        this.l1 = l1;
        this.l2 = l2;
    }

    @Override
    public Iterator<JointMorphism> iterator() {
        return new JointMorphismIterator(l1, l2, jmChecker);
    }
}
