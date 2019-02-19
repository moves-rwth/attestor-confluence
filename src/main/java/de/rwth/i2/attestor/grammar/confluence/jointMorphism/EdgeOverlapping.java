package de.rwth.i2.attestor.grammar.confluence.jointMorphism;

import de.rwth.i2.attestor.graph.digraph.NodeLabel;
import de.rwth.i2.attestor.graph.morphism.Graph;
import de.rwth.i2.attestor.types.Type;
import de.rwth.i2.attestor.util.Pair;

import java.util.*;


/**
 * A class that represents possible overlappings of hyperedges between two graphs in a graph.
 * It also keeps track of the necessary node overlappings.
 *
 * If a new edge is added this
 *
 */
public class EdgeOverlapping extends Overlapping<EdgeGraphElement> {
    final private Map<NodeGraphElement, NodeGraphElement> mapNodeHc1ToHc2, mapNodeHc2ToHc1;  // TODO: We probably only need one map

    /**
     * Returns a new empty Overlapping
     */
    private EdgeOverlapping(HeapConfigurationContext context, Collection<EdgeGraphElement> hc1Remaining,
                            Collection<EdgeGraphElement> hc2Remaining) {
        super(context, hc1Remaining, hc2Remaining);
        // Initialize empty node equivalences
        this.mapNodeHc1ToHc2 = new HashMap<>();
        this.mapNodeHc2ToHc1 = new HashMap<>();
    }

    private EdgeOverlapping(EdgeOverlapping oldEdgeOverlapping, Pair<EdgeGraphElement, EdgeGraphElement> newEquivalence) {
        super(oldEdgeOverlapping, newEquivalence);
        // 1. Copy old node equivalences
        this.mapNodeHc1ToHc2 = new HashMap<>(oldEdgeOverlapping.mapNodeHc1ToHc2);
        this.mapNodeHc2ToHc1 = new HashMap<>(oldEdgeOverlapping.mapNodeHc2ToHc1);

        // 2. Add node equivalences induced by the added edge
        EdgeGraphElement hc1Edge = newEquivalence.first();
        EdgeGraphElement hc2Edge = newEquivalence.second();
        List<NodeGraphElement> connectedNodesHc1 = hc1Edge.getConnectedNodes(getContext().getGraph1());
        List<NodeGraphElement> connectedNodesHc2 = hc2Edge.getConnectedNodes(getContext().getGraph2());
        for (int i = 0; i<connectedNodesHc1.size(); i++) {
            NodeGraphElement nodeHc1 = connectedNodesHc1.get(i);
            NodeGraphElement nodeHc2 = connectedNodesHc2.get(i);
            this.mapNodeHc1ToHc2.put(nodeHc1, nodeHc2);
            this.mapNodeHc2ToHc1.put(nodeHc2, nodeHc1);
        }
    }

    Map<NodeGraphElement, NodeGraphElement> getNodeMapHC1ToHC2() {
        return new HashMap<>(this.mapNodeHc1ToHc2);
    }

    NodeGraphElement getHC2Node(NodeGraphElement hc1Element) {
        return mapNodeHc1ToHc2.getOrDefault(hc1Element, null);
    }

    Map<NodeGraphElement, NodeGraphElement> getNodeMapHC2ToHC1() {
        return new HashMap<>(this.mapNodeHc2ToHc1);
    }

    NodeGraphElement getHC1Node(NodeGraphElement hc2Element) {
        return mapNodeHc2ToHc1.getOrDefault(hc2Element, null);
    }

    @Override
    boolean isNextPairCompatible(Pair<EdgeGraphElement, EdgeGraphElement> newPair) {
        int id1 = newPair.first().getPrivateId();
        int id2 = newPair.second().getPrivateId();
        String labelHc1 = newPair.first().getSelectorLabel();
        String labelHc2 = newPair.second().getSelectorLabel();

        // 1. Check if the edge types are compatible
        if (labelHc1 == null && labelHc2 == null) {
            // Two hyperedges -> Need to get the type
            NodeLabel nodeLabelHc1 = getContext().getGraph1().getNodeLabel(id1);
            NodeLabel nodeLabelHc2 = getContext().getGraph2().getNodeLabel(id2);
            if (!nodeLabelHc1.matches(nodeLabelHc2)) {
                return false;
            }
        } else if (labelHc1 != null && labelHc2 != null) {
            // Two selector edges
            if (!labelHc1.equals(labelHc2)) {
                // The selector edges are not the same
                return false;
            }
        } else {
            // Two incompatible edges
            return false;
        }

        // 2. Consider induced node equivalences
        List<NodeGraphElement> connectedNodesHC1, connectedNodesHC2;
        // Calculate the connected nodes (the lists must have same length because the types match)
        connectedNodesHC1 = newPair.first().getConnectedNodes(getContext().getGraph1());
        connectedNodesHC2 = newPair.second().getConnectedNodes(getContext().getGraph2());
        for (int i = 0; i < connectedNodesHC1.size(); i++) {
            NodeGraphElement connectedNode1, connectedNode2;
            connectedNode1 = connectedNodesHC1.get(i);
            connectedNode2 = connectedNodesHC2.get(i);
            if (mapNodeHc1ToHc2.containsKey(connectedNode1)) {
                if (!mapNodeHc1ToHc2.get(connectedNode1).equals(connectedNode2)) {
                    // The connected nodes are already in different equivalence classes
                    return false;
                }
            } else if (mapNodeHc2ToHc1.containsKey(connectedNode2)) {
                // The node2 is in the intersection, but node1 is not
                return false;
            }

            Type t1 = (Type) getContext().getGraph1().getNodeLabel(connectedNode1.getPrivateId());
            Type t2 = (Type) getContext().getGraph2().getNodeLabel(connectedNode2.getPrivateId());

            if (!t1.matches(t2)) {
                // The types of connected nodes do not match
                return false;
            }
        }

        // No violation found
        return true;
    }

    @Override
    EdgeOverlapping getOverlapping(Pair<EdgeGraphElement, EdgeGraphElement> newPair) {
        return new EdgeOverlapping(this, newPair);
    }

    /**
     * Checks if the current edge overlapping allows for compatible whole overlappings.
     * This method checks all remaining edges to see if there are any violations.
     * If the method returns true one can start to look for node overlappings based on this edge overlapping.
     *
     * All edges not in the intersection must not be connected to a node equivalent to an internal node in the other graph
     */
    public boolean isEdgeOverlappingValid() {
        // Check if the edge overlapping is valid for the edges not in the intersection in Hc1 and in Hc2
        return isEdgeOverlappingValid(getHc1Remaining(), getNodeMapHC1ToHC2(), getContext().getGraph1(), getContext().getGraph2()) &&
                isEdgeOverlappingValid(getHc2Remaining(), getNodeMapHC2ToHC1(), getContext().getGraph2(), getContext().getGraph1());
    }

    /**
     * Checks if the edge overlapping is valid for the given remaining edges that are not in the intersection.
     *
     * Checks if the nodes connected to the remaining edges (edges not in the intersection) do not correspond to an
     * internal node in graph2.
     *
     * @param remainingEdges1  The remaining edges for which violations are considered
     * @param mapNode1To2  A map of nodes from graph1 to graph2
     * @param graph1  The graph that contain the edges from remainingEdges1
     * @param graph2  The graph that does *not* contain the edges from remainingEdges1
     * @return  true if there are no violations
     */
    private boolean isEdgeOverlappingValid(Collection<EdgeGraphElement> remainingEdges1, Map<NodeGraphElement, NodeGraphElement> mapNode1To2, Graph graph1, Graph graph2) {
        for (EdgeGraphElement edge : remainingEdges1) {
            for (NodeGraphElement connectedNode1 : edge.getConnectedNodes(graph1)) {
                // Check if connectedNode1 is already in the intersection
                if (mapNode1To2.containsKey(connectedNode1)) {
                    // Check if the corresponding other node is external in graph2
                    NodeGraphElement connectedNode2 = mapNode1To2.get(connectedNode1);
                    if (!graph2.isExternal(connectedNode2.getPrivateId())) {
                        // Found a violation. connectedNode1 corresponds to an internal node in graph2.
                        return false;
                    }

                }

            }
        }
        return true;
    }

    /**
     * Returns the base EdgeOverlapping object to iterate over the overlapping of edges between the graphs stored
     * in the context.
     * We use a static method instead of a constructor because we need to do some work before calling the super
     * constructor.
     *
     * @param context This context contains the two HeapConfiguration objects for the overlapping.
     */
    public static EdgeOverlapping getEdgeOverlapping(HeapConfigurationContext context) {
        // Extract edges from the graphs
        Collection<EdgeGraphElement> edgesGraph1, edgesGraph2;
        edgesGraph1 = EdgeGraphElement.getEdgesOfGraph(context.getGraph1());
        edgesGraph2 = EdgeGraphElement.getEdgesOfGraph(context.getGraph2());

        return new EdgeOverlapping(context, edgesGraph1, edgesGraph2);
    }

}
