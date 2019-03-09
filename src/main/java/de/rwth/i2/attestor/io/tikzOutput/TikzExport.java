package de.rwth.i2.attestor.io.tikzOutput;

import de.rwth.i2.attestor.grammar.CollapsedHeapConfiguration;
import de.rwth.i2.attestor.grammar.Grammar;
import de.rwth.i2.attestor.grammar.confluence.CriticalPair;
import de.rwth.i2.attestor.grammar.confluence.CriticalPairFinder;
import de.rwth.i2.attestor.grammar.confluence.jointMorphism.JointHeapConfiguration;
import de.rwth.i2.attestor.grammar.confluence.jointMorphism.NodeGraphElement;
import de.rwth.i2.attestor.grammar.confluence.main.ConfluenceTool;
import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.SelectorLabel;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import de.rwth.i2.attestor.graph.heap.HeapConfigurationBuilder;
import de.rwth.i2.attestor.graph.heap.internal.InternalHeapConfiguration;
import de.rwth.i2.attestor.graph.morphism.Graph;
import de.rwth.i2.attestor.main.Attestor;
import de.rwth.i2.attestor.main.scene.SceneObject;
import de.rwth.i2.attestor.types.GeneralType;
import de.rwth.i2.attestor.types.Type;
import de.rwth.i2.attestor.util.Pair;
import gnu.trove.list.array.TIntArrayList;
import jdk.nashorn.api.scripting.URLReader;

import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * All public methods should be inside a try / catch block and call finishExport() in a finally block
 * Usage:
 *   - Constructor creates a new file at destinationLocation
 *   - Call exportCriticalPairs(...), exportGrammar(...),  exportHeapConfigurations(...) for all reports that should be included
 *   - At the end call finishExport(). After this no other operation is allowed.
 *   - It should be ensured that finishExport() is called at the end by using
 *
 * TODO: Add comments with the name & id of report elements
 *
 */
public class TikzExport {
    private BufferedWriter writer;
    private int currentReportElementId = 0;
    // TODO: Chat that character encoding works on every platform
    private Collection<Pair<String, String>> pgfSingleValues;
    private Collection<Pair<String, Collection<String>>> pgfListValues;
    private final String BASE_PATH = "";

    public TikzExport(String destinationLocation) throws IOException {
        writer = new BufferedWriter(new FileWriter(destinationLocation));
        URL tikzMacrosFile = Attestor.class.getClassLoader().getResource("latexTemplates/tikzMacros.tex");
        BufferedReader reader = new BufferedReader(new URLReader(tikzMacrosFile));
        String line = reader.readLine();
        while (line != null) {
            writer.write(line);
            writer.newLine();
            line = reader.readLine();
        }
        writer.newLine();
        writer.write("\\begin{document}");
        writer.newLine();
    }

    /**
     * Must be called at the end.
     * Inserts \end{document} and closes the file stream
     */
    public void finishExport() throws IOException {
        // TODO: Need to ensure that writer.close() is called when append throws an exception?
        writer.append("\\end{document}");
        writer.close();
    }

    public void createPageBreak() throws IOException {
        writer.append("\\pagebreak");
        writer.newLine();
    }

    public void exportCriticalPairs(Collection<CriticalPair> criticalPairs) throws IOException {
        // Start new scope
        for (CriticalPair criticalPair : criticalPairs) {
            currentReportElementId++;
            pgfSingleValues = new ArrayList<>();
            pgfListValues = new ArrayList<>();
            addCriticalPair(BASE_PATH, criticalPair);
            writeCurrentReportToFile("\\AttestorCriticalPairReport");
        }
        pgfSingleValues = null;
        pgfListValues = null;
    }

    public void exportGrammar(Grammar grammar, boolean exportCollapsedRules) throws IOException {
        for (Nonterminal nonterminal : grammar.getAllLeftHandSides()) {
            for (HeapConfiguration heapConfiguration : grammar.getRightHandSidesFor(nonterminal)) {
                currentReportElementId++;
                pgfSingleValues = new ArrayList<>();
                pgfListValues = new ArrayList<>();
                CollapsedHeapConfiguration collapsedHeapConfiguration = new CollapsedHeapConfiguration(heapConfiguration, heapConfiguration, null);
                addGrammarRule(BASE_PATH, nonterminal, collapsedHeapConfiguration);
                writeCurrentReportToFile("\\AttestorGrammarReport");
            }
            if (exportCollapsedRules) {
                for (CollapsedHeapConfiguration collapsedHeapConfiguration : grammar.getCollapsedRightHandSidesFor(nonterminal)) {
                    currentReportElementId++;
                    pgfSingleValues = new ArrayList<>();
                    pgfListValues = new ArrayList<>();
                    addGrammarRule(BASE_PATH, nonterminal, collapsedHeapConfiguration);
                    writeCurrentReportToFile("\\AttestorGrammarReport");
                }
            }
        }
        pgfSingleValues = null;
        pgfListValues = null;
    }

    public void exportHeapConfigurations(Collection<HeapConfiguration> heapConfigurations) throws IOException {
        for (HeapConfiguration hc : heapConfigurations) {
            currentReportElementId++;
            pgfSingleValues = new ArrayList<>();
            pgfListValues = new ArrayList<>();
            addHeapConfiguration(BASE_PATH, hc, "standalone");
            writeCurrentReportToFile("\\AttestorHeapConfigurationReport");
        }
        pgfSingleValues = null;
        pgfListValues = null;
    }


    private void addGrammarRule(String pgfPath, Nonterminal leftHandSide, CollapsedHeapConfiguration rightHandSide) {

        // Create a handle (Graph containing just the nonterminal from the lefthandside with the external edges)
        HeapConfiguration heapConfiguration = new InternalHeapConfiguration();
        HeapConfigurationBuilder handleBuilder = heapConfiguration.builder();
        HeapConfiguration rhs = rightHandSide.getCollapsed();
        TIntArrayList originalToCollapsedExternalIndices = rightHandSide.getOriginalToCollapsedExternalIndices();
        TIntArrayList externalNodesCollapsedHC = rightHandSide.getCollapsed().externalNodes();
        TIntArrayList handleNodes = new TIntArrayList(externalNodesCollapsedHC.size());
        for (int handleExternalIdx = 0; handleExternalIdx < externalNodesCollapsedHC.size(); handleExternalIdx++) {
            Type nodeType = rhs.nodeTypeOf(externalNodesCollapsedHC.get(handleExternalIdx));
            // Add new external node to handle
            handleBuilder.addNodes(nodeType, 1, handleNodes).setExternal(handleNodes.get(handleExternalIdx));
        }
        // Find all tentacles that go to this node
        TIntArrayList attachedNodes = new TIntArrayList(leftHandSide.getRank());
        for (int tentacleIdx = 0; tentacleIdx < leftHandSide.getRank(); tentacleIdx++) {
            int collapsedExternalIdx;
            if (originalToCollapsedExternalIndices == null) {
                collapsedExternalIdx = tentacleIdx;
            } else {
                collapsedExternalIdx = originalToCollapsedExternalIndices.get(tentacleIdx);  // Note: External index of collapsed HC and handle is identical
            }
            attachedNodes.add(heapConfiguration.externalNodeAt(collapsedExternalIdx));
        }
        handleBuilder.addNonterminalEdge(leftHandSide, attachedNodes);

        addHeapConfiguration(pgfPath+"/left hand side", handleBuilder.build(), "left hand side");
        addHeapConfiguration(pgfPath+"/right hand side", rightHandSide.getCollapsed(), "right hand side");
    }

    private void addCriticalPair(String pgfPath, CriticalPair criticalPair) {
        pgfSingleValues.add(new Pair<>(pgfPath + "/joinability result", criticalPair.getJoinability().toString()));
        // TODO: Set ruleId
        pgfSingleValues.add(new Pair<>(pgfPath + "/rule 1 id", "TODO"));
        pgfSingleValues.add(new Pair<>(pgfPath + "/rule 2 id", "TODO"));
        addCriticalPairDebugTable(pgfPath + "/debug table", criticalPair);

        JointHeapConfiguration jointHeapConfiguration = criticalPair.getJointHeapConfiguration();
        addHeapConfiguration(pgfPath + "/joint graph", jointHeapConfiguration.getHeapConfiguration(), "joint graph");
        addHeapConfiguration(pgfPath + "/applied rule 1", jointHeapConfiguration.getRule1Applied(), "applied rule 1");
        addHeapConfiguration(pgfPath + "/applied rule 2", jointHeapConfiguration.getRule2Applied(), "applied rule 1");
        addHeapConfiguration(pgfPath + "/canonical 1", jointHeapConfiguration.getCanonical1(), "canonical 1");
        addHeapConfiguration(pgfPath + "/canonical 2", jointHeapConfiguration.getCanonical2(), "canonical 2");
    }

    private void addCriticalPairDebugTable(String pgfPath, CriticalPair criticalPair) {
        HeapConfiguration hc = criticalPair.getJointHeapConfiguration().getHeapConfiguration();
        StringBuilder criticalPairDebugTableEntryMacros = new StringBuilder();
        TIntArrayList nodes = hc.nodes();
        pgfSingleValues.add(new Pair<>(pgfPath + "/num table entries", Integer.toString(nodes.size())));
        for (int idx=0; idx<nodes.size(); idx++) {
            int publicId = nodes.get(idx);
            String currentPath = pgfPath + "/entries/" + Integer.toString(idx);
            pgfSingleValues.add(new Pair<>(currentPath + "/node id", Integer.toString(publicId)));
            pgfSingleValues.add(new Pair<>(currentPath + "/type hc1", "TODO"));
            pgfSingleValues.add(new Pair<>(currentPath + "/type hc2", "TODO"));
            pgfSingleValues.add(new Pair<>(currentPath + "/reduc tent 1", "TODO"));
            pgfSingleValues.add(new Pair<>(currentPath + "/reduc tent 2", "TODO"));
        }
    }

    /**
     * Draws a HeapConfiguration (must already be inside a tikzpicture environment)
     */
    private void addHeapConfiguration(String pgfPath, HeapConfiguration hc, String reportStep) {
        addNodesAndSelectorEdges(pgfPath, hc);
        addNonterminals(pgfPath, hc);
        // TODO: addVariables
    }

    private void addNodesAndSelectorEdges(String pgfPath, HeapConfiguration hc) {
        Collection<String> nodes = new ArrayList<>();
        hc.nodes().forEach(publicId -> {
            Type type = hc.nodeTypeOf(publicId);
            // Add node to pgf keys
            nodes.add(Integer.toString(publicId));
            String currentNodePath = pgfPath + "/nodes/" + publicId;
            pgfSingleValues.add(new Pair<>(currentNodePath + "/type", type.toString()));
            boolean isExternal = hc.isExternalNode(publicId);
            pgfSingleValues.add(new Pair<>(currentNodePath + "/is external", isExternal?"true":"false"));
            if (isExternal) {
                String externalIndex = Integer.toString(hc.externalIndexOf(publicId));
                pgfSingleValues.add(new Pair<>(currentNodePath + "/external index", externalIndex));
            }

            // Find all selector edges (Restructure them for easier processing)
            Map<Integer, Collection<String>> targetToSelectorsMap = new HashMap<>();
            for (SelectorLabel selectorLabel : hc.selectorLabelsOf(publicId)) {
                int selectorTarget = hc.selectorTargetOf(publicId, selectorLabel);
                Collection<String> selectorsOfTarget = getFromMapDefaultEmptyCollection(targetToSelectorsMap, selectorTarget);
                selectorsOfTarget.add(selectorLabel.getLabel());
            }
            // Add the neccessary keys for the selector edges
            Collection<String> selectorTargets = new ArrayList<>();
            for (Map.Entry<Integer, Collection<String>> entry : targetToSelectorsMap.entrySet()) {
                selectorTargets.add(Integer.toString(entry.getKey()));
                pgfListValues.add(new Pair<>(currentNodePath + "/selector/" + entry.getKey(), entry.getValue()));
            }
            pgfListValues.add(new Pair<>(currentNodePath + "/selector targets", selectorTargets));
            // Continue with the other nodes
            return true;
        });
        pgfListValues.add(new Pair<>(pgfPath + "/nodes", nodes));
    }

    private void addNonterminals(String pgfPath, HeapConfiguration hc) {
        Collection<String> nonterminals = new ArrayList<>();
        hc.nonterminalEdges().forEach(publicId -> {
            Nonterminal nonterminal = hc.labelOf(publicId);
            int rank = nonterminal.getRank();
            // Add nonterminal to pgf keys
            nonterminals.add(Integer.toString(publicId));
            String currentNonterminalPath = pgfPath + "/nonterminals/" + publicId;
            pgfSingleValues.add(new Pair<>(currentNonterminalPath + "/label", nonterminal.getLabel()));
            pgfSingleValues.add(new Pair<>(currentNonterminalPath + "/rank", Integer.toString(rank)));
            // Add tentacle edges
            Map<Integer, Collection<String>> attachedNodeToTentacleList = new HashMap<>();
            TIntArrayList attachedNodes = hc.attachedNodesOf(publicId);
            for (int idx=0; idx<rank; idx++) {
                int attachedNode = attachedNodes.get(idx);
                Collection<String> tentacleList = getFromMapDefaultEmptyCollection(attachedNodeToTentacleList, attachedNode);
                tentacleList.add(Integer.toString(idx));
            }
            Collection<String> tentacleTargets = new ArrayList<>();
            for (Map.Entry<Integer, Collection<String>> entry : attachedNodeToTentacleList.entrySet()) {
                tentacleTargets.add(Integer.toString(entry.getKey()));
                pgfListValues.add(new Pair<>(currentNonterminalPath + "/tentacles/" + entry.getKey(), entry.getValue()));
            }
            pgfListValues.add(new Pair<>(currentNonterminalPath + "/tentacle targets", tentacleTargets));
            // Continue with the other nonterminals
            return true;
        });
        pgfListValues.add(new Pair<>(pgfPath + "/nonterminals", nonterminals));
    }

    private Collection<String> getFromMapDefaultEmptyCollection(Map<Integer, Collection<String>> map, Integer key) {
        Collection<String> selectorsOfTarget;
        if (map.containsKey(key)) {
            selectorsOfTarget = map.get(key);
        } else {
            selectorsOfTarget = new ArrayList<>();
            map.put(key, selectorsOfTarget);
        }
        return selectorsOfTarget;
    }



    private void writeCurrentReportToFile(String reportCommand) throws IOException {
        // TODO: Escape characters (allow only spaces and A-Za-z0-9 and in paths allow "/")
        String indent = "    ";
        String lineSeparator = "%" + System.lineSeparator();
        StringBuilder result = new StringBuilder();
        // Start new scope
        result.append('{').append(lineSeparator);

        for (Pair<String, String> pgfKeyPair : pgfSingleValues) {
            result.append(indent).append("\\pgfkeyssetvalue{").append(pgfKeyPair.first()).append("}{")
                    .append(pgfKeyPair.second()).append('}').append(lineSeparator);
        }

        for (Pair<String, Collection<String>> pgfKeyPair : pgfListValues) {
            result.append(indent).append("\\pgfkeyssetvalue{").append(pgfKeyPair.first()).append("}{")
                    .append(String.join(",", pgfKeyPair.second())).append('}').append(lineSeparator);
        }

        result.append(indent).append(reportCommand).append(lineSeparator)
                .append('}').append(lineSeparator);
        writer.append(result);
    }

    private Graph getGraph(HeapConfiguration hc) {
        return (Graph) hc;
    }

}
