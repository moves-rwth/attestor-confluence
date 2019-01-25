package de.rwth.i2.attestor.grammar.confluence;

import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import de.rwth.i2.attestor.util.Pair;
import org.jboss.util.Heap;

import java.util.HashSet;
import java.util.Set;

public class JointMorphism {
    Set<Pair<Integer, Integer>> nodeEquivalence;
    Set<Integer> hc1RemainingNodes;
    Set<Integer> hc2RemainingNodes;

    /**
     * Initializes JointMorphism where all nodes are disjoined
     */
    public JointMorphism(HeapConfiguration hc1, HeapConfiguration hc2) {
        nodeEquivalence = new HashSet<>();
        hc1RemainingNodes = new HashSet<>();
        hc1.nodes().forEach(node -> hc1RemainingNodes.add(node));
        hc2RemainingNodes = new HashSet<>();
        hc2.nodes().forEach(node -> hc2RemainingNodes.add(node));
    }

    private JointMorphism(JointMorphism oldJointMorphism) {
        nodeEquivalence = new HashSet<>(nodeEquivalence);
        hc1RemainingNodes = new HashSet<>(hc1RemainingNodes);
    }

    private void addNewEquivalence(int hc1Node, int hc2Node) {
        nodeEquivalence.add(new Pair<>(hc1Node, hc2Node));
    }

    @Override
    public int hashCode() {
        return nodeEquivalence.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return nodeEquivalence.equals(o);
    }
}
