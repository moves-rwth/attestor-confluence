package de.rwth.i2.attestor.grammar.confluence.jointMorphism;

import de.rwth.i2.attestor.graph.SelectorLabel;
import de.rwth.i2.attestor.graph.morphism.Graph;

import java.util.ArrayList;
import java.util.List;

public class EdgeGraphElement extends GraphElement {
    public EdgeGraphElement(int privateId, String selectorLabel) {
        super(privateId, selectorLabel);
    }

    /**
     * Returns a list of GraphElements that represent the nodes connected to this edge. This object must be an edge in
     * the given graph.
     *
     * @param graph  A graph in which this object must be an edge.
     * @return
     */
    public List<NodeGraphElement> getConnectedNodes(Graph graph) {
        List<NodeGraphElement> result = new ArrayList<>();
        String selectorLabel = getSelectorLabel();
        int privateId = getPrivateId();
        if (selectorLabel == null) {
            // This is a nonterminal edge -> The successors are the connected nodes
            graph.getSuccessorsOf(privateId).forEach(nodeId -> {
                result.add(new NodeGraphElement(nodeId));
                return true;
            });
            if (result.size() == 0) {
                throw new IllegalArgumentException("The GraphElement is not a nonterminal edge in the given graph.");
            }
        } else {
            // selector edge -> The privateId and the successor (edge label equal selectorLabel) are the connected nodes
            result.add(new NodeGraphElement(privateId));
            graph.getSuccessorsOf(privateId).forEach(successorId -> {
                for (Object edgeLabel : graph.getEdgeLabel(privateId, successorId)) {
                    if (edgeLabel instanceof SelectorLabel && ((SelectorLabel) edgeLabel).hasLabel(selectorLabel)) {
                        // Found the correct successor
                        result.add(new NodeGraphElement(successorId));
                        return false; // Don't continue the forEach loop
                    }
                }
                return true;  // successorId is not the correct connected node -> Search for other successors
            });
            if (result.size() != 2) {
                throw new IllegalArgumentException("The GraphElement is not a selectorEdge in the given graph.");
            }
        }
        return result;
    }
}
