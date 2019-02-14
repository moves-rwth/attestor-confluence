package de.rwth.i2.attestor.grammar.confluence.jointMorphism;

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
    Map<GraphElement, Integer> hc1PubIdMap, hc2PubIdMap;
    HeapConfigurationContext context;
    HeapConfiguration jointHeapConfiguration;

    /**
     * Creates a new HeapConfiguration that is the union between the two HeapConfigurations in the context object.
     * The overlapping is specified by nodeOverlapping and edgeOverlapping.
     */
    public JointHeapConfiguration(HeapConfigurationContext context,
                                                         NodeOverlapping nodeOverlapping,
                                                         EdgeOverlapping edgeOverlapping) {
        this.context = context;
        Graph graph1 = context.getGraph1();
        Graph graph2 = context.getGraph2();
        // Create a new HeapConfigurationBuilder (by using getEmpty() on one of the existing heap configurations
        //     we are independent of the InternalHeapConfiguration class)
        HeapConfigurationBuilder builder = context.getHc1().getEmpty().builder();

        // Create maps to keep track of which GraphElement maps to which public ID in the new HeapConfiguration
        hc1PubIdMap = new HashMap<>();
        hc2PubIdMap = new HashMap<>();

        // 1. Add nodes
        // 1.1 Add all nodes from graph1
        addNodes(builder, graph1, hc1PubIdMap);

        // 1.2 Compute corresponding nodes in hc2PubIdMap
        computeCorrespondingElements(hc1PubIdMap, nodeOverlapping.getMapHC1toHC2(), hc2PubIdMap);

        // 1.3 Add remaining nodes that are exclusively in graph2
        addNodes(builder, graph2, hc2PubIdMap);


        // 2. Add edges
        // 2.1 Add all edges from graph1
        addEdges(builder, graph1, hc1PubIdMap);

        // 2.2 Compute corresponding edges
        computeCorrespondingElements(hc1PubIdMap, edgeOverlapping.getMapHC1toHC2(), hc2PubIdMap);

        // 2.3 Add remaining edges from graph2
        addEdges(builder, graph2, hc2PubIdMap);

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
                NodeGraphElement currentNode = new NodeGraphElement(privId);
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
     *
     * @param builder  The new edges are added to this builder.
     * @param graph  The graph from which the edges are added.
     * @param pubIdMap  Must already contain the mappings from all nodes to pubId.
     */
    private static void addEdges(HeapConfigurationBuilder builder, Graph graph, Map<GraphElement, Integer> pubIdMap) {
        // Iterate over all elements in the graph
        for (int privId = 0; privId < graph.size(); privId++) {
            NodeLabel nodeLabel = graph.getNodeLabel(privId);
            if (nodeLabel instanceof Type) {
                // The privId corresponds to a node -> find all outgoing selectors
                final int selectorSource = privId;  // Lambda expression below needs final variable
                graph.getSuccessorsOf(selectorSource).forEach(succId -> {
                    if (graph.getNodeLabel(succId) instanceof Type) {
                        // There is a selector edge from privId to succId
                        for (Object edgeLabel : graph.getEdgeLabel(selectorSource, succId)) {
                            if (edgeLabel instanceof SelectorLabel) {
                                // Get public ids (all nodes must already be present in the pubIdMap)
                                int pubIdFrom = pubIdMap.get(new NodeGraphElement(selectorSource));
                                int pubIdTo = pubIdMap.get(new NodeGraphElement(succId));
                                SelectorLabel selectorLabel = (SelectorLabel) edgeLabel;
                                EdgeGraphElement selectorElement = new EdgeGraphElement(selectorSource, selectorLabel.getLabel());
                                // If the selector does not yet exist -> add it
                                if (!pubIdMap.containsKey(selectorElement)) {
                                    // Selector does not yet exist -> add it to builder
                                    builder.addSelector(pubIdFrom, selectorLabel, pubIdTo);
                                    // Save selector in the map to remember that is was added
                                    pubIdMap.put(selectorElement, pubIdFrom);
                                }
                            }
                        }
                    }
                    // Check next successor
                    return true;
                });
            } else if (nodeLabel instanceof Nonterminal) {
                // The privId corresponds to a nonterminal -> add the edge
                EdgeGraphElement edgeElement = new EdgeGraphElement(privId, null);
                if (!pubIdMap.containsKey(edgeElement)) {
                    // Nonterminal edge not yet present -> Add it
                    Nonterminal nonterminal = (Nonterminal) nodeLabel;
                    TIntArrayList attachedNodes = graph.getSuccessorsOf(privId);
                    int nonterminalPubId = builder.addNonterminalEdgeAndReturnId(nonterminal, attachedNodes);
                    pubIdMap.put(edgeElement, nonterminalPubId);
                }
            } else {
                throw new IllegalArgumentException("Graph should only contain nodes with NodeLabel 'Type' or 'Nonterminal'");
            }
        }
    }

    /**
     * Adds entries to the valuePubIdMap that correspond to the overlapping graph elements from the overlapping that are already present in the keyPubIdMap
     *
     * @param keyPubIdMap  Maps GraphElements (that are keys in the overlapping) to public ids in the joint heap configuration (its key-set must contain at least the keys of the overlapping)
     * @param overlapping  Maps GraphElements from one graph to equivalent GraphElements of the other graph
     * @param valuePubIdMap Maps GraphElements (that are values in the overlapping) to public ids in the joint heap configuration (here we add corresponding entries that exist in the keyPubIdMap)
     */
    private static void computeCorrespondingElements(Map<GraphElement, Integer> keyPubIdMap, Map<GraphElement, GraphElement> overlapping, Map<GraphElement, Integer> valuePubIdMap) {
        for (Map.Entry<GraphElement, GraphElement> entry: overlapping.entrySet()) {
            int pubId = keyPubIdMap.get(entry.getKey());  // The value must be present
            // Add pubId in valuePubIdMap for the corresponding GraphElement
            valuePubIdMap.put(entry.getValue(), pubId);
        }
    }
}
