package de.rwth.i2.attestor.io.tikzOutput;

import de.rwth.i2.attestor.grammar.CollapsedHeapConfiguration;
import de.rwth.i2.attestor.grammar.Grammar;
import de.rwth.i2.attestor.grammar.NamedGrammar;
import de.rwth.i2.attestor.grammar.confluence.CriticalPair;
import de.rwth.i2.attestor.grammar.confluence.CriticalPairFinder;
import de.rwth.i2.attestor.grammar.confluence.Joinability;
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
 * /attestor/joinability result  : (String)
 * /attestor/grammar name  : (String)
 * /attestor/rule 1/original rule idx  : (Int)
 * /attestor/rule 1/is original rule  : (Bool)
 * /attestor/rule 1/collapsed rule idx  : (Int)
 * /attestor/rule 2/original rule idx  : (Int)
 * /attestor/rule 2/is original rule : (Bool)
 * /attestor/rule 2/collapsed rule idx  : (Int)
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
 * <heap configuration>:
 * nodes : (List[Int])  A list of the Ids of all nodes
 * nodes/<id>/type : (String) The type of the node
 * nodes/<id>/is external : (Bool)
 * nodes/<id>/is primitive : (Bool)
 * nodes/<id>/external indices : (List[Int]) Only given for external nodes (Multiple values in case of a collapsed heap)
 * nodes/<id>/selector targets : (List[Int]) A list of all nodes that are selector targets of node <id>
 * nodes/<id1>/selectors/<id2>/labels  : (List[String]) A list of all selector labels from node <id1> to <id2>
 * nodes/<id1>/selectors/<id2>/has reverse  : (Bool) If true there is also a selector edge in the other direction
 *
 * nonterminals : (List[Int]) A list of the IDs of all nonterminals
 * nonterminals/<id>/label : (String)
 * nonterminals/<id>/rank : (Int)
 * nonterminals/<nterm-id>/tentacle targets : (List[Int]) A list of all nodes that are connected to the nonterminal <nterm-id>
 * nonterminals/<nterm-id>/tentacles/<node-id>/indices : (List[Int]) A list of all tentacle edges between <nterm-id> and <node-id>
 * nonterminals/<nterm-id>/tentacles/<node-id>/reduction tentacle : (String) one of "all" "none" "mixed"
 *
 * <heap configuration>: (only for heap configurations of a critical pair)  TODO
 * nodes/<id>/critical pair involvement : (String) one of "hc1", "hc2", "both", "new"
 * nodes/<id>/is rule1 external : (Bool) If set to true this node is external in rule 1 (only applies for critical pair joint heap configuration)
 * nodes/<id>/rule1 external indices: (List[Int])
 * nodes/<id>/is rule2 external : (Bool) If set to true this node is external in rule 1 (only applies for critical pair joint heap configuration)
 * nodes/<id>/rule2 external indices: (List[Int])
 *
 *
 */
public class TikzExport {
    private BufferedWriter writer;
    // TODO: Check that character encoding works on every platform
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

    public void exportCriticalPairs(Collection<CriticalPair> criticalPairs, Joinability joinability) throws IOException {
        writer.write("\\section{Critical Pair Report}");
        writer.newLine();
        int i = 0;
        // Start new scope
        for (CriticalPair criticalPair : criticalPairs) {
            if (criticalPair.getJoinability().getValue() <= joinability.getValue()) {
                i++;
                pgfSingleValues = new ArrayList<>();
                pgfListValues = new ArrayList<>();
                writer.write("% Critical Pair: " + i);
                writer.newLine();
                addCriticalPair(BASE_PATH, criticalPair);
                writeCurrentReportToFile("\\AttestorCriticalPairReport");
            }
        }
        pgfSingleValues = null;
        pgfListValues = null;
    }

    public void exportGrammar(Grammar grammar, String name, boolean exportCollapsedRules) throws IOException {
        exportGrammar(new NamedGrammar(grammar, name), exportCollapsedRules);
    }

    public void exportGrammar(NamedGrammar grammar, boolean exportCollapsedRules) throws IOException {
        String grammarName = grammar.getGrammarName();
        writer.write(String.format("\\section{Grammar Report (%s)}", escapeString(grammarName)));
        writer.newLine();
        for (int originalRuleIdx=0; originalRuleIdx<grammar.numberOriginalRules(); originalRuleIdx++) {
            Pair<Nonterminal, HeapConfiguration> originalRule = grammar.getOriginalRule(originalRuleIdx);
            Nonterminal nonterminal = originalRule.first();
            HeapConfiguration originalRhs = originalRule.second();
            // Create new report element
            pgfSingleValues = new ArrayList<>();
            pgfListValues = new ArrayList<>();
            writer.write(String.format("%% Grammar %s Rule %d", escapeString(grammarName), originalRuleIdx+1));
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
                    writer.write(String.format("%% Grammar %s Rule %d.%d", escapeString(grammarName), originalRuleIdx+1, collapsedRuleIdx+1));
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
        addHeapConfiguration(pgfPath+"/right hand side", rightHandSide);
    }

    private void addCriticalPair(String pgfPath, CriticalPair criticalPair) {
        Pair<Integer, Integer> r1 = criticalPair.getR1ID();
        Pair<Integer, Integer> r2 = criticalPair.getR2ID();
        pgfSingleValues.add(new Pair<>(pgfPath + "/joinability result", criticalPair.getJoinability().toString()));
        // Set rules
        pgfSingleValues.add(new Pair<>(pgfPath + "/rule 1/original rule idx", Integer.toString(r1.first() + 1)));
        pgfSingleValues.add(new Pair<>(pgfPath + "/rule 1/is original rule", r1.second()==null?"true":"false"));
        if (r1.second() != null) {
            pgfSingleValues.add(new Pair<>(pgfPath + "/rule 1/collapsed rule idx", Integer.toString(r1.second() + 1)));
        }
        pgfSingleValues.add(new Pair<>(pgfPath + "/rule 2/original rule idx", Integer.toString(r2.first()+1)));
        pgfSingleValues.add(new Pair<>(pgfPath + "/rule 2/is original rule", r2.second()==null?"true":"false"));
        if (r2.second() != null) {
            pgfSingleValues.add(new Pair<>(pgfPath + "/rule 2/collapsed rule idx", Integer.toString(r2.second() + 1)));
        }

        addCriticalPairDebugTable(pgfPath + "/debug table", criticalPair);

        JointHeapConfiguration jointHeapConfiguration = criticalPair.getJointHeapConfiguration();
        addHeapConfiguration(pgfPath + "/joint graph", jointHeapConfiguration.getHeapConfiguration());
        addHeapConfiguration(pgfPath + "/applied rule 1", criticalPair.getRule1Applied());
        addHeapConfiguration(pgfPath + "/applied rule 2", criticalPair.getRule2Applied());
        addHeapConfiguration(pgfPath + "/canonical 1", criticalPair.getCanonical1());
        addHeapConfiguration(pgfPath + "/canonical 2", criticalPair.getCanonical2());
    }

    private void addCriticalPairDebugTable(String pgfPath, CriticalPair criticalPair) {
        // TODO
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
    private void addHeapConfiguration(String pgfPath, CollapsedHeapConfiguration collapsedHc) {
        addNodesAndSelectorEdges(pgfPath, collapsedHc);
        addNonterminals(pgfPath, collapsedHc);
        // TODO: addVariables
    }

    private void addHeapConfiguration(String pgfPath, HeapConfiguration hc) {
        addHeapConfiguration(pgfPath, new CollapsedHeapConfiguration(hc, hc, null));
    }

    private void addNodesAndSelectorEdges(String pgfPath, CollapsedHeapConfiguration collapsedHc) {
        HeapConfiguration hc = collapsedHc.getCollapsed();
        Collection<String> nodes = new ArrayList<>();
        hc.nodes().forEach(publicId -> {
            Type type = hc.nodeTypeOf(publicId);
            // Add node to pgf keys
            nodes.add(Integer.toString(publicId));
            String currentNodePath = pgfPath + "/nodes/" + publicId;
            pgfSingleValues.add(new Pair<>(currentNodePath + "/type", type.toString()));
            boolean isExternal = hc.isExternalNode(publicId);
            pgfSingleValues.add(new Pair<>(currentNodePath + "/is external", isExternal?"true":"false"));
            boolean isPrimitive = type.isPrimitiveType();
            pgfSingleValues.add(new Pair<>(currentNodePath + "/is primitive", isPrimitive?"true":"false"));
            if (isExternal) {
                int collapsedExternalIdx = hc.externalIndexOf(publicId);
                TIntArrayList originalToCollapsedExternalIndices = collapsedHc.getOriginalToCollapsedExternalIndices();
                List<String> externalIdices = new ArrayList<>();
                if (originalToCollapsedExternalIndices == null) {
                    // Is original heap configuration
                    externalIdices.add(Integer.toString(collapsedExternalIdx));
                } else {
                    // Obtain original heap configuration indices
                    for (int idx=0; idx < originalToCollapsedExternalIndices.size(); idx++) {
                        if (originalToCollapsedExternalIndices.get(idx) == collapsedExternalIdx) {
                            externalIdices.add(Integer.toString(idx));
                        }
                    }
                }
                pgfListValues.add(new Pair<>(currentNodePath + "/external indices", externalIdices));
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
                int selectorTarget = entry.getKey();
                pgfListValues.add(new Pair<>(currentNodePath + "/selectors/" + selectorTarget + "/labels", entry.getValue()));
                List reverseSelectors = hc.selectorLabelsOf(selectorTarget);
                pgfSingleValues.add(new Pair<>(currentNodePath + "/selectors/" + selectorTarget + "/has reverse", reverseSelectors.isEmpty()?"false":"true"));
            }
            pgfListValues.add(new Pair<>(currentNodePath + "/selector targets", selectorTargets));
            // Continue with the other nodes
            return true;
        });
        pgfListValues.add(new Pair<>(pgfPath + "/nodes", nodes));
    }

    private void addNonterminals(String pgfPath, CollapsedHeapConfiguration collapsedHc) {
        HeapConfiguration hc = collapsedHc.getCollapsed();
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
            Map<Integer, Integer> numberReductionTentacles = new HashMap<>();
            TIntArrayList attachedNodes = hc.attachedNodesOf(publicId);
            for (int idx=0; idx<rank; idx++) {
                int attachedNode = attachedNodes.get(idx);
                Collection<String> tentacleList = attachedNodeToTentacleList.computeIfAbsent(attachedNode, ArrayList::new);
                tentacleList.add(Integer.toString(idx));
                if (nonterminal.isReductionTentacle(idx)) {
                    int oldNumberReductionTentacles = numberReductionTentacles.getOrDefault(attachedNode, 0);
                    numberReductionTentacles.put(attachedNode, oldNumberReductionTentacles + 1);
                }

            }
            Collection<String> tentacleTargets = new ArrayList<>();
            for (Map.Entry<Integer, Collection<String>> entry : attachedNodeToTentacleList.entrySet()) {
                tentacleTargets.add(Integer.toString(entry.getKey()));
                pgfListValues.add(new Pair<>(currentNonterminalPath + "/tentacles/" + entry.getKey() + "/indices", entry.getValue()));
                int numReductionTentacles = numberReductionTentacles.getOrDefault(entry.getKey(), 0);
                String reductionTentacle;
                if (numReductionTentacles == 0) {
                    reductionTentacle = "none";
                } else if (numReductionTentacles == entry.getValue().size()) {
                    reductionTentacle = "all";
                } else {
                    reductionTentacle = "mixed";
                }
                pgfSingleValues.add(new Pair<>(currentNonterminalPath + "/tentacles/" + entry.getKey() + "/reduction tentacle", reductionTentacle));
            }
            pgfListValues.add(new Pair<>(currentNonterminalPath + "/tentacle targets", tentacleTargets));
            // Continue with the other nonterminals
            return true;
        });
        pgfListValues.add(new Pair<>(pgfPath + "/nonterminals", nonterminals));
    }

    private void writeCurrentReportToFile(String reportCommand) throws IOException {
        String indent = "    ";
        String lineSeparator = "%" + System.lineSeparator();
        StringBuilder result = new StringBuilder();
        // Start new scope
        result.append('{').append(lineSeparator);

        for (Pair<String, String> pgfKeyPair : pgfSingleValues) {
            result.append(indent).append("\\pgfkeyssetvalue{").append(escapeString(pgfKeyPair.first())).append("}{")
                    .append(escapeString(pgfKeyPair.second())).append('}').append(lineSeparator);
        }

        for (Pair<String, Collection<String>> pgfKeyPair : pgfListValues) {
            result.append(indent).append("\\pgfkeyssetvalue{").append(escapeString(pgfKeyPair.first())).append("}{")
                    .append(escapeString(String.join(",", pgfKeyPair.second()))).append('}').append(lineSeparator);
        }

        result.append(indent).append(reportCommand).append(lineSeparator)
                .append('}').append(lineSeparator);
        writer.append(result);
        // Note: We set those to null and not a new initialized list so an error is thrown if this method is called too early
        pgfSingleValues = null;
        pgfListValues = null;
    }

    private String escapeString(String string) {
        return string.replaceAll("[^a-zA-Z0-9/, ]+", "");
    }

}
