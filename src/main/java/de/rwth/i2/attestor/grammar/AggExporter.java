package de.rwth.i2.attestor.grammar;

import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.SelectorLabel;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import de.rwth.i2.attestor.types.Type;
import gnu.trove.list.array.TIntArrayList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Exports a grammar to the input format for the agg tool.
 * Note: It switches RHS and LHS (because we care about backwards rule application)
 */
public class AggExporter implements GrammarExporter {
    private int idCounter;
    private int maxRank;
    private Map<SelectorLabel, String> mapSelectorToAggId;
    private Map<Nonterminal, String> mapNonterminalToAggId;
    private Map<Type, String> mapNodeTypeToAggId;
    private Map<Integer, String> mapTentacleToAggId;
    private Document document;

    @Override
    public void export(String directory, Grammar grammar) throws IOException {
        resetIdMaps();
        try {
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
            document = documentBuilder.newDocument();
            Element graphTransformationSystem = document.createElement("GraphTransformationSystem");
            graphTransformationSystem.setAttribute("ID", getNewId());
            graphTransformationSystem.setAttribute("directed", "true");
            graphTransformationSystem.setAttribute("name", "GraGra");
            graphTransformationSystem.setAttribute("parallel", "true");
            document.appendChild(graphTransformationSystem);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String getNewId() {
        idCounter++;
        return "I" + idCounter;
    }

    private void resetIdMaps() {
        idCounter = 0;
        maxRank = 0;
        mapSelectorToAggId = new HashMap<>();
        mapNonterminalToAggId = new HashMap<>();
        mapNodeTypeToAggId = new HashMap<>();
        mapTentacleToAggId = new HashMap<>();
    }

    private String getSelectorId(SelectorLabel selector) {
        if (!mapSelectorToAggId.containsKey(selector)) {
            mapSelectorToAggId.put(selector, getNewId());
        }
        return mapSelectorToAggId.get(selector);
    }

    private String getNonterminalId(Nonterminal nonterminal) {
        if (!mapNonterminalToAggId.containsKey(nonterminal)) {
            mapNonterminalToAggId.put(nonterminal, getNewId());
        }
        return mapNonterminalToAggId.get(nonterminal);
    }

    private String getNodeTypeId(Type nodeType) {
        if (!mapNodeTypeToAggId.containsKey(nodeType)) {
            mapNodeTypeToAggId.put(nodeType, getNewId());
        }
        return mapNodeTypeToAggId.get(nodeType);
    }

    private String getTentacleId(int tentacle) {
        if (!mapTentacleToAggId.containsKey(tentacle)) {
            mapTentacleToAggId.put(tentacle, getNewId());
        }
        return mapTentacleToAggId.get(tentacle);
    }


    private Node exportRule(Nonterminal nt, HeapConfiguration hc) {
        Element rule = document.createElement("Rule");
        String ruleId = getNewId();
        rule.setAttribute("ID", ruleId);
        rule.setAttribute("formula", "true");
        rule.setAttribute("name", ruleId);

        Map<Integer, String> mapHCNodeIds = new HashMap<>();
        Map<Integer, String> mapHandleNodeIds = new HashMap<>();

        // 1. Export LHS


        // 2. Export LHS
        // TODO

        return rule;
    }

    private Node exportNonterminalAsRHS(Nonterminal nt, TIntArrayList originalToCollapsedExternalIndices, Map<Integer, String> mapNodeIds) {
        Element graph = document.createElement("Graph");
        graph.setAttribute("ID", getNewId());
        graph.setAttribute("kind", "LHS");
        graph.setAttribute("name", "Left");

        // Add nonterminal
        Element newNode = document.createElement("Node");
        String nonterminalNodeId = getNewId();
        newNode.setAttribute("ID", nonterminalNodeId);
        newNode.setAttribute("type", getNonterminalId(nt));

        // Add tentacles
        // TODO:

        return graph;
    }

    private Node exportHeapConfigurationAsLHS(HeapConfiguration hc, Map<Integer, String> mapNodeIds) {
        Element graph = document.createElement("Graph");
        graph.setAttribute("ID", getNewId());
        graph.setAttribute("kind", "LHS");
        graph.setAttribute("name", "Left");

        // Add nodes
        hc.nodes().forEach(node -> {
            Element newNode = document.createElement("Node");
            String newNodeId = getNewId();
            mapNodeIds.put(node, newNodeId);
            newNode.setAttribute("ID", newNodeId);
            newNode.setAttribute("type", getNodeTypeId(hc.nodeTypeOf(node)));
            graph.appendChild(newNode);
            return true;
        });

        // Add nonterminals
        hc.nonterminalEdges().forEach(ntEdge -> {
            Element newNode = document.createElement("Node");
            String newNodeId = getNewId();
            newNode.setAttribute("ID", newNodeId);
            newNode.setAttribute("type", getNonterminalId(hc.labelOf(ntEdge)));
            graph.appendChild(newNode);

            TIntArrayList attachedNodes =  hc.attachedNodesOf(ntEdge);

            for (int i=0; i< attachedNodes.size(); i++) {
                Element newTentacle = document.createElement("Edge");
                newTentacle.setAttribute("ID", getNewId());
                newTentacle.setAttribute("source", newNodeId);
                newTentacle.setAttribute("target", mapNodeIds.get(attachedNodes.get(i)));
                newTentacle.setAttribute("type", getTentacleId(i));
                graph.appendChild(newTentacle);
            }

            return true;
        });

        // Add selectors
        hc.nodes().forEach(source -> {
            for (SelectorLabel selector : hc.selectorLabelsOf(source)) {
                int target = hc.selectorTargetOf(source, selector);
                Element newSelector = document.createElement("Edge");
                newSelector.setAttribute("ID", getNewId());
                newSelector.setAttribute("source", mapNodeIds.get(source));
                newSelector.setAttribute("target", mapNodeIds.get(target));
                newSelector.setAttribute("type", getSelectorId(selector));
                graph.appendChild(newSelector);
            }
            return true;
        });

        return graph;
    }
}
