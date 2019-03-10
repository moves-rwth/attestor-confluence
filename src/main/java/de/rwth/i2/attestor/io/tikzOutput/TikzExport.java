package de.rwth.i2.attestor.io.tikzOutput;

import de.rwth.i2.attestor.grammar.CollapsedHeapConfiguration;
import de.rwth.i2.attestor.grammar.Grammar;
import de.rwth.i2.attestor.grammar.NamedGrammar;
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
 * Usage:
 *   - Constructor creates a new file at destinationLocation
 *   - Call exportCriticalPairs(...), exportGrammar(...),  exportHeapConfigurations(...) for all reports that should be included
 *   - At the end call finishExport(). After this no other operation is allowed.
 *   - It should be ensured that finishExport() is called at the end by using try / catch
 *
 * PGF Keys:
 * The data from every report entry is passed to LaTeX in form of PGF Keys.
 * Every value is technically a string, but the following documentation uses types to hint what values are expected.
 *
 * Global Keys:
 * /attestor/node type table  : (Bool) If set to true a table is printed for every report that shows the type of each node and whether it is a reduction tentacle. // TODO
 *
 * Grammar Report:
 *
 * /attestor/grammar name  : (String)  Name of the grammar that is used in the heading
 * /attestor/original rule idx  : (Int) Number used in the heading to distinguish the rules
 * /attestor/is original rule  : (Bool)
 * /attestor/collapsed rule idx : (Int) Only given if .../is original rule = false
 * /attestor/left hand side/<heap configuration>  : A handle for the nonterminal
 * /attestor/right hand side/<heap configuration> : The right hand side of the rule
 *
 * Critical Pair Report:
 *
 * /attestor/joinability result  : (String)   // TODO
 * /attestor/grammar name  : (String)   // TODO
 * /attestor/rule 1/original rule idx  : (Int)   // TODO
 * /attestor/rule 1/is original rule  : (Bool)   // TODO
 * /attestor/rule 1/collapsed rule idx  : (Int)   // TODO
 * /attestor/rule 2/original rule idx  : (Int)   // TODO
 * /attestor/rule 2/is original rule : (Bool)   // TODO
 * /attestor/rule 2/collapsed rule idx  : (Int)   // TODO
 *
 * /attestor/debug table/num table entries  : (Int)
 * /attestor/debug table/<idx>/node id : (Int) The Id of the node of this entry
 * /attestor/debug table/<idx>/type hc1 : (String) The node type of the node in hc1   // TODO
 * /attestor/debug table/<idx>/type hc2 : (String) The node type of the node in hc2   // TODO
 * /attestor/debug table/<idx>/reduc tent 1 : (Bool) The node is a reduction tentacle in hc1   // TODO
 * /attestor/debug table/<idx>/reduc tent 2 : (Bool) The node is a reduction tentacle in hc2   // TODO
 *
 * /attestor/joint graph/<heap configuration> : The overlapping heap configurations
 * /attestor/applied rule 1/<heap configuration> : Rule 1 applied in reverse to "joint graph"
 * /attestor/applied rule 2/<heap configuration> : Rule 2 applied in reverse to "joint graph"
 * /attestor/canonical 1/<heap configuration>  : Canonicalization of "applied rule 1"
 * /attestor/canonical 2/<heap configuration>  : Canonicalization of "applied rule 2"
 *
 * Heap Configuration Report:
 * /attestor/<heap configuration>
 *
 *
 * <heap configuration>:  // TODO: Add graph element involvement
 * nodes : (List[Int])  A list of the Ids of all nodes
 * nodes/<id>/type : (String) The type of the node
 * nodes/<id>/is external : (Bool)
 * nodes/<id>/external index : (Int) Only given for external nodes
 * nodes/<id>/selector targets : (List[Int]) A list of all nodes that are selector targets of node <id>
 * nodes/<id1>/selector/<id2>  : (List[String]) A list of all selector labels from node <id1> to <id2>
 *
 * nonterminals : (List[Int]) A list of the IDs of all nonterminals
 * nonterminals/<id>/label : (String)
 * nonterminals/<id>/rank : (Int)
 * nonterminals/<nterm-id>/tentacle targets : (List[Int]) A list of all nodes that are connected to the nonterminal <nterm-id>
 * nonterminals/<nterm-id>/tentacles/<node-id> : (List[Int]) A list of all tentacle edges between <nterm-id> and <node-id>
 *
 *
 * TODO: Add comments with the name & id of report elements
 *
 */
public class TikzExport {
    private BufferedWriter writer;
    // TODO: Chat that character encoding works on every platform
    private Collection<Pair<String, String>> pgfSingleValues;
    private Collection<Pair<String, Collection<String>>> pgfListValues;
    private final String BASE_PATH = "/attestor";

    public TikzExport(String destinationLocation, boolean printTableOfContents) throws IOException {
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
        if (printTableOfContents) {
            writer.write("\\title{Attestor Report}");
            writer.newLine();
            writer.write("\\maketitle");
            writer.newLine();
            writer.write("\\tableofcontents");
            writer.newLine();
        }
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
        writer.write("\\section{Critical Pair Report}");
        writer.newLine();
        int i = 0;
        // Start new scope
        for (CriticalPair criticalPair : criticalPairs) {
            i++;
            pgfSingleValues = new ArrayList<>();
            pgfListValues = new ArrayList<>();
            writer.write("% Critical Pair: " + i);
            writer.newLine();
            addCriticalPair(BASE_PATH, criticalPair);
            writeCurrentReportToFile("\\AttestorCriticalPairReport");
        }
        pgfSingleValues = null;
        pgfListValues = null;
    }

    public void exportGrammar(Grammar grammar, String name, boolean exportCollapsedRules) throws IOException {
        exportGrammar(new NamedGrammar(grammar, name), exportCollapsedRules);
    }

    public void exportGrammar(NamedGrammar grammar, boolean exportCollapsedRules) throws IOException {
        String grammarName = grammar.getGrammarName();
        writer.write(String.format("\\section{Grammar Report (%s)}", grammarName));
        writer.newLine();
        for (int originalRuleIdx=0; originalRuleIdx<grammar.numberOriginalRules(); originalRuleIdx++) {
            Pair<Nonterminal, HeapConfiguration> originalRule = grammar.getOriginalRule(originalRuleIdx);
            Nonterminal nonterminal = originalRule.first();
            HeapConfiguration originalRhs = originalRule.second();
            // Create new report element
            pgfSingleValues = new ArrayList<>();
            pgfListValues = new ArrayList<>();
            writer.write(String.format("%% Grammar %s Rule %d", grammarName, originalRuleIdx+1));
            writer.newLine();
            CollapsedHeapConfiguration collapsedHeapConfiguration = new CollapsedHeapConfiguration(originalRhs, originalRhs, null);
            pgfSingleValues.add(new Pair<>(BASE_PATH + "/grammar name", grammarName));
            pgfSingleValues.add(new Pair<>(BASE_PATH + "/is original rule", "true"));
            pgfSingleValues.add(new Pair<>(BASE_PATH + "/original rule idx", Integer.toString(originalRuleIdx+1)));
            addGrammarRule(BASE_PATH, nonterminal, collapsedHeapConfiguration);
            writeCurrentReportToFile("\\AttestorGrammarReport");
            if (exportCollapsedRules) {
                // Export collapsed rules
                int numberCollapsedRules = grammar.numberCollapsedRules(originalRuleIdx);
                for (int collapsedRuleIdx=0; collapsedRuleIdx < numberCollapsedRules; collapsedRuleIdx++) {
                    pgfSingleValues = new ArrayList<>();
                    pgfListValues = new ArrayList<>();
                    writer.write(String.format("%% Grammar %s Rule %d.%d", grammarName, originalRuleIdx+1, collapsedRuleIdx+1));
                    writer.newLine();
                    pgfSingleValues.add(new Pair<>(BASE_PATH + "/grammar name", grammarName));
                    pgfSingleValues.add(new Pair<>(BASE_PATH + "/is original rule", "false"));
                    pgfSingleValues.add(new Pair<>(BASE_PATH + "/original rule idx", Integer.toString(originalRuleIdx+1)));
                    pgfSingleValues.add(new Pair<>(BASE_PATH + "/collapsed rule idx", Integer.toString(collapsedRuleIdx+1)));
                    addGrammarRule(BASE_PATH, nonterminal, grammar.getCollapsedRhs(originalRuleIdx, collapsedRuleIdx));
                    writeCurrentReportToFile("\\AttestorGrammarReport");
                }
            }
        }
    }

    public void exportHeapConfigurations(Collection<HeapConfiguration> heapConfigurations) throws IOException {
        for (HeapConfiguration hc : heapConfigurations) {
            pgfSingleValues = new ArrayList<>();
            pgfListValues = new ArrayList<>();
            addHeapConfiguration(BASE_PATH, hc);
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

        addHeapConfiguration(pgfPath+"/left hand side", handleBuilder.build());
        addHeapConfiguration(pgfPath+"/right hand side", rightHandSide.getCollapsed());
    }

    private void addCriticalPair(String pgfPath, CriticalPair criticalPair) {
        pgfSingleValues.add(new Pair<>(pgfPath + "/joinability result", criticalPair.getJoinability().toString()));
        // TODO: Set ruleId
        pgfSingleValues.add(new Pair<>(pgfPath + "/rule 1 id", "TODO"));
        pgfSingleValues.add(new Pair<>(pgfPath + "/rule 2 id", "TODO"));
        addCriticalPairDebugTable(pgfPath + "/debug table", criticalPair);

        JointHeapConfiguration jointHeapConfiguration = criticalPair.getJointHeapConfiguration();
        addHeapConfiguration(pgfPath + "/joint graph", jointHeapConfiguration.getHeapConfiguration());
        addHeapConfiguration(pgfPath + "/applied rule 1", jointHeapConfiguration.getRule1Applied());
        addHeapConfiguration(pgfPath + "/applied rule 2", jointHeapConfiguration.getRule2Applied());
        addHeapConfiguration(pgfPath + "/canonical 1", jointHeapConfiguration.getCanonical1());
        addHeapConfiguration(pgfPath + "/canonical 2", jointHeapConfiguration.getCanonical2());
    }

    private void addCriticalPairDebugTable(String pgfPath, CriticalPair criticalPair) {
        HeapConfiguration hc = criticalPair.getJointHeapConfiguration().getHeapConfiguration();
        StringBuilder criticalPairDebugTableEntryMacros = new StringBuilder();
        TIntArrayList nodes = hc.nodes();
        pgfSingleValues.add(new Pair<>(pgfPath + "/num table entries", Integer.toString(nodes.size())));
        for (int idx=0; idx<nodes.size(); idx++) {
            int publicId = nodes.get(idx);
            String currentPath = pgfPath + "/" + Integer.toString(idx);
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
    private void addHeapConfiguration(String pgfPath, HeapConfiguration hc) {
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
                Collection<String> selectorsOfTarget = targetToSelectorsMap.computeIfAbsent(selectorTarget, ArrayList::new);
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
                Collection<String> tentacleList = attachedNodeToTentacleList.computeIfAbsent(attachedNode, ArrayList::new);
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
        // Note: We set those to null and not a new initialized list so an error is thrown if this method is called too early
        pgfSingleValues = null;
        pgfListValues = null;
    }

}
