package de.rwth.i2.attestor.grammar.confluence.jointMorphism;

import de.rwth.i2.attestor.graph.digraph.NodeLabel;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import de.rwth.i2.attestor.graph.morphism.Graph;
import de.rwth.i2.attestor.types.Type;
import de.rwth.i2.attestor.util.Pair;

import java.security.InvalidParameterException;


public class NodeCompatibilityChecker implements JointMorphismCompatibilityChecker {
    private final HeapConfiguration hc1, hc2;
    private final Graph graph1, graph2;

    /**
     * Initializes a NodeCompatibilityChecker
     *
     * @param edgeMorphism  A morphism between the edges
     * @param hc1  must be of type {@link Graph}
     * @param hc2  must be of type {@link Graph}
     */
    public NodeCompatibilityChecker(JointMorphism edgeMorphism, HeapConfiguration hc1, HeapConfiguration hc2) {
        if (!(hc1 instanceof Graph) || !(hc2 instanceof Graph)) {
            throw new InvalidParameterException("hc1 and hc2 must both be of type Graph");
        }
        this.hc1 = hc1;
        this.hc2 = hc2;
        this.graph1 = (Graph) hc1;
        this.graph2 = (Graph) hc2;
    }


    @Override
    public JointMorphismCompatibility newPairCompatibility(JointMorphism m, Pair<GraphElement, GraphElement> newPair) {
        int id1 = newPair.first().getPrivateId();
        int id2 = newPair.second().getPrivateId();

        Type t1 = (Type) graph1.getNodeLabel(id1);
        Type t2 = (Type) graph2.getNodeLabel(id2);

        if (!t1.matches(t2)) {
            return JointMorphismCompatibility.INCOMPATIBLE;
        }


    }

    public JointMorphismCompatibility getNodeEdgeVio(Graph graph1, Graph graph2, int id1, int id2) {
        graph1.getPredecessorsOf(id1).forEach(predecessors -> {
            return true;
        });
    }
}
