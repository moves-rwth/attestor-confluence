package de.rwth.i2.attestor.grammar.confluence.jointMorphism;

import de.rwth.i2.attestor.util.Pair;

import java.util.Collection;

public class EdgeJointMorphism extends JointMorphism {
    JointMorphism nodeMorphism; // Keep track of the node equivalences that are induced by the edge equivalences

    @Override
    protected Collection<Pair<GraphElement, GraphElement>> getAllNextEquivalences() {
        return super.getAllNextEquivalences();
    }
}
