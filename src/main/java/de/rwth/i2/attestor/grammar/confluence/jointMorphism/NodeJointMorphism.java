package de.rwth.i2.attestor.grammar.confluence.jointMorphism;

import de.rwth.i2.attestor.graph.digraph.NodeLabel;
import de.rwth.i2.attestor.graph.morphism.Graph;
import de.rwth.i2.attestor.types.Type;
import de.rwth.i2.attestor.util.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class NodeJointMorphism extends JointMorphism {

    private NodeJointMorphism(HeapConfigurationContext context, Collection<GraphElement> l1Remaining,
                              Collection<GraphElement> l2Remaining, Map<GraphElement, GraphElement> mapL1toL2,
                              Map<GraphElement, GraphElement> mapL2toL1) {
        super(context, l1Remaining, l2Remaining, mapL1toL2, mapL2toL1);
    }

    private NodeJointMorphism(NodeJointMorphism oldNodeJointMorphism, Pair<GraphElement, GraphElement> newPair) {
        super(oldNodeJointMorphism, newPair);
    }

    @Override
    boolean isNextPairCompatible(Pair<GraphElement, GraphElement> newPair) {
        int id1 = newPair.first().getPrivateId();
        int id2 = newPair.second().getPrivateId();

        // 1. Check that the node types match
        // Note: The node labels must be castable to 'Type', because they id1 and id2 belong to nodes (and not edges)
        Type t1 = (Type) getContext().getGraph1().getNodeLabel(id1);
        Type t2 = (Type) getContext().getGraph2().getNodeLabel(id2);

        if (!t1.matches(t2)) {
            return false;
        }

        // 2. Check compatibility with edges not in the intersection
        /*
        TODO:  Check that the additional node equivalence does not connect an edge not in the intersection
        TODO:  with a node in other graph internally
         */

        return true;
    }

    @Override
    NodeJointMorphism getJointMorphism(Pair<GraphElement, GraphElement> newPair) {
        return new NodeJointMorphism(this, newPair);
    }

    public static NodeJointMorphism getNodeJointMorphism(HeapConfigurationContext context,
                                                         EdgeJointMorphism edgeJointMorphism) {
        Collection<GraphElement> l1Remaining, l2Remaining;
        Map<GraphElement, GraphElement> mapL1toL2, mapL2toL1;

        // Get induced node equivalences from edgeJointMorphism
        mapL1toL2 = edgeJointMorphism.getNodeMapHC1ToHC2();
        mapL2toL1 = edgeJointMorphism.getNodeMapHC2ToHC1();

        // Get all remaining nodes
        l1Remaining = getNodes(context.getGraph1(), mapL1toL2.keySet());
        l2Remaining = getNodes(context.getGraph2(), mapL2toL1.keySet());

        // Return the NodeJointMorphism
        return new NodeJointMorphism(context, l1Remaining, l2Remaining, mapL1toL2, mapL2toL1);
    }

    /**
     * Returns a collection of all nodes in the given graph excluding the specified nodes.
     */
    private static Collection<GraphElement> getNodes(Graph graph, Collection<GraphElement> excludeNodes) {
        Collection<GraphElement> result = new ArrayList<>();
        for (int privateId = 0; privateId < graph.size(); privateId++) {
            NodeLabel label = graph.getNodeLabel(privateId);
            if (label instanceof Type) {
                // The current privateId corresponds to a node in hc
                GraphElement newNode = new GraphElement(privateId, null);
                // Add the node if it is not contained in 'excludeNodes'
                if (!excludeNodes.contains(newNode)) {
                    result.add(newNode);
                }
            }
        }
        return result;
    }
}
