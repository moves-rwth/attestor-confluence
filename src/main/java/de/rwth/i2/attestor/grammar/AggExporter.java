package de.rwth.i2.attestor.grammar;

import de.rwth.i2.attestor.grammar.confluence.benchmark.BenchmarkRunner;
import de.rwth.i2.attestor.grammar.confluence.main.ConfluenceTool;
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
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileOutputStream;
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
    private Map<SelectorLabel, String> mapSelectorToAggId;
    private Map<Nonterminal, String> mapNonterminalToAggId;
    private Map<Type, String> mapNodeTypeToAggId;
    private Map<Integer, String> mapTentacleToAggId;
    private Document document;

    public static void main(String[] args) {
        exportPredefinedGrammar("SLList");
        exportPredefinedGrammar("DLList");
        exportPredefinedGrammar("BT");
        exportSidGrammar("InTree");
        exportSidGrammar("InTreeLinkedLeaves");
        exportSidGrammar("LinkedTree1");
        exportSidGrammar("LinkedTree2");
        exportSidGrammar("SimpleDLL");
    }

    private static void exportPredefinedGrammar(String name) {
        Grammar grammar = ConfluenceTool.parsePredefinedGrammar(name);
        exportGrammar(grammar, name);
    }

    private static void exportSidGrammar(String name) {
        try {
            exportGrammar(BenchmarkRunner.getSeparationLogicGrammar(name), name);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }


    private static void exportGrammar(Grammar grammar, String name) {
        AggExporter  exporter = new AggExporter();
        exporter.export(name+".ggx", grammar);
    }

    @Override
    public void export(String filePath, Grammar grammar) {
        resetIdMaps();
        try {
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
            document = documentBuilder.newDocument();
            Element documentElement = document.createElement("Document");
            documentElement.setAttribute("version", "1.0");
            Element graphTransformationSystem = document.createElement("GraphTransformationSystem");
            setTaggedValuesForGraphTransformationSystem(graphTransformationSystem);
            graphTransformationSystem.setAttribute("ID", getNewId());
            graphTransformationSystem.setAttribute("directed", "true");
            graphTransformationSystem.setAttribute("name", "GraGra");
            graphTransformationSystem.setAttribute("parallel", "true");

            for (Nonterminal nt : grammar.getAllLeftHandSides()) {
                for (HeapConfiguration hc : grammar.getRightHandSidesFor(nt)) {
                    graphTransformationSystem.appendChild(exportRule(nt, hc));
                }
                for (CollapsedHeapConfiguration collapsedHeapConfiguration : grammar.getCollapsedRightHandSidesFor(nt)) {
                    graphTransformationSystem.appendChild(exportCollapsedRule(nt, collapsedHeapConfiguration));
                }
            }

            graphTransformationSystem.appendChild(getAllAggTypes());

            documentElement.appendChild(graphTransformationSystem);
            document.appendChild(documentElement);
            Transformer tr = TransformerFactory.newInstance().newTransformer();
            tr.setOutputProperty(OutputKeys.INDENT, "yes");
            tr.setOutputProperty(OutputKeys.METHOD, "xml");
            tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            // send DOM to file
            tr.transform(new DOMSource(document),
                    new StreamResult(new FileOutputStream(filePath)));
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
        TIntArrayList originalToCollapsed = new TIntArrayList(nt.getRank());
        for (int i = 0; i < nt.getRank(); i++) {
            originalToCollapsed.add(i);
        }
        CollapsedHeapConfiguration collapsedHeapConfiguration = new CollapsedHeapConfiguration(hc, hc, originalToCollapsed);
        return exportCollapsedRule(nt, collapsedHeapConfiguration);
    }

    private Node exportCollapsedRule(Nonterminal nt, CollapsedHeapConfiguration collapsedHeapConfiguration) {
        Element rule = document.createElement("Rule");
        String ruleId = getNewId();
        rule.setAttribute("ID", ruleId);
        rule.setAttribute("formula", "true");
        rule.setAttribute("name", ruleId);

        HeapConfiguration hc = collapsedHeapConfiguration.getCollapsed();

        // 1. Export heap configuration
        Map<Integer, String> mapHCNodeIds = new HashMap<>();
        rule.appendChild(exportHeapConfigurationAsLHS(hc, mapHCNodeIds));

        // 2. Export handle
        Map<Integer, String> mapHandleNodeIds = new HashMap<>();
        rule.appendChild(exportNonterminalAsRHS(nt, collapsedHeapConfiguration.getOriginalToCollapsedExternalIndices(), hc, mapHandleNodeIds));

        // 3. Add morphism (mapping handle to heap configuration)
        Element morphism = document.createElement("Morphism");
        morphism.setAttribute("name", ruleId);
        for (Map.Entry<Integer, String> handleNodeEntry : mapHandleNodeIds.entrySet()) {
            String hcAggId = mapHCNodeIds.get(handleNodeEntry.getKey());
            Element mapping = document.createElement("Mapping");
            mapping.setAttribute("image", handleNodeEntry.getValue());
            mapping.setAttribute("orig", hcAggId);
            morphism.appendChild(mapping);
        }
        rule.appendChild(morphism);
        setTaggedValuesForRule(rule);

        return rule;
    }

    /**
     *
     * @param nt
     * @param originalToCollapsedExternalIndices
     * @param hc : Still need heap configuration to figure out id and type of external nodes
     * @param mapNodeIds : Maps public id to AGG ID (empty at start)
     * @return
     */
    private Node exportNonterminalAsRHS(Nonterminal nt, TIntArrayList originalToCollapsedExternalIndices, HeapConfiguration hc, Map<Integer, String> mapNodeIds) {
        Element graph = document.createElement("Graph");
        graph.setAttribute("ID", getNewId());
        graph.setAttribute("kind", "LHS");
        graph.setAttribute("name", "Left");

        // Add nonterminal
        Element newNonterminalNode = document.createElement("Node");
        String nonterminalAggId = getNewId();
        newNonterminalNode.setAttribute("ID", nonterminalAggId);
        newNonterminalNode.setAttribute("type", getNonterminalId(nt));
        graph.appendChild(newNonterminalNode);

        // Add external nodes and tentacles
        for (int tentacleId = 0; tentacleId < originalToCollapsedExternalIndices.size(); tentacleId++) {
            // Add external node if it does not already exist
            int publicNodeId = hc.externalNodeAt(originalToCollapsedExternalIndices.get(tentacleId));
            String connectedNodeAggId;
            if (mapNodeIds.containsKey(publicNodeId)) {
                connectedNodeAggId = mapNodeIds.get(publicNodeId);
            } else {
                connectedNodeAggId = getNewId();
                Element connectedNode = document.createElement("Node");
                connectedNode.setAttribute("ID", connectedNodeAggId);
                connectedNode.setAttribute("type", getNodeTypeId(hc.nodeTypeOf(publicNodeId)));
                mapNodeIds.put(publicNodeId, connectedNodeAggId);
                graph.appendChild(connectedNode);
            }

            // Add tentacle edge
            Element newTentacle = document.createElement("Edge");
            newTentacle.setAttribute("ID", getNewId());
            newTentacle.setAttribute("source", nonterminalAggId);
            newTentacle.setAttribute("target", connectedNodeAggId);
            newTentacle.setAttribute("type", getTentacleId(tentacleId));
            graph.appendChild(newTentacle);
        }
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

    private Node getAllAggTypes() {
        Element types = document.createElement("Types");

        // 1. Tentacle Types
        for (Map.Entry<Integer, String> entry : mapTentacleToAggId.entrySet()) {
            Element edgeType = document.createElement("EdgeType");
            edgeType.setAttribute("ID", entry.getValue());
            edgeType.setAttribute("abstract", "false");
            edgeType.setAttribute("name", entry.getKey() + "%:SOLID_LINE:java.awt.Color[r=0,g=0,b=0]:[EDGE]:");
            types.appendChild(edgeType);
        }

        // 2. Selector Types
        for (Map.Entry<SelectorLabel, String> entry : mapSelectorToAggId.entrySet()) {
            Element edgeType = document.createElement("EdgeType");
            edgeType.setAttribute("ID", entry.getValue());
            edgeType.setAttribute("abstract", "false");
            edgeType.setAttribute("name", entry.getKey().getLabel() + "%:SOLID_LINE:java.awt.Color[r=0,g=0,b=0]:[EDGE]:");
            types.appendChild(edgeType);
        }

        // 3. Nonterminal Types
        for (Map.Entry<Nonterminal, String> entry : mapNonterminalToAggId.entrySet()) {
            Element nodeType = document.createElement("NodeType");
            nodeType.setAttribute("ID", entry.getValue());
            nodeType.setAttribute("abstract", "false");
            nodeType.setAttribute("name", entry.getKey().getLabel() + "%:RECT:java.awt.Color[r=0,g=0,b=0]:[NODE]");
            types.appendChild(nodeType);
        }

        // 4. Node Types
        for (Map.Entry<Type, String> entry : mapNodeTypeToAggId.entrySet()) {
            Element nodeType = document.createElement("NodeType");
            nodeType.setAttribute("ID", entry.getValue());
            nodeType.setAttribute("abstract", "false");
            nodeType.setAttribute("name", entry.getKey() + "%:CIRCLE:java.awt.Color[r=0,g=0,b=0]:[NODE]");
            types.appendChild(nodeType);
        }

        return types;
    }

    private void setTaggedValuesForGraphTransformationSystem(Element graphTransformationSystem) {
        Element attrHandler = getAttributedTag("TaggedValue", "Tag", "AttrHandler", "TagValue", "Java Expr");
        attrHandler.appendChild(getAttributedTag("TaggedValue", "Tag", "Package", "TagValue", "java.lang"));
        attrHandler.appendChild(getAttributedTag("TaggedValue", "Tag", "Package", "TagValue", "java.util"));
        graphTransformationSystem.appendChild(attrHandler);
        graphTransformationSystem.appendChild(getAttributedTag("TaggedValue", "Tag", "CSP", "TagValue", "true"));
        graphTransformationSystem.appendChild(getAttributedTag("TaggedValue", "Tag", "injective", "TagValue", "true"));
        graphTransformationSystem.appendChild(getAttributedTag("TaggedValue", "Tag", "dangling", "TagValue", "true"));
        graphTransformationSystem.appendChild(getAttributedTag("TaggedValue", "Tag", "identification", "TagValue", "true"));
        graphTransformationSystem.appendChild(getAttributedTag("TaggedValue", "Tag", "NACs", "TagValue", "true"));
        graphTransformationSystem.appendChild(getAttributedTag("TaggedValue", "Tag", "PACs", "TagValue", "true"));
        graphTransformationSystem.appendChild(getAttributedTag("TaggedValue", "Tag", "GACs", "TagValue", "true"));
        graphTransformationSystem.appendChild(getAttributedTag("TaggedValue", "Tag", "breakAllLayer", "TagValue", "true"));
        graphTransformationSystem.appendChild(getAttributedTag("TaggedValue", "Tag", "showGraphAfterStep", "TagValue", "true"));
        graphTransformationSystem.appendChild(getAttributedTag("TaggedValue", "Tag", "TypeGraphLevel", "TagValue", "DISABLED"));
    }

    private void setTaggedValuesForRule(Element rule) {
        rule.appendChild(getAttributedTag("TaggedValue", "Tag", "layer", "TagValue", "0"));
        rule.appendChild(getAttributedTag("TaggedValue", "Tag", "priority", "TagValue", "0"));
    }

    private Element getAttributedTag(String tag, String ...attributes) {
        Element node = document.createElement(tag);
        for (int i=0; i+1 <attributes.length; i += 2) {
            node.setAttribute(attributes[i], attributes[i+1]);
        }
        return node;
    }
}
