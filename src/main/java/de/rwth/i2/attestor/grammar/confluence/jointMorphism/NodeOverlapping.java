package de.rwth.i2.attestor.grammar.confluence.jointMorphism;

import de.rwth.i2.attestor.graph.digraph.NodeLabel;
import de.rwth.i2.attestor.graph.morphism.Graph;
import de.rwth.i2.attestor.types.Type;
import de.rwth.i2.attestor.util.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class NodeOverlapping extends Overlapping {

    private NodeOverlapping(HeapConfigurationContext context, Collection<GraphElement> hc1Remaining,
                            Collection<GraphElement> hc2Remaining, Map<GraphElement, GraphElement> mapHc1toHc2,
                            Map<GraphElement, GraphElement> mapHc2toHc1) {
        super(context, hc1Remaining, hc2Remaining, mapHc1toHc2, mapHc2toHc1);
    }

    private NodeOverlapping(NodeOverlapping oldNodeOverlapping, Pair<GraphElement, GraphElement> newPair) {
        super(oldNodeOverlapping, newPair);
    }

    @Override
    boolean isNextPairCompatible(Pair<GraphElement, GraphElement> newPair) {
        GraphElement node1 = newPair.first();
        GraphElement node2 = newPair.second();
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
     * For a node that is present in graph1 and graph2 the method returns true if the node is external in the graph2 or
     * if is not connected to any edges in graph1.
     *
     * We require that edges that are in the intersection cannot be connected to the node.
     *
     * @param graph1
     * @param node1  The GraphElement of the node in graph1
     * @param graph2
     * @param node2  The GraphElement of the node in graph2
     */
    private static boolean isNodeViolationPointInGraph(Graph graph1, GraphElement node1, Graph graph2, GraphElement node2) {
        // 1. Check if node is internal in graph
        if (graph2.isExternal(node2.getPrivateId())) {
            // The node is external -> Cannot be a violation point
            return false;
        }

        // 2. Check if there are any connected edges (Only neccessary if isolated nodes are allowed in RHS)
        if (graph1.getPredecessorsOf(node1.getPrivateId()).size() == 0) {
            // There are no edges connected to the node -> No violation point
            return false;
        } else {
            // There are edges connected to the node
            // -> Because of requirement (see docstring of method) those edges are not in the intersection
            // -> Violation
            return true;
        }
    }

    @Override
    NodeOverlapping getOverlapping(Pair<GraphElement, GraphElement> newPair) {
        return new NodeOverlapping(this, newPair);
    }

    public static NodeOverlapping getNodeOverlapping(HeapConfigurationContext context,
                                                     EdgeOverlapping edgeOverlapping) {
        Collection<GraphElement> hc1Remaining, hc2Remaining;
        Map<GraphElement, GraphElement> mapHc1toHc2, mapHc2toHc1;

        // Get induced node equivalences from edgeOverlapping
        mapHc1toHc2 = edgeOverlapping.getNodeMapHC1ToHC2();
        mapHc2toHc1 = edgeOverlapping.getNodeMapHC2ToHC1();

        // Get all remaining nodes
        hc1Remaining = getNodes(context.getGraph1(), mapHc1toHc2.keySet());
        hc2Remaining = getNodes(context.getGraph2(), mapHc2toHc1.keySet());

        // Return the NodeOverlapping
        return new NodeOverlapping(context, hc1Remaining, hc2Remaining, mapHc1toHc2, mapHc2toHc1);
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
