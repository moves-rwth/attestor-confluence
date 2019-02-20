package de.rwth.i2.attestor.grammar.confluence.jointMorphism;

import de.rwth.i2.attestor.graph.morphism.Graph;
import de.rwth.i2.attestor.types.Type;
import de.rwth.i2.attestor.util.Pair;

import java.util.Collection;
import java.util.Map;

public class NodeOverlapping extends Overlapping<NodeGraphElement> {

    private final boolean isIndependent;

    private NodeOverlapping(HeapConfigurationContext context, Collection<NodeGraphElement> hc1Remaining,
                            Collection<NodeGraphElement> hc2Remaining, Map<NodeGraphElement, NodeGraphElement> mapHc1toHc2,
                            Map<NodeGraphElement, NodeGraphElement> mapHc2toHc1) {
        super(context, hc1Remaining, hc2Remaining, mapHc1toHc2, mapHc2toHc1);
        // The node overlapping is independent if the already existing node equivalences are only containing external nodes
        isIndependent = areAllNodesExternal(context.getGraph1(), mapHc1toHc2.keySet())
                && areAllNodesExternal(context.getGraph1(), mapHc1toHc2.keySet());
    }

    private boolean areAllNodesExternal(Graph graph, Collection<NodeGraphElement> nodes) {
        for (NodeGraphElement node : nodes) {
            if (!graph.isExternal(node.getPrivateId())) {
                return false;
            }
        }
        return true;
    }

    private NodeOverlapping(NodeOverlapping oldNodeOverlapping, Pair<NodeGraphElement, NodeGraphElement> newPair) {
        super(oldNodeOverlapping, newPair);
        if (oldNodeOverlapping.isNodeOverlappingIndependent()) {
            // Check if the overlapping is still independent with the new pair (if the newPair is external in each graph)
            isIndependent = getContext().getGraph1().isExternal(newPair.first().getPrivateId())
                    && getContext().getGraph2().isExternal(newPair.second().getPrivateId());
        } else {
            // Once an overlapping is not independent its children cannot be independent again
            isIndependent = false;
        }
    }

    @Override
    boolean isNextPairCompatible(Pair<NodeGraphElement, NodeGraphElement> newPair) {
        NodeGraphElement node1 = newPair.first();
        NodeGraphElement node2 = newPair.second();
        Graph graph1 = getContext().getGraph1();
        Graph graph2 = getContext().getGraph2();
        int id1 = node1.getPrivateId();
        int id2 = node2.getPrivateId();

        // 1. Check that the node types match
        // Note: The node labels must be castable to 'Type', because they id1 and id2 belong to nodes (and not edges)
        Type t1 = (Type) graph1.getNodeLabel(id1);
        Type t2 = (Type) graph2.getNodeLabel(id2);

        if (!t1.matches(t2)) {
            // The node types do not match
            return false;
        }

        // 2. Check compatibility with edges not in the intersection
        // Note: The given node cannot be connected to edges in the intersection!
        if (isNodeViolationPointInGraph(graph1, node1, graph2, node2) ||
                isNodeViolationPointInGraph(graph2, node2, graph1, node1)) {
            // There is a violation
            return false;
        } else {
            // There is no violation
            return true;
        }
    }

    /**
     * For a node that is present in graph1 and graph2 the method returns false if the node is external in the graph2 or
     * if is not connected to any edges in graph1.
     *
     * We require that edges that are in the intersection cannot be connected to the node.
     *
     * @param node1  The NodeGraphElement of the node in graph1
     * @param node2  The NodeGraphElement of the node in graph2
     */
    private static boolean isNodeViolationPointInGraph(Graph graph1, NodeGraphElement node1, Graph graph2, NodeGraphElement node2) {
        // 1. Check if node is internal in graph
        if (graph2.isExternal(node2.getPrivateId())) {
            // The node is external -> Cannot be a violation point
            return false;
        }

        // 2. Check if there are any connected edges (Only necessary if isolated nodes are allowed in RHS)
        return node1.hasConnectedEdges(graph1);
    }

    @Override
    NodeOverlapping getOverlapping(Pair<NodeGraphElement, NodeGraphElement> newPair) {
        return new NodeOverlapping(this, newPair);
    }

    public static NodeOverlapping getNodeOverlapping(EdgeOverlapping edgeOverlapping) {
        Collection<NodeGraphElement> hc1Remaining, hc2Remaining;
        Map<NodeGraphElement, NodeGraphElement> mapHc1toHc2, mapHc2toHc1;

        // Get induced node equivalences from edgeOverlapping
        mapHc1toHc2 = edgeOverlapping.getNodeMapHC1ToHC2();
        mapHc2toHc1 = edgeOverlapping.getNodeMapHC2ToHC1();

        // Get all remaining nodes
        hc1Remaining = NodeGraphElement.getNodes(edgeOverlapping.getContext().getGraph1(), mapHc1toHc2.keySet());
        hc2Remaining = NodeGraphElement.getNodes(edgeOverlapping.getContext().getGraph2(), mapHc2toHc1.keySet());

        // Return the NodeOverlapping
        return new NodeOverlapping(edgeOverlapping.getContext(), hc1Remaining, hc2Remaining, mapHc1toHc2, mapHc2toHc1);
    }

    /**
     * An overlapping is independent if it allows that both rules that the overlapping is based on can be applied
     * directly after the other.
     * This is the case if the intersection of nodes does not contain any internal nodes
     */
    public boolean isNodeOverlappingIndependent() {
        return isIndependent;
    }


}
