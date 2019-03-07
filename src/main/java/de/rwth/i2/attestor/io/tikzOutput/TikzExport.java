package de.rwth.i2.attestor.io.tikzOutput;

import de.rwth.i2.attestor.grammar.CollapsedHeapConfiguration;
import de.rwth.i2.attestor.grammar.Grammar;
import de.rwth.i2.attestor.grammar.confluence.CriticalPair;
import de.rwth.i2.attestor.grammar.confluence.jointMorphism.EdgeGraphElement;
import de.rwth.i2.attestor.grammar.confluence.jointMorphism.JointHeapConfiguration;
import de.rwth.i2.attestor.grammar.confluence.jointMorphism.NodeGraphElement;
import de.rwth.i2.attestor.grammar.confluence.main.ConfluenceTool;
import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import de.rwth.i2.attestor.graph.heap.internal.InternalHeapConfiguration;
import de.rwth.i2.attestor.graph.morphism.Graph;
import de.rwth.i2.attestor.main.Attestor;
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

    // TODO: Remove this test method
    public static void main(String args[]) {
        Grammar grammar = ConfluenceTool.parseGrammar("BT");
        try {
            TikzExport exporter = new TikzExport("test.tex");
            exporter.exportGrammar(grammar, true);
            exporter.finishExport();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

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


    private void addGrammarRule(String pgfPath, Nonterminal leftHandSide, CollapsedHeapConfiguration rightHandSide) { // TODO: Maye simple HeapConfiguration instead of CollapsedHeapConfiguration is enough
        HeapConfiguration handle = new InternalHeapConfiguration();
        // TODO: Create handle
        addHeapConfiguration(pgfPath+"/left hand side", handle, "left hand side");
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
        Graph jointHeapConfiguration = getGraph(criticalPair.getJointHeapConfiguration().getHeapConfiguration());
        StringBuilder criticalPairDebugTableEntryMacros = new StringBuilder();
        int i = 0;
        for (NodeGraphElement currentNode : NodeGraphElement.getNodes(jointHeapConfiguration)) {
            i++;
            String currentPath = pgfPath + "/entry " + String.valueOf(i);
            pgfSingleValues.add(new Pair<>(currentPath + "/node id", String.valueOf(currentNode.getPrivateId())));
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
        Graph graph = getGraph(hc);

        Collection<String> nodes = new ArrayList<>();
        for (NodeGraphElement nodeGraphElement : NodeGraphElement.getNodes(graph)) {
            int privateId = nodeGraphElement.getPrivateId();
            nodes.add(String.valueOf(privateId));
            String currentPath = pgfPath + "/nodes/" + String.valueOf(privateId);
            Type nodeType = (Type) graph.getNodeLabel(nodeGraphElement.getPrivateId());
            pgfSingleValues.add(new Pair<>(currentPath + "/type", nodeType.toString()));
            boolean isExternal = graph.isExternal(nodeGraphElement.getPrivateId());
            pgfSingleValues.add(new Pair<>(currentPath + "/is external", isExternal?"true":"false"));
            if (isExternal) {
                pgfSingleValues.add(new Pair<>(currentPath + "/external index", String.valueOf(graph.getExternalIndex(privateId))));
            }
        }
        pgfListValues.add(new Pair<>(pgfPath+ "/nodes", nodes));


        int selectorNumber = 0;
        Collection<String> nonterminals = new ArrayList<>();
        for (EdgeGraphElement currentEdge : EdgeGraphElement.getEdgesOfGraph(getGraph(hc))) {
            if (currentEdge.isSelector()) {
                selectorNumber++;
                String currentPath = pgfPath + "/selectors/" + selectorNumber;
                pgfSingleValues.add(new Pair<>(currentPath + "/from", String.valueOf(currentEdge.getPrivateId())));
                String selectorDestination = String.valueOf(currentEdge.getConnectedNodes(graph).get(1).getPrivateId());
                pgfSingleValues.add(new Pair<>(currentPath + "/to", selectorDestination));
                pgfSingleValues.add(new Pair<>(currentPath + "/label", currentEdge.getSelectorLabel()));
            } else {
                // Nonterminal edge TODO: set "isNew" argument correctly
                int privateId = currentEdge.getPrivateId();
                String currentPath = pgfPath + "/nonterminals/" + privateId;
                nonterminals.add(String.valueOf(privateId));
                int rank = graph.getSuccessorsOf(privateId).size();
                pgfSingleValues.add(new Pair<>(currentPath + "/rank", String.valueOf(rank)));
                Nonterminal nonterminal = (Nonterminal) graph.getNodeLabel(privateId);
                pgfSingleValues.add(new Pair<>(currentPath + "/label", nonterminal.getLabel()));
                TIntArrayList successors = graph.getSuccessorsOf(privateId);
                for (int i=0; i<rank; i++) {
                    String successor = String.valueOf(successors.get(i));
                    pgfSingleValues.add(new Pair<>(currentPath + "/tentacle/" + i, successor));
                }
            }
        }
        pgfSingleValues.add(new Pair<>(pgfPath + "/num selectors", String.valueOf(selectorNumber)));
        pgfListValues.add(new Pair<>(pgfPath + "/nonterminals", nonterminals));
    }

    private void writeCurrentReportToFile(String reportCommand) throws IOException {
        String indent = "  \\pgfkeyssetvalue{/test key/}{test}";
        StringBuilder result = new StringBuilder();
        // Start new scope
        result.append('{').append(System.lineSeparator());

        for (Pair<String, String> pgfKeyPair : pgfSingleValues) {
            result.append("  \\pgfkeyssetvalue{").append(pgfKeyPair.first()).append("}{")
                    .append(pgfKeyPair.second()).append('}').append(System.lineSeparator());
        }

        for (Pair<String, Collection<String>> pgfKeyPair : pgfListValues) {
            result.append("  \\pgfkeyssetvalue{").append(pgfKeyPair.first()).append("}{")
                    .append(String.join(",", pgfKeyPair.second())).append('}').append(System.lineSeparator());
        }

        result.append("  ").append(reportCommand).append(System.lineSeparator())
                .append('}').append(System.lineSeparator());
        writer.append(result);
    }

    private Graph getGraph(HeapConfiguration hc) {
        return (Graph) hc;
    }

}
