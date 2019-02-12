package de.rwth.i2.attestor.grammar.confluence.jointMorphism;

import de.rwth.i2.attestor.types.Type;
import de.rwth.i2.attestor.util.Pair;

import java.util.Collection;
import java.util.Map;

public class NodeJointMorphism extends JointMorphism {

    private NodeJointMorphism(HeapConfigurationContext context, Collection<GraphElement> l1Remaining,
                              Collection<GraphElement> l2Remaining, Map<GraphElement, GraphElement> mapL1toL2,
                              Map<GraphElement, GraphElement> mapL2toL1) {
        super(context, l1Remaining, l2Remaining, mapL1toL2, mapL2toL1);
    }

    @Override
    boolean isNextPairCompatible(Pair<GraphElement, GraphElement> newPair) {
        int id1 = newPair.first().getPrivateId();
        int id2 = newPair.second().getPrivateId();

        // 1. Check that the node types match
        // Note: The node labels must be castable to 'Type', because they id1 and id2 belong to nodes (and not edges)
        Type t1 = (Type) getContext().getGraph1().getNodeLabel(id1);
        Type t2 = (Type) getContext().getGraph2().getNodeLabel(id2);

        // 2. Check
        return t1.matches(t2);
    }

    @Override
    NodeJointMorphism getJointMorphism(Pair<GraphElement, GraphElement> newPair) {
        return null; // TODO: Implement
    }

    public static NodeJointMorphism getNodeJointMorphism(HeapConfigurationContext context,
                                                         EdgeJointMorphism edgeJointMorphism) {
        // TODO: compute the actual NodeJointMorphism
        return null;
    }
}
