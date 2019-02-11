package de.rwth.i2.attestor.grammar.confluence.jointMorphism;

import de.rwth.i2.attestor.graph.digraph.NodeLabel;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import de.rwth.i2.attestor.graph.morphism.Graph;
import de.rwth.i2.attestor.util.Pair;

import java.security.InvalidParameterException;

public class EdgeCompatibilityChecker implements JointMorphismCompatibilityChecker {
    private final HeapConfiguration hc1, hc2;
    private final Graph graph1, graph2;

    /**
     * Initializes an EdgeCompatibilityChecker.
     *
     * @param hc1  must be of type {@link Graph}
     * @param hc2  must be of type {@link Graph}
     */
    public EdgeCompatibilityChecker(HeapConfiguration hc1, HeapConfiguration hc2) {
        if (!(hc1 instanceof Graph) || !(hc2 instanceof Graph)) {
            throw new InvalidParameterException("hc1 and hc2 must both be of type Graph");
        }
        this.hc1 = hc1;
        this.hc2 = hc2;
        this.graph1 = (Graph) hc1;
        this.graph2 = (Graph) hc2;
    }

    @Override
    public boolean isNewPairCompatibile(JointMorphism m, Pair<GraphElement, GraphElement> newPair) {
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

}
