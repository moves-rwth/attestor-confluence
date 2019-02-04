package de.rwth.i2.attestor.grammar.confluence.jointMorphism;

import de.rwth.i2.attestor.util.Pair;

public interface JointMorphismCompatibilityChecker {

    /**
     * Returns if the jointMorphism resulting from 'm' with the additional equivalence 'newPair' is compatible,
     * incompatible or not yet compatible.
     *
     * @param m
     * @param newPair
     * @return
     */
    JointMorphismCompatibility newPairCompatibility(JointMorphism m, Pair<Integer, Integer> newPair);

}
