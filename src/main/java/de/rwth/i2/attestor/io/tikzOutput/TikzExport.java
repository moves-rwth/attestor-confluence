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
import jdk.nashorn.api.scripting.URLReader;

import java.io.*;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;

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
    private int indentLevel = 0;  // The current indentation level
    private int indentSpaces = 2; // The number of spaces per indentation level
    private int currentReportElementId = 0;
    // TODO: Chat that character encoding works on every platform

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
        for (CriticalPair criticalPair : criticalPairs) {
            currentReportElementId++;
            writer.append(getCriticalPairMacro(criticalPair));
        }
    }

    public void exportGrammar(Grammar grammar, boolean exportCollapsedRules) throws IOException {
        for (Nonterminal nonterminal : grammar.getAllLeftHandSides()) {
            for (HeapConfiguration heapConfiguration : grammar.getRightHandSidesFor(nonterminal)) {
                currentReportElementId++;
                CollapsedHeapConfiguration collapsedHeapConfiguration = new CollapsedHeapConfiguration(heapConfiguration, heapConfiguration, null);
                writer.append(getGrammarRuleMacro(nonterminal, collapsedHeapConfiguration));
            }
            if (exportCollapsedRules) {
                for (CollapsedHeapConfiguration collapsedHeapConfiguration : grammar.getCollapsedRightHandSidesFor(nonterminal)) {
                    currentReportElementId++;
                    writer.append(getGrammarRuleMacro(nonterminal, collapsedHeapConfiguration));
                }
            }
        }
    }

    public void exportHeapConfigurations(Collection<HeapConfiguration> heapConfigurations) throws IOException {
        for (HeapConfiguration hc : heapConfigurations) {
            currentReportElementId++;
            writer.append(getStandaloneHeapConfigurationMacro(hc));
        }
    }





    private StringBuilder getStandaloneHeapConfigurationMacro(HeapConfiguration heapConfiguration) {
        final String macroCommand = "standalone heap configuration";
        indentLevel++;
        String reportElementId = String.valueOf(currentReportElementId);
        StringBuilder heapConfigurationMacro = getHeapConfigurationMacro(heapConfiguration);
        indentLevel--;
        return macroBuilder(macroCommand, reportElementId, heapConfigurationMacro);
    }

    private StringBuilder getGrammarRuleMacro(Nonterminal leftHandSide, CollapsedHeapConfiguration rightHandSide) { // TODO: Maye simple HeapConfiguration instead of CollapsedHeapConfiguration is enough
        final String macroCommand = "grammar rule";
        indentLevel++;
        String reportElementId = String.valueOf(currentReportElementId);
        StringBuilder leftHandSideMacro = getLeftHandSideMacro(leftHandSide);
        StringBuilder rightHandSideMacro = getRightHandSideMacro(rightHandSide);
        indentLevel--;
        return macroBuilder(macroCommand, reportElementId, leftHandSideMacro, rightHandSideMacro);
    }

    private StringBuilder getLeftHandSideMacro(Nonterminal nonterminal) {
        final String macroCommand = "left hand side";
        HeapConfiguration handle = new InternalHeapConfiguration();
        // TODO: Create handle
        indentLevel++;
        StringBuilder handleMacro = getHeapConfigurationMacro(handle);
        indentLevel--;
        return macroBuilder(macroCommand, handleMacro);
    }

    private StringBuilder getRightHandSideMacro(CollapsedHeapConfiguration rightHandSide) {
        final String macroCommand = "right hand side";
        indentLevel++;
        StringBuilder rightHandSideMacro = getHeapConfigurationMacro(rightHandSide.getCollapsed());
        indentLevel--;
        return macroBuilder(macroCommand, rightHandSideMacro);
    }


    private StringBuilder getCriticalPairMacro(CriticalPair criticalPair) {
        final String macroCommand = "critical pair";
        indentLevel++;
        String reportElementId = String.valueOf(currentReportElementId);
        String joinabilityResult = criticalPair.getJoinability().toString();
        StringBuilder criticalPairDebugTable = getCriticalPairDebugTableMacro(criticalPair);
        StringBuilder jointGraph = getHeapConfigurationMacro(criticalPair.getJointHeapConfiguration().getHeapConfiguration());
        StringBuilder appliedRule1 = getHeapConfigurationMacro(criticalPair.getJointHeapConfiguration().getRule1Applied());
        StringBuilder appliedRule2 = getHeapConfigurationMacro(criticalPair.getJointHeapConfiguration().getRule2Applied());
        StringBuilder canonical1 = getHeapConfigurationMacro(criticalPair.getJointHeapConfiguration().getCanonical1());
        StringBuilder canonical2 = getHeapConfigurationMacro(criticalPair.getJointHeapConfiguration().getCanonical2());
        String rule1ID = "TODO";
        String rule2ID = "TODO";
        indentLevel--;
        return macroBuilder(macroCommand, reportElementId, joinabilityResult, rule1ID, rule2ID, jointGraph, appliedRule1, appliedRule2, canonical1, canonical2, criticalPairDebugTable);
    }

    private StringBuilder getCriticalPairDebugTableMacro(CriticalPair criticalPair) {
        Graph jointHeapConfiguration = getGraph(criticalPair.getJointHeapConfiguration().getHeapConfiguration());
        final String macroCommand = "critical pair debug table";
        indentLevel++;
        StringBuilder criticalPairDebugTableEntryMacros = new StringBuilder();
        for (NodeGraphElement currentNode : NodeGraphElement.getNodes(jointHeapConfiguration)) {
            criticalPairDebugTableEntryMacros.append(getCriticalPairDebugTableEntryMacro(criticalPair.getJointHeapConfiguration(), currentNode));
        }
        indentLevel--;
        return macroBuilder(macroCommand, criticalPairDebugTableEntryMacros);
    }

    private StringBuilder getCriticalPairDebugTableEntryMacro(JointHeapConfiguration jointHeapConfiguration, NodeGraphElement nodeJointGraph) {
        final String macroCommand = "critical pair debug table entry";
        String nodeId = String.valueOf(nodeJointGraph.getPrivateId());
        String typeHC1 = "TODO";
        String typeHC2 = "TODO";
        String reductionTentacle1 = "TODO";
        String reductionTentacle2 = "TODO";
        return macroBuilder(macroCommand, nodeId, typeHC1, typeHC2, reductionTentacle1, reductionTentacle2);
    }




    /**
     * Draws a HeapConfiguration (must already be inside a tikzpicture environment)
     */
    private StringBuilder getHeapConfigurationMacro(HeapConfiguration hc) {
        final String macroCommand = "heap configuration";
        Graph graph = getGraph(hc);
        indentLevel++;
        StringBuilder nodes = new StringBuilder();
        for (NodeGraphElement currentNode : NodeGraphElement.getNodes(getGraph(hc))) {
            nodes.append(getNodeMacro(graph, currentNode));
        }
        StringBuilder selectorEdgeMacros = new StringBuilder();
        StringBuilder hyperedgeMacros = new StringBuilder();
        StringBuilder hyperedgeTentacleMacros = new StringBuilder();
        for (EdgeGraphElement currentEdge : EdgeGraphElement.getEdgesOfGraph(getGraph(hc))) {
            if (currentEdge.isSelector()) {
                // Selector edge
                selectorEdgeMacros.append(getSelectorEdgeMacro(graph, currentEdge));
            } else {
                // Nonterminal edge TODO: set "isNew" argument correctly
                hyperedgeMacros.append(getNonterminalMacro(graph, currentEdge, false));
                int rank = graph.getSuccessorsOf(currentEdge.getPrivateId()).size();
                for (int i=0; i<rank; i++) {
                    hyperedgeTentacleMacros.append(getTentacleEdgeMacro(graph, currentEdge, i));
                }
            }
        }
        indentLevel--;
        return macroBuilder(macroCommand, nodes, selectorEdgeMacros, hyperedgeMacros, hyperedgeTentacleMacros);
    }

    private StringBuilder getSelectorEdgeMacro(Graph graph, EdgeGraphElement selectorEdge) {
        final String macroCommand = "selector edge";
        String sourceNode = String.valueOf(selectorEdge.getPrivateId());
        String selectorLabel = selectorEdge.getSelectorLabel();  // TODO: Remove invalid characters. Only a-zA-Z0-9 allowed
        return macroBuilder(macroCommand, sourceNode, selectorLabel);
    }

    private StringBuilder getTentacleEdgeMacro(Graph graph, EdgeGraphElement nonterminal, int tentacleIdx) {
        final String macroCommand = "tentacle edge";
        String nonterminalId = String.valueOf(nonterminal.getPrivateId());
        String tentacleIdxString = String.valueOf(tentacleIdx);
        return macroBuilder(macroCommand, nonterminalId, tentacleIdxString);
    }

    private StringBuilder getNodeMacro(Graph graph, NodeGraphElement nodeGraphElement) {
        if (graph.isExternal(nodeGraphElement.getPrivateId())) {
            return getExternalNodeMacro(graph, nodeGraphElement);
        } else {
            return getInternalNodeMacro(graph, nodeGraphElement);
        }
    }

    private StringBuilder getExternalNodeMacro(Graph graph, NodeGraphElement nodeGraphElement) {
        final String macroCommand = "external node";
        String externalIndex = String.valueOf(graph.getExternalIndex(nodeGraphElement.getPrivateId()));
        String nodeId = String.valueOf(nodeGraphElement.getPrivateId());
        return macroBuilder(macroCommand, nodeId, externalIndex);
    }

    private StringBuilder getInternalNodeMacro(Graph graph, NodeGraphElement nodeGraphElement) {
        final String macroCommand = "internal node";
        String nodeId = String.valueOf(nodeGraphElement.getPrivateId());
        return macroBuilder(macroCommand, nodeId);
    }

    private StringBuilder getNonterminalMacro(Graph graph, EdgeGraphElement nonterminal, boolean isNew) {
        String macroCommand;
        if (isNew) {
            macroCommand = "new nonterminal";
        } else {
            macroCommand = "old nonterminal";
        }
        String nonterminalId = String.valueOf(nonterminal.getPrivateId());
        Nonterminal nonterminalLabel = (Nonterminal) graph.getNodeLabel(nonterminal.getPrivateId());
        String nonterminalName = nonterminalLabel.getLabel();
        return macroBuilder(macroCommand, nonterminalId, nonterminalName);
    }



    private StringBuilder macroBuilder(String macroCommand, CharSequence... arguments) {
        // TODO: No new line for simple String arguments. If only strings put whole macro in one line
        char[] indentationSpaces = new char[indentLevel*indentSpaces];
        Arrays.fill(indentationSpaces, ' ');
        StringBuilder result = new StringBuilder().append(indentationSpaces).append("\\pgfkeys{/attestor export/").append(macroCommand).append('=');
        int i=0;
        for (CharSequence arg : arguments) {
            i++;
            if (arg instanceof String || arg.length() == 0) {
                // We assume that Strings are small enough to be printed in one line
                result.append('{').append(arg).append('}');
            } else {
                // All other CharSequences are probably longer and are therefore printed in new lines
                result.append(System.lineSeparator()).append(indentationSpaces).append('{')
                        .append(" % ").append(macroCommand).append(" arg #").append(i).append(System.lineSeparator())
                        .append(arg)
                        .append(indentationSpaces).append('}');
            }

        }
        return result.append('}').append(System.lineSeparator());
    }

    private Graph getGraph(HeapConfiguration hc) {
        return (Graph) hc;
    }

}
