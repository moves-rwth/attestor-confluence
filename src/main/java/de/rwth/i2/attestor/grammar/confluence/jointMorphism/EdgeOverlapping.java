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
    private EdgeOverlapping(HeapConfigurationContext context, Collection<GraphElement> l1Remaining,
                            Collection<GraphElement> l2Remaining) {
        super(context, l1Remaining, l2Remaining);
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

    public Map<GraphElement, GraphElement> getNodeMapHC2ToHC1() {
        return new HashMap<>(this.mapNodeHc2ToHc1);
    }

    @Override
    boolean isNextPairCompatible(Pair<GraphElement, GraphElement> newPair) {
        int id1 = newPair.first().getPrivateId();
        int id2 = newPair.second().getPrivateId();
        String label1 = newPair.first().getSelectorLabel();
        String label2 = newPair.second().getSelectorLabel();

        // 1. Check if the edge types are compatible
        if (label1 == null && label2 == null) {
            // Two hyperedges -> Need to get the type
            NodeLabel nodeLabel1 = getContext().getGraph1().getNodeLabel(id1);
            NodeLabel nodeLabel2 = getContext().getGraph2().getNodeLabel(id2);
            if (!nodeLabel1.matches(nodeLabel2)) {
                return false;
            }
        } else if (label1 != null && label2 != null) {
            // Two selector edges
            if (!label1.equals(label2)) {
                // The selector edges are not the same
                return false;
            }
        } else {
            // Two incompatible edges
            return false;
        }

        // 2. Consider induced node equivalences
        List<GraphElement> connectedNodesHC1, connectedNodesHC2;
        // Calculate the connected nodes (the lists must have same length because the types match)
        connectedNodesHC1 = newPair.first().getConnectedNodes(getContext().getGraph1());
        connectedNodesHC2 = newPair.second().getConnectedNodes(getContext().getGraph2());
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
     * Returns the base EdgeOverlapping object to interate over the overlapping of edges between the graphs stored
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
                result.add(new GraphElement(privateId, null));
            } else if (label instanceof Type) {
                // The current privateId is a node. Check if there are any outgoing selectors
                final int finalPrivateId = privateId; // variable must be final to be used in lambda expression later
                graph.getSuccessorsOf(privateId).forEach(successor -> {
                    for (Object edgeLabel : graph.getEdgeLabel(finalPrivateId, successor)) {
                        if (edgeLabel instanceof SelectorLabel) {
                            // There is a selector from privateId to successor
                            String selectorLabel = ((SelectorLabel) edgeLabel).getLabel();
                            result.add(new GraphElement(finalPrivateId, selectorLabel));
                        }
                    }
                    return true;
                });
            }
        }
        return result;
    }
}
