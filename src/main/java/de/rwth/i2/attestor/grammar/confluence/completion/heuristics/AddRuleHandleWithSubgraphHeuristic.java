package de.rwth.i2.attestor.grammar.confluence.completion.heuristics;

import de.rwth.i2.attestor.grammar.AbstractionOptions;
import de.rwth.i2.attestor.grammar.CollapsedHeapConfiguration;
import de.rwth.i2.attestor.grammar.Grammar;
import de.rwth.i2.attestor.grammar.confluence.CriticalPair;
import de.rwth.i2.attestor.grammar.util.SimpleIterator;
import de.rwth.i2.attestor.graph.BasicNonterminal;
import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.SelectorLabel;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import de.rwth.i2.attestor.graph.heap.HeapConfigurationBuilder;
import de.rwth.i2.attestor.graph.heap.Matching;
import de.rwth.i2.attestor.graph.heap.matching.AbstractMatchingChecker;
import de.rwth.i2.attestor.graph.morphism.MorphismOptions;
import de.rwth.i2.attestor.util.Pair;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.array.TIntArrayList;

import java.util.*;

/**
 * If one of the fully abstracted heap configurations of the critical pair contains a handle and a subgraph that is
 * also contained in the other fully abstracted heap configuration then we add a new rule mapping the handle to a heap
 * configuration without this subgraph to make the critical pair strongly joinable if possible.
 *
 * The algorithm for a given critical pair works the following:
 * For all nonterminals N in HC1:
 *   1: Compute G1 = HC1 \ N
 *   2: For all embeddings: G2 = embedding(HC2, G1)  (if no embedding try next nonterminal)
 *   3: Compute RHS = HC2 \ G2     (N => RHS is new rule)
 *
 * Repeat with HC1 and HC2 switched
 *
 * We only generate rules where the RHS is growing and a single component
 *
 *
 *
 */
public class AddRuleHandleWithSubgraphHeuristic extends CompletionRuleAddingHeuristic {
    private final MorphismOptions morphismOptions;

    public AddRuleHandleWithSubgraphHeuristic() {
        morphismOptions = new AbstractionOptions()
                .setAdmissibleAbstraction(false)
                .setAdmissibleConstants(false)
                .setAdmissibleMarkings(false);
    }

    @Override
    Iterable<Collection<Pair<Nonterminal, HeapConfiguration>>> addNewRules(CriticalPair criticalPair) {
        HeapConfiguration hc1 = criticalPair.getCanonical1();
        HeapConfiguration hc2 = criticalPair.getCanonical2();
        return new Iterable<Collection<Pair<Nonterminal, HeapConfiguration>>>() {
            @Override
            public Iterator<Collection<Pair<Nonterminal, HeapConfiguration>>> iterator() {
                TIntIterator hc1Nonterminals = hc1.nonterminalEdges().iterator();
                TIntIterator hc2Nonterminals = hc2.nonterminalEdges().iterator();
                return new SimpleIterator<Collection<Pair<Nonterminal, HeapConfiguration>>>() {
                    @Override
                    public Collection<Pair<Nonterminal, HeapConfiguration>> computeNext() {
                        Collection<Pair<Nonterminal, HeapConfiguration>> result = null;
                        while (result == null) {
                            if (hc1Nonterminals.hasNext()) {
                                // Choose rule left hand side from hc1
                                result = getRule(hc1, hc2, hc1Nonterminals.next());
                            } else if (hc2Nonterminals.hasNext()) {
                                // Choose rule left hand side from hc2 (switch hc1 & hc2)
                                result = getRule(hc2, hc1, hc2Nonterminals.next());
                            } else {
                                // No more possible rules
                                return null;
                            }
                        }
                        return result;
                    }
                };
            }
        };
    }

    private Collection<Pair<Nonterminal, HeapConfiguration>> getRule(HeapConfiguration hc1, HeapConfiguration hc2, int hc1Nonterminal) {
        // Determine if the nonterminal is collapsed in hc1
        TIntArrayList attachedNodes = hc1.attachedNodesOf(hc1Nonterminal);
        boolean collapsedNonterminal = false;
        for (int i = 0; i < attachedNodes.size(); i++) {
            if (attachedNodes.subList(0, i).contains(attachedNodes.get(i))) {
                // The nonterminal is connected to the same node twice
                collapsedNonterminal = true;
                break;
            }
        }

        // 1. Compute G1 = HC1 \ N
        HeapConfiguration g1 = hc1.clone().builder().removeNonterminalEdge(hc1Nonterminal).build();
        // Make all nodes external (so they don't get removed automatically later)
        HeapConfigurationBuilder g1Builder = g1.builder();
        TIntArrayList g1Nodes = g1.nodes();
        g1Nodes.forEach(node -> {
            g1Builder.setExternal(node);
            return true;
        });
        g1 = g1Builder.build();

        // 2. Compute matching = embedding(HC2, G1)
        AbstractMatchingChecker matchingChecker = hc2.getEmbeddingsOf(g1, morphismOptions);
        if (!matchingChecker.hasMatching()) {
            // No matching was found
            return null;
        }
        Matching matching = matchingChecker.getMatching();

        // 3. Compute RHS = HC2 \ matching
        Nonterminal tempNonterminal = new BasicNonterminal.Factory().create(null, g1.countExternalNodes(), new boolean[g1.countExternalNodes()]);
        HeapConfiguration rhs = hc2.clone().builder().replaceMatching(matching, tempNonterminal).build();  // Some nonterminal is required
        // Remove the nonterminal edge added by the replaceMatching method
        TIntArrayList nonterminalEdges = rhs.nonterminalEdges();
        for (int i = 0; i < nonterminalEdges.size(); i++) {
            if (rhs.labelOf(nonterminalEdges.get(i)) == tempNonterminal) {
                // Found the newly added nonterminal
                rhs = rhs.builder().removeNonterminalEdge(nonterminalEdges.get(i)).build();
                break;
            }
        }

        // 4. Remove isolated nodes
        rhs = removeIsolatedNodes(rhs);
        if (!isSingleComponent(rhs)) {
            // rhs must be a single component
            return null;
        }


        // Set external nodes
        HeapConfigurationBuilder rhsBuilder = rhs.builder();
        for (int i = 0; i < attachedNodes.size(); i++) {
            int rhsNode = matching.match(attachedNodes.get(i));
            if (!rhs.nodes().contains(rhsNode)) {
                // The node was removed, because it was isolated
                return null;
            }
            rhsBuilder.setExternal(rhsNode);
        }
        rhs = rhsBuilder.build();

        if (!isGrowingRule(rhs)) {
            // The rule is not growing
            return null;
        }

        Nonterminal nt = hc1.labelOf(hc1Nonterminal);
        return Collections.singleton(new Pair<>(nt, rhs));
    }

    /**
     * Sets the external nodes correctly and creates a collapsed heap configuration
     */
    private static CollapsedHeapConfiguration getCollapsedHeapConfiguration(HeapConfiguration rhs, TIntArrayList attachedNodes) {

    }

    private static HeapConfiguration removeIsolatedNodes(HeapConfiguration hc) {
        HeapConfigurationBuilder builder = hc.builder();
        TIntArrayList nodes = hc.nodes();
        for (int i = 0; i < nodes.size(); i++) {
            int node = nodes.get(i);
            if (hc.attachedNonterminalEdgesOf(node).isEmpty() && hc.predecessorNodesOf(node).isEmpty() && hc.selectorLabelsOf(node).isEmpty()) {
                builder.removeNode(node);
            }
        }
        return builder.build();
    }

    private static boolean isSingleComponent(HeapConfiguration hc) {
        if (hc.countNodes() == 0) {
            // There is no component
            return false;
        }
        // Convert to graph for easier computation
        Set<Integer> visitedNodes = new HashSet<>();
        Stack<Integer> unvisitedNodes = new Stack<>();
        unvisitedNodes.add(hc.nodes().get(0));
        while (!unvisitedNodes.isEmpty()) {
            int node = unvisitedNodes.pop();
            // Check if node was already visited
            if (!visitedNodes.contains(node)) {
                visitedNodes.add(node);
                Set<Integer> connectedNodes = new HashSet<>();

                // Calculate all nodes that are connected to node

                // 1. Connected through outgoing selectors
                for (SelectorLabel label: hc.selectorLabelsOf(node)) {
                    connectedNodes.add(hc.selectorTargetOf(node, label));
                }

                // 2. Connected through incoming selectors
                hc.predecessorNodesOf(node).forEach(predNode -> {
                    connectedNodes.add(predNode);
                    return true;
                });


                // 3. Connected through nonterminal
                hc.attachedNonterminalEdgesOf(node).forEach(ntEdge -> {
                    hc.attachedNodesOf(ntEdge).forEach(connectedNode -> {
                        connectedNodes.add(connectedNode);
                        return true;
                    });
                    return true;
                });

                connectedNodes.removeAll(visitedNodes);
                unvisitedNodes.addAll(connectedNodes);
            }
        }

        TIntArrayList nodes = hc.nodes();
        for (int i = 0; i < nodes.size(); i++) {
            if (!visitedNodes.contains(nodes.get(i))) {
                // The current node has never been visited -> is a separate component
                return false;
            }
        }

        return true;
    }

    private static boolean isGrowingRule(HeapConfiguration rhs) {
        if (rhs.countNonterminalEdges() > 1) {
            return true;
        }
        TIntArrayList nodes = rhs.nodes();
        for (int i = 0; i < nodes.size(); i++) {
            int node = nodes.get(i);
            if (!rhs.selectorLabelsOf(node).isEmpty() || !rhs.isExternalNode(node)) {
                return true;
            }
        }
        return false;

    }
}
