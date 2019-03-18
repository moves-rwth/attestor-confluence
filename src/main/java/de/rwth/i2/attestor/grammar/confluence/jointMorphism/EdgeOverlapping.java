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
        // Null nodes in both graphs must be equal
        NodeGraphElement nullHc1 = NodeGraphElement.getNullNode(context.getGraph1());
        NodeGraphElement nullHc2 = NodeGraphElement.getNullNode(context.getGraph1());
        if (nullHc1 != null && nullHc2 != null) {
            this.mapNodeHc1ToHc2.put(nullHc1, nullHc2);
            this.mapNodeHc2ToHc1.put(nullHc2, nullHc1);
        }
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
        return Collections.unmodifiableMap(this.mapNodeHc1ToHc2);
    }

    Map<NodeGraphElement, NodeGraphElement> getNodeMapHC2ToHC1() {
        return Collections.unmodifiableMap(this.mapNodeHc2ToHc1);
    }

    @Override
    boolean isNextPairCompatible(Pair<EdgeGraphElement, EdgeGraphElement> newPair) {  // TODO: Only one outgoing selector of one type in union
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
        // We need to keep track of the additional node equivalences (in case one of the tentacles connects to the same node and another one does not)
        Map<NodeGraphElement, NodeGraphElement> newMapNodeHc1ToHc2 = new HashMap<>();
        Map<NodeGraphElement, NodeGraphElement> newMapNodeHc2ToHc1 = new HashMap<>();
        for (int i = 0; i < connectedNodesHC1.size(); i++) {
            NodeGraphElement connectedNode1 = connectedNodesHC1.get(i);
            NodeGraphElement connectedNode2 = connectedNodesHC2.get(i);
            if (!addToNodeEquivalences(newMapNodeHc1ToHc2, getNodeMapHC1ToHC2(), connectedNode1, connectedNode2)
                    || !addToNodeEquivalences(newMapNodeHc2ToHc1, getNodeMapHC2ToHC1(), connectedNode2, connectedNode1)) {
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


    /**
     * Adds the new equivalence (newKey, newValue) to the modifiableMap if it is not already in the unmodifiableMap.
     * Returns true, if there are no violations. There is a violation if the newKey is already in the modifiableMap or the unmodifiableMap but maps to a different value than newValue
     */
    private boolean addToNodeEquivalences(Map<NodeGraphElement, NodeGraphElement> modifiableMap, Map<NodeGraphElement, NodeGraphElement> unmodifiableMap, NodeGraphElement newKey, NodeGraphElement newValue) {
        if ((unmodifiableMap.containsKey(newKey) && !unmodifiableMap.get(newKey).equals(newValue))
                || (modifiableMap.containsKey(newKey) && !modifiableMap.get(newKey).equals(newValue))) {
            return false;
        } else {
            if (!unmodifiableMap.containsKey(newKey)) {
                modifiableMap.put(newKey, newValue);
            }
            return true;
        }

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
     * Things that are checked:
     * 1. All edges not in the intersection must NOT be connected to a node equivalent to an internal node in the other graph (dangling edge condition)
     * 2. All implied node equivalences must lead to compatible outgoing selector edges (same outgoing selectors have same target) TODO: Here might might prune invalid states earlier
     * 3. If two nodes are in the intersection all selector edges connecting them must also be in the intersection
     */
    public boolean isEdgeOverlappingValid() {
        // Check if the edge overlapping is valid for the edges not in the intersection in Hc1 and in Hc2
        return areRemainingEdgesValid(getHc1Remaining(), mapNodeHc1ToHc2, getContext().getGraph1(), getContext().getGraph2())
                && areRemainingEdgesValid(getHc2Remaining(), mapNodeHc2ToHc1, getContext().getGraph2(), getContext().getGraph1())
                && validOutgoingSelectors(getContext().getGraph1(), getContext().getGraph2(), getMapHC1toHC2(), mapNodeHc1ToHc2)
                && validOutgoingSelectors(getContext().getGraph2(), getContext().getGraph1(), getMapHC2toHC1(), mapNodeHc2ToHc1);
    }

    /**
     * Checks that the current overlapping allows compatible node overlappings with regard to the selectors
     *
     * This method checks the following:
     * For all selectors in graph1:
     *   - 1. Case: Both source and target nodes are in the intersection
     *      1.1 Case: The current selector edge is in the current node overlapping -> No violation
     *      1.2 Case: There is no outgoing selector edge (to ANY node) in the other graph of the same type -> No violation
     *      1.3 Case: Otherwise -> Violation
     *
     *   - 2. Case: Only source is in the intersection
     *      2.1 Case: There is an outgoing selector at the node in the other graph that has the same type -> Violation
     *           Explanation: Target not in intersection => Selector not in intersection => NodeOverlapping cannot set targets to be equivalent => Selectors are incompatible
     *      2.2 Case: Otherwise -> No violation
     *
     *   - 3. Case: Source is not in the intersection
     *      -> No violation
     *
     * TODO: Does valid selectors in graph1 imply valid selectors in graph2?
     * TODO: Optimization potential (finding the corresponding selector in case 1.2)
     */
    private boolean validOutgoingSelectors(Graph graph1, Graph graph2, Map<EdgeGraphElement, EdgeGraphElement> mapEdge1To2, Map<NodeGraphElement, NodeGraphElement> mapNode1To2) {

        for (Pair<NodeGraphElement, NodeGraphElement> selectorNodePair : EdgeGraphElement.getSelectorNodePairs(graph1)) {
            NodeGraphElement selectorSourceHc1 = selectorNodePair.first();
            NodeGraphElement selectorTargetHc1 = selectorNodePair.second();

            // There can only be a violation if at least the source node is in the intersection
            if (mapNode1To2.containsKey(selectorSourceHc1)) {
                NodeGraphElement selectorSourceHc2 = mapNode1To2.get(selectorSourceHc1);
                Collection<String> selectorsHc1 = EdgeGraphElement.getSelectorLabels(graph1, selectorNodePair);  // Selectors between source and target in hc1
                // TODO: Performance optimization: Keep map of already calculated outgoing selectors of selectorsHc2
                Collection<String> selectorsHc2 = selectorSourceHc2.getOutgoingSelectors(graph2);  // All outgoing selectors of source in hc2

                if (mapNode1To2.containsKey(selectorTargetHc1)) {
                    // 1. Case: src and target are in intersection. Check that all labels between those nodes are either
                    for (String selectorLabel : selectorsHc1) {
                        EdgeGraphElement selectorEdge = selectorSourceHc1.getOutgoingSelectorEdge(selectorLabel);
                        if (!mapEdge1To2.containsKey(selectorEdge)) {
                            // The selectorEdge is not in the intersection -> Check if selectorSourceHc2 has an outgoing selector of the same type
                            if (selectorsHc2.contains(selectorLabel)) {
                                // Found a conflict
                                return false;
                            }
                        }
                    }
                } else {
                    // 2. Case: Check that the selectorSource node in the other graph does not share any outgoing selector types
                    if (!Collections.disjoint(selectorsHc1, selectorsHc2)) {
                        // There is at least one outgoing selector of the same type in both graphs
                        return false;
                    }
                }
            }

        }
        return true;
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
    private boolean areRemainingEdgesValid(Collection<EdgeGraphElement> remainingEdges1, Map<NodeGraphElement, NodeGraphElement> mapNode1To2, Graph graph1, Graph graph2) {
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
