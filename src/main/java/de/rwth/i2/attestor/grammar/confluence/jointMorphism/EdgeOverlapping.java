package de.rwth.i2.attestor.grammar.confluence.jointMorphism;

import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.SelectorLabel;
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
public class EdgeOverlapping extends Overlapping {
    final private Map<GraphElement, GraphElement> mapNodeHc1ToHc2, mapNodeHc2ToHc1;  // TODO: We probably only need one map

    /**
     * Returns a new empty Overlapping
     * @param context
     */
    private EdgeOverlapping(HeapConfigurationContext context, Collection<GraphElement> hc1Remaining,
                            Collection<GraphElement> hc2Remaining) {
        super(context, hc1Remaining, hc2Remaining);
        // Initialize empty node equivalences
        this.mapNodeHc1ToHc2 = new HashMap<>();
        this.mapNodeHc2ToHc1 = new HashMap<>();
    }

    private EdgeOverlapping(EdgeOverlapping oldEdgeOverlapping, Pair<GraphElement, GraphElement> newEquivalence) {
        super(oldEdgeOverlapping, newEquivalence);
        // 1. Copy old node equivalences
        this.mapNodeHc1ToHc2 = new HashMap<>(oldEdgeOverlapping.mapNodeHc1ToHc2);
        this.mapNodeHc2ToHc1 = new HashMap<>(oldEdgeOverlapping.mapNodeHc2ToHc1);

        // 2. Add node equivalences induced by the added edge
        this.mapNodeHc1ToHc2.put(newEquivalence.first(), newEquivalence.second());
        this.mapNodeHc2ToHc1.put(newEquivalence.second(), newEquivalence.first());
    }

    public Map<GraphElement, GraphElement> getNodeMapHC1ToHC2() {
        return new HashMap<>(this.mapNodeHc1ToHc2);
    }

    public GraphElement getHC2Node(GraphElement hc1Element) {
        if (this.mapNodeHc1ToHc2.containsKey(hc1Element)) {
            return this.mapNodeHc1ToHc2.get(hc1Element);
        } else {
            return null;
        }
    }

    public Map<GraphElement, GraphElement> getNodeMapHC2ToHC1() {
        return new HashMap<>(this.mapNodeHc2ToHc1);
    }

    public GraphElement getHC1Node(GraphElement hc2Element) {
        if (this.mapNodeHc2ToHc1.containsKey(hc2Element)) {
            return this.mapNodeHc2ToHc1.get(hc2Element);
        } else {
            return null;
        }
    }

    @Override
    boolean isNextPairCompatible(Pair<GraphElement, GraphElement> newPair) {
        int id1 = newPair.first().getPrivateId();
        int id2 = newPair.second().getPrivateId();
        String labeHc1 = newPair.first().getSelectorLabel();
        String labeHc2 = newPair.second().getSelectorLabel();

        // 1. Check if the edge types are compatible
        if (labeHc1 == null && labeHc2 == null) {
            // Two hyperedges -> Need to get the type
            NodeLabel nodeLabeHc1 = getContext().getGraph1().getNodeLabel(id1);
            NodeLabel nodeLabeHc2 = getContext().getGraph2().getNodeLabel(id2);
            if (!nodeLabeHc1.matches(nodeLabeHc2)) {
                return false;
            }
        } else if (labeHc1 != null && labeHc2 != null) {
            // Two selector edges
            if (!labeHc1.equals(labeHc2)) {
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
        connectedNodesHC1 = ((EdgeGraphElement) newPair.first()).getConnectedNodes(getContext().getGraph1());
        connectedNodesHC2 = ((EdgeGraphElement) newPair.second()).getConnectedNodes(getContext().getGraph2());
        for (int i = 0; i < connectedNodesHC1.size(); i++) {
            GraphElement connectedNode1, connectedNode2;
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
        }

        // No violation found
        return true;
    }

    @Override
    EdgeOverlapping getOverlapping(Pair<GraphElement, GraphElement> newPair) {
        EdgeOverlapping newOverlapping = new EdgeOverlapping(this, newPair);
        return newOverlapping;
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
    private boolean isEdgeOverlappingValid(Collection<GraphElement> remainingEdges1, Map<GraphElement, GraphElement> mapNode1To2, Graph graph1, Graph graph2) {
        for (GraphElement edge : remainingEdges1) {
            for (GraphElement connectedNode1 : ((EdgeGraphElement) edge).getConnectedNodes(graph1)) {
                // Check if connectedNode1 is already in the intersection
                if (mapNode1To2.containsKey(connectedNode1)) {
                    // Check if the corresponding other node is external in graph2
                    GraphElement connectedNode2 = mapNode1To2.get(connectedNode1);
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
     * @return
     */
    public static EdgeOverlapping getEdgeOverlapping(HeapConfigurationContext context) {
        // Extract edges from the graphs
        Collection<GraphElement> edgesGraph1, edgesGraph2;
        edgesGraph1 = getEdgesOfGraph(context.getGraph1());
        edgesGraph2 = getEdgesOfGraph(context.getGraph2());

        return new EdgeOverlapping(context, edgesGraph1, edgesGraph2);
    }

    private static Collection<GraphElement> getEdgesOfGraph(Graph graph) {
        Collection<GraphElement> result = new ArrayList<>();
        for (int privateId = 0; privateId < graph.size(); privateId++) {
            NodeLabel label = graph.getNodeLabel(privateId);
            if (label instanceof Nonterminal) {
                // The current privateId corresponds to a nonterminal edge
                result.add(new EdgeGraphElement(privateId, null));
            } else if (label instanceof Type) {
                // The current privateId is a node. Check if there are any outgoing selectors
                final int finalPrivateId = privateId; // variable must be final to be used in lambda expression later
                graph.getSuccessorsOf(privateId).forEach(successor -> {
                    for (Object edgeLabel : graph.getEdgeLabel(finalPrivateId, successor)) {
                        if (edgeLabel instanceof SelectorLabel) {
                            // There is a selector from privateId to successor
                            String selectorLabel = ((SelectorLabel) edgeLabel).getLabel();
                            result.add(new EdgeGraphElement(finalPrivateId, selectorLabel));
                        }
                    }
                    return true;
                });
            }
        }
        return result;
    }
}
