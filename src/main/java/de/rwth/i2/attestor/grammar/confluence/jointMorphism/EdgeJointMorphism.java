package de.rwth.i2.attestor.grammar.confluence.jointMorphism;

import de.rwth.i2.attestor.graph.digraph.NodeLabel;
import de.rwth.i2.attestor.graph.morphism.Graph;
import de.rwth.i2.attestor.util.Pair;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


/**
 * A class that represents possible overlappings of hyperedges between two graphs in a graph.
 * It also keeps track of the necessary node overlappings.
 *
 * If a new edge is added this
 *
 */
public class EdgeJointMorphism extends JointMorphism {
    final private Map<Integer, Integer> mapNodeHc1ToHc2, mapNodeHc2ToHc1;

    Collection<GraphElement>

    /**
     * Returns a new empty JointMorphism
     * @param context
     */
    public EdgeJointMorphism(HeapConfigurationContext context) {
        super(context, edges1, edges2);
        // TODO: Implement
        // TODO: Get edges of hc1 and hc2
        Collection<GraphElement> edges1 = new HashSet<>();
        Collection<GraphElement> edges2 = new HashSet<>();

    }

    protected EdgeJointMorphism(JointMorphism oldJointMorphism, Pair<GraphElement, GraphElement> newEquivalence) {
        super(oldJointMorphism, newEquivalence);
        if (oldJointMorphism instanceof EdgeJointMorphism) {
            this.mapNodeHc1ToHc2 = new HashMap<>(((EdgeJointMorphism) oldJointMorphism).mapNodeHc1ToHc2);
            this.mapNodeHc2ToHc1 = new HashMap<>(((EdgeJointMorphism) oldJointMorphism).mapNodeHc2ToHc1);
        } else {
            this.mapNodeHc1ToHc2 = new HashMap<>();
            this.mapNodeHc2ToHc1 = new HashMap<>();
        }
        // Add node equivalences induced by the added edge
        // TODO


    }

    @Override
    boolean isNextPairCompatible(Pair<GraphElement, GraphElement> newPair) {
        // TODO: We could further prune edges by already detecting node equivalences here and detect impossible states
        int id1 = newPair.first().getPrivateId();
        int id2 = newPair.second().getPrivateId();
        String label1 = newPair.first().getSelectorLabel();
        String label2 = newPair.second().getSelectorLabel();

        if (label1 == null && label2 == null) {
            // Two hyperedges -> Need to get the type
            NodeLabel nodeLabel1 = graph1.getNodeLabel(id1);
            NodeLabel nodeLabel2 = graph2.getNodeLabel(id2);
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
    }

    @Override
    EdgeJointMorphism getJointMorphism(Pair<GraphElement, GraphElement> newPair) {

        return null;  // TODO: Implement
    }
}
