package de.rwth.i2.attestor.grammar.confluence;

import de.rwth.i2.attestor.grammar.confluence.jointMorphism.EdgeJointMorphism;
import de.rwth.i2.attestor.grammar.confluence.jointMorphism.GraphElement;
import de.rwth.i2.attestor.grammar.confluence.jointMorphism.HeapConfigurationContext;
import de.rwth.i2.attestor.grammar.confluence.jointMorphism.NodeJointMorphism;
import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.SelectorLabel;
import de.rwth.i2.attestor.graph.digraph.NodeLabel;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import de.rwth.i2.attestor.graph.heap.HeapConfigurationBuilder;
import de.rwth.i2.attestor.graph.morphism.Graph;
import de.rwth.i2.attestor.types.Type;
import gnu.trove.list.array.TIntArrayList;

import java.util.HashMap;
import java.util.Map;

public class JointHeapConfiguration {
    Map<GraphElement, Integer> mapHC1, mapHC2;
    HeapConfigurationContext context;
    HeapConfiguration jointHeapConfiguration;

    /**
     * Creates a new HeapConfiguration that is the union between the two HeapConfigurations in the context object.
     * The overlapping is specified by nodeJointMorphism and edgeJointMorphism.
     */
    public JointHeapConfiguration(HeapConfigurationContext context,
                                                         NodeJointMorphism nodeJointMorphism,
                                                         EdgeJointMorphism edgeJointMorphism) {
        Graph graph1 = context.getGraph1();
        Graph graph2 = context.getGraph2();
        // Create a new HeapConfigurationBuilder (by using getEmpty() on one of the existing heap configurations
        //     we are independent of the InternalHeapConfiguration class)
        HeapConfigurationBuilder builder = context.getHc1().getEmpty().builder();

        // Create maps to keep track of which GraphElement maps to which public ID in the new HeapConfiguration
        mapHC1 = new HashMap<>();
        mapHC2 = new HashMap<>();

        // 1. Add nodes
        // 1.1 Add all nodes from graph1
        addNodes(builder, graph1, mapHC1);

        // 1.2 Compute corresponding nodes in mapHC2
        Map<GraphElement, GraphElement> mapHc1toHc2 = nodeJointMorphism.getMapL1toL2();
        for (Map.Entry<GraphElement, Integer> entry: mapHC1.entrySet()) {
            if (mapHc1toHc2.containsKey(entry.getKey())) {
                // The node in Hc1 corresponds to another node in Hc2
                mapHC2.put(mapHc1toHc2.get(entry.getKey()), entry.getValue());
            }
        }

        // 1.3 Add remaining nodes that are exclusively in graph2
        addNodes(builder, graph2, mapHC2);


        // 2. Add edges
        // 2.1 Add all edges from graph1
        addEdges(builder, graph1, mapHC1);

        // 2.2 Compute corresponding edges
        // TODO

        // 2.3 Add remaining edges from graph2
        // TODO: Add edges from both graphs

        // 3. Build the completed HeapConfiguration
        jointHeapConfiguration = builder.build();
    }

    public HeapConfiguration getHeapConfiguration() {
        return jointHeapConfiguration;
    }

    /**
     *  Adds nodes from graph that are not yet in the pubIdMap to the builder.
     *  It stores which graph nodes correspond to which pubId and saves the mapping to the pubIdMap
     */
    private static void addNodes(HeapConfigurationBuilder builder, Graph graph, Map<GraphElement, Integer> pubIdMap) {
        TIntArrayList nodeList = new TIntArrayList(1);
        for (int privId = 0; privId < graph.size(); privId++) {
            // Check if privId is a node
            NodeLabel nodeLabel = graph.getNodeLabel(privId);
            if (nodeLabel instanceof Type) {
                GraphElement currentNode = new GraphElement(privId, null);
                if (!pubIdMap.containsKey(currentNode)) {
                    // The node was not been added yet
                    nodeList.clear(1);
                    builder.addNodes((Type) nodeLabel, 1, nodeList);
                    pubIdMap.put(currentNode, nodeList.get(0));
                }
            }
        }
    }

    /**
     * Adds edges from graph that are not yet in the pubIdMap to the builder.
     * It stores which graph edges correspond to which pubId and saves the mapping in the pubIdMap
     */
    private static void addEdges(HeapConfigurationBuilder builder, Graph graph, Map<GraphElement, Integer> pubIdMap) {
        for (int privId = 0; privId < graph.size(); privId++) {
            NodeLabel nodeLabel = graph.getNodeLabel(privId);
            if (nodeLabel instanceof Type) {
                // The privId corresponds to a node -> check for outgoing selectors
                final int selectorSource = privId;  // Lambda expression below needs final variable
                graph.getSuccessorsOf(selectorSource).forEach(succId -> {
                    if (graph.getNodeLabel(succId) instanceof Type) {
                        // There is a selector edge from privId to succId
                        for (Object edgeLabel : graph.getEdgeLabel(selectorSource, succId)) {
                            if (edgeLabel instanceof SelectorLabel) {
                                // Add the selector with edgeLabel
                                // TODO
                            }
                        }
                    }
                    return true;
                });
                // TODO
            } else if (nodeLabel instanceof Nonterminal) {
                // The privId corresponds to a nonterminal -> add the edge
                // TODO
            } else {
                throw new IllegalArgumentException("Graph should only contain nodes with NodeLabel 'Type' or 'Nonterminal'");
            }
        }
    }

    /**
     *
     * @param keyPubIdMap  Maps GraphElements (that are keys in the overlapping) to public ids in the joint heap configuration
     * @param overlapping  Maps GraphElements from one graph to equivalent GraphElements of the other graph
     * @param valuePubIdMap Maps GraphElements (that are values in the overlapping) to public ids in the joint heap configuration
     */
    private static void computeCorrespondingElements(Map<GraphElement, Integer> keyPubIdMap, Map<GraphElement, GraphElement> overlapping, Map<GraphElement, Integer> valuePubIdMap) {
        // TODO
    }
}
