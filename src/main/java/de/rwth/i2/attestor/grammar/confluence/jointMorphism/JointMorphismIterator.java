package de.rwth.i2.attestor.grammar.confluence.jointMorphism;

import de.rwth.i2.attestor.util.Pair;
import gnu.trove.list.array.TIntArrayList;

import java.util.*;

public class JointMorphismIterator implements Iterator<JointMorphism> {
    private final Queue<JointMorphism> compatibleJointMorphisms;
    private final Queue<JointMorphism> notYetCompatibleJointMorphisms;
    private final JointMorphismCompatibilityChecker jmChecker;

    JointMorphismIterator(Collection<GraphElement> l1, Collection<GraphElement> l2,
                          JointMorphismCompatibilityChecker jmChecker) {
        this.jmChecker = jmChecker;
        compatibleJointMorphisms = new ArrayDeque<>();
        notYetCompatibleJointMorphisms = new ArrayDeque<>();
        notYetCompatibleJointMorphisms.add(new JointMorphism(l1, l2));
    }

    @Override
    public boolean hasNext() {
        while (compatibleJointMorphisms.isEmpty()) {
            if (notYetCompatibleJointMorphisms.isEmpty()) {
                // There is no jointMorphism in notYetCompatibleJointMorphisms and also not in compatibleJointMorphisms
                return false;
            }
            JointMorphism baseJm = notYetCompatibleJointMorphisms.remove();
            addFollowingJointMorphisms(baseJm);
        }
        // compatibleJointMorphisms is no longer empty --> there are more compatible JointMorphisms to return
        return true;
    }

    @Override
    public JointMorphism next() {
        // The hasNext() method guarantees that compatibleJointMorphisms is not empty
        return compatibleJointMorphisms.remove();
    }

    private void addFollowingJointMorphisms(JointMorphism jm) {
        Collection<JointMorphism> result = new ArrayList<>();
        for (Pair<GraphElement, GraphElement> nextPair : jm.getAllNextEquivalences()) {
            switch (jmChecker.newPairCompatibility(jm, nextPair)) {
                case COMPATIBLE:
                    compatibleJointMorphisms.add(new JointMorphism(jm, nextPair));
                    break;
                case NOT_COMPATIBLE_YET:
                    notYetCompatibleJointMorphisms.add(new JointMorphism(jm, nextPair));
                    break;
                case INCOMPATIBLE:
                    // Do nothing
                    break;
            }
        }
    }
}
