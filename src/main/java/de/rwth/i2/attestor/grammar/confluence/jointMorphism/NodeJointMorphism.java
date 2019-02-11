package de.rwth.i2.attestor.grammar.confluence.jointMorphism;

import de.rwth.i2.attestor.types.Type;
import de.rwth.i2.attestor.util.Pair;

public class NodeJointMorphism extends JointMorphism {



    @Override
    boolean isNextPairCompatible(Pair<GraphElement, GraphElement> newPair) {
        int id1 = newPair.first().getPrivateId();
        int id2 = newPair.second().getPrivateId();

        Type t1 = (Type) graph1.getNodeLabel(id1);
        Type t2 = (Type) graph2.getNodeLabel(id2);

        if (!t1.matches(t2)) {
            return false;
        }

        // TODO: Check if
    }

    @Override
    JointMorphism getJointMorphism(Pair<GraphElement, GraphElement> newPair) {
        return null; // TODO: Implement
    }
}
