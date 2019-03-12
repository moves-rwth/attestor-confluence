package de.rwth.i2.attestor.grammar.confluence.jointMorphism;

import de.rwth.i2.attestor.graph.SelectorLabel;
import de.rwth.i2.attestor.graph.digraph.NodeLabel;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import de.rwth.i2.attestor.graph.heap.internal.HeapConfigurationIdConverter;
import de.rwth.i2.attestor.graph.morphism.Graph;
import de.rwth.i2.attestor.types.Type;
import de.rwth.i2.attestor.types.Types;
import gnu.trove.list.array.TIntArrayList;
import jdk.nashorn.internal.runtime.regexp.joni.constants.NodeType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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

    public static Collection<NodeGraphElement> getNodes(Graph graph) {
        return getNodes(graph, new ArrayList<>());
    }

    public static NodeGraphElement[] getGraphElementsFromPublicIds(HeapConfiguration hc, TIntArrayList nodes) {
        NodeGraphElement[] result = new NodeGraphElement[nodes.size()];
        for (int i = 0; i<result.length; i++) {
            result[i] = new NodeGraphElement(HeapConfigurationIdConverter.getGraphId(hc, nodes.get(i)));
        }
        return result;
    }

    public EdgeGraphElement getOutgoingSelectorEdge(String label) {
        return new EdgeGraphElement(getPrivateId(), label);
    }

    public boolean hasConnectedEdges(Graph graph) {
        if (graph.getPredecessorsOf(getPrivateId()).size() != 0) {
            // There is either a nonterminal edge connected to the node, or a selector edge points to this node
            return true;
        } else {
            // Check if there is an outgoing selector edge
            TIntArrayList successors = graph.getSuccessorsOf(getPrivateId());
            for (int i = 0; i < successors.size(); i++) {
                if (graph.getNodeLabel(successors.get(i)) instanceof Type) {
                    // The successor is a node & we have found a selector
                    return true;
                }
            }
            // No incoming selector edge was found
            return false;
        }
    }

    public Set<String> getOutgoingSelectors(Graph graph) {
        Set<String> result = new HashSet<>();
        graph.getSuccessorsOf(getPrivateId()).forEach(successor -> {
            for (Object edgeLabel : graph.getEdgeLabel(getPrivateId(), successor)) {
                if (edgeLabel instanceof SelectorLabel) {
                    result.add(((SelectorLabel) edgeLabel).getLabel());
                }
            }
            return true;
        });
        return result;
    }

    public NodeGraphElement getSelectorDestination(Graph graph, String selector) {
        NodeGraphElement result;
        TIntArrayList successors = graph.getSuccessorsOf(getPrivateId());
        for (int i=0; i<successors.size(); i++) {
            for (Object edgeLabel : graph.getEdgeLabel(getPrivateId(), successors.get(i))) {
                if (edgeLabel instanceof SelectorLabel && ((SelectorLabel) edgeLabel).hasLabel(selector)) {
                    // Found the corresponding selector edge
                    return new NodeGraphElement(successors.get(i));
                }
            }
        }
        return null;
    }

    public static NodeGraphElement getNullNode(Graph graph) {
        for (int i=0; i<graph.size(); i++) {
            NodeLabel label = graph.getNodeLabel(i);
            if (label instanceof Type) {
                Type type = (Type) label;
                if (type.matches(Types.NULL)) {
                    return new NodeGraphElement(i);
                }
            }
        }
        return null;
    }
}
