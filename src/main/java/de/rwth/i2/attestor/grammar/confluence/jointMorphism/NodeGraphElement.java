package de.rwth.i2.attestor.grammar.confluence.jointMorphism;

import de.rwth.i2.attestor.graph.digraph.NodeLabel;
import de.rwth.i2.attestor.graph.morphism.Graph;
import de.rwth.i2.attestor.types.Type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class NodeGraphElement extends GraphElement {
    public NodeGraphElement(int privateId) {
        super(privateId, null);
    }

    @Override
    public String toString() {
        return "node" + getPrivateId();
    }

    /**
     * Returns a collection of all nodes in the given graph excluding the specified nodes.
     */
    public static Collection<NodeGraphElement> getNodes(Graph graph, Collection<NodeGraphElement> excludeNodes) {
        Collection<NodeGraphElement> result = new ArrayList<>();
        for (int privateId = 0; privateId < graph.size(); privateId++) {
            NodeLabel label = graph.getNodeLabel(privateId);
            if (label instanceof Type) {
                // The current privateId corresponds to a node in hc
                NodeGraphElement newNode = new NodeGraphElement(privateId);
                // Add the node if it is not contained in 'excludeNodes'
                if (!excludeNodes.contains(newNode)) {
                    result.add(newNode);
                }
            }
        }
        return result;
    }

    /**
     * Returns a collection of all nodes in the given graph excluding the specified nodes.
     */
    public static Collection<NodeGraphElement> getNodes(Graph graph) {
        return getNodes(graph, new HashSet<>());
    }
}
