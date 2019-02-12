package de.rwth.i2.attestor.grammar.confluence.jointMorphism;

import de.rwth.i2.attestor.graph.digraph.NodeLabel;
import de.rwth.i2.attestor.graph.morphism.Graph;
import de.rwth.i2.attestor.util.Pair;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * A class that represents possible overlappings of hyperedges between two graphs in a graph.
 * It also keeps track of the necessary node overlappings.
 *
 * If a new edge is added this
 *
 */
public class EdgeJointMorphism extends JointMorphism {
    final private Map<GraphElement, GraphElement> mapNodeHc1ToHc2, mapNodeHc2ToHc1;  // TODO: We probably only need one map

    /**
     * Returns a new empty JointMorphism
     * @param context
     */
    private EdgeJointMorphism(HeapConfigurationContext context, Collection<GraphElement> l1Remaining,
                              Collection<GraphElement> l2Remaining) {
        super(context, l1Remaining, l2Remaining);
        // Initialize empty node equivalences
        this.mapNodeHc1ToHc2 = new HashMap<>();
        this.mapNodeHc2ToHc1 = new HashMap<>();
    }

    private EdgeJointMorphism(EdgeJointMorphism oldEdgeJointMorphism, Pair<GraphElement, GraphElement> newEquivalence) {
        super(oldEdgeJointMorphism, newEquivalence);
        // 1. Copy old node equivalences
        this.mapNodeHc1ToHc2 = new HashMap<>(oldEdgeJointMorphism.mapNodeHc1ToHc2);
        this.mapNodeHc2ToHc1 = new HashMap<>(oldEdgeJointMorphism.mapNodeHc2ToHc1);

        // 2. Add node equivalences induced by the added edge
        // TODO
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
            if (nodeLabel1.matches(nodeLabel2)) {
                return true;
            } else {
                return false;
            }
        } else if (label1 != null && label2 != null) {
            // Two selector edges
            if (label1.equals(label2)) {
                // The selector edges are the same
                return true;
            } else {
                // The selector edges are not the same
                return false;
            }
        } else {
            // Two incompatible edges
            return false;
        }

        // 2. Consider induced node equivalences
        // TODO
    }

    @Override
    EdgeJointMorphism getJointMorphism(Pair<GraphElement, GraphElement> newPair) {
        EdgeJointMorphism newJointMorphism = new EdgeJointMorphism(this, newPair);
        return newJointMorphism;
    }

    /**
     * Returns the base EdgeJointMorphism object to interate over the joint morphisms of edges between the graphs stored
     * in the context.
     * We use a static method instead of a constructor because we need to do some work before calling the super
     * constructor.
     *
     * @param context This context contains the two HeapConfiguration objects for the joint morphism.
     * @return
     */
    public static EdgeJointMorphism getEdgeJointMorphism(HeapConfigurationContext context) {
        // Extract edges from the graphs
        Collection<GraphElement> edgesGraph1, edgesGraph2;
        edgesGraph1 = getEdgesOfGraph(context.getGraph1());
        edgesGraph2 = getEdgesOfGraph(context.getGraph2());

        return new EdgeJointMorphism(context, edgesGraph1, edgesGraph2);
    }

    private static Collection<GraphElement> getEdgesOfGraph(Graph graph) {
        // TODO: Extract edges
        return null;
    }
}
