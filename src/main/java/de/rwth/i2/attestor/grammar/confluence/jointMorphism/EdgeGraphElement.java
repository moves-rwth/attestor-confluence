package de.rwth.i2.attestor.grammar.confluence.jointMorphism;

import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.SelectorLabel;
import de.rwth.i2.attestor.graph.digraph.NodeLabel;
import de.rwth.i2.attestor.graph.morphism.Graph;
import de.rwth.i2.attestor.types.Type;
import de.rwth.i2.attestor.util.Pair;

import java.util.*;

public class EdgeGraphElement extends GraphElement {
    EdgeGraphElement(int privateId, String selectorLabel) {
        super(privateId, selectorLabel);
    }

    /**
     * Returns a list of GraphElements that represent the nodes connected to this edge. This object must be an edge in
     * the given graph.
     *
     * @param graph  A graph in which this object must be an edge.
     */
    List<NodeGraphElement> getConnectedNodes(Graph graph) {
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

    static Collection<EdgeGraphElement> getEdgesOfGraph(Graph graph) {
        Collection<EdgeGraphElement> result = new ArrayList<>();
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

    /**
     * Gets all pairs of nodes in the graph where there is at least one selector from the first node to the second node
     */
    static Collection<Pair<NodeGraphElement, NodeGraphElement>> getSelectorNodePairs(Graph graph) {
        Collection<Pair<NodeGraphElement, NodeGraphElement>> result = new ArrayList<>();
        for (int graphNode = 0; graphNode < graph.size(); graphNode++) {
            if (graph.getNodeLabel(graphNode) instanceof Type) {
                // graphNode is a node -> find all successors that are also nodes
                final int selectorSource = graphNode;
                graph.getSuccessorsOf(selectorSource).forEach(selectorTarget -> {
                    if (graph.getNodeLabel(selectorTarget) instanceof Type) {
                        // Found a selector from selectorSource to selectorTarget
                        result.add(new Pair<>(new NodeGraphElement(selectorSource), new NodeGraphElement(selectorTarget)));
                    }
                    return true; // Search for other selectors
                });
            }
        }
        return result;
    }

    /**
     * Gets all possible selectors that go from the first node to the second node in the nodePair
     */
    static Collection<EdgeGraphElement> getSelectorEdges(Graph graph, Pair<NodeGraphElement, NodeGraphElement> nodePair) {
        int src = nodePair.first().getPrivateId();
        int dst = nodePair.second().getPrivateId();
        Collection<EdgeGraphElement> result = new HashSet<>();
        for (String label : getSelectorLabels(graph, nodePair)) {
            result.add(new EdgeGraphElement(src, label));
        }
        return result;
    }

    static Collection<String> getSelectorLabels(Graph graph, Pair<NodeGraphElement, NodeGraphElement> nodePair) {
        int src = nodePair.first().getPrivateId();
        int dst = nodePair.second().getPrivateId();
        Collection<String> result = new HashSet<>();
        for (Object edgeLabel : graph.getEdgeLabel(src, dst)) {
            if (edgeLabel instanceof SelectorLabel) {
                result.add(((SelectorLabel) edgeLabel).getLabel());
            }
        }
        return result;
    }

    @Override
    public String toString() {
        if (getSelectorLabel() == null) {
            return "nonterminal" + getPrivateId();
        } else {
            return "selector(" + getPrivateId() + "," + getSelectorLabel() + ")";
        }
    }
}
