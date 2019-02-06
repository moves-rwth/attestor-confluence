package de.rwth.i2.attestor.grammar.confluence;

import de.rwth.i2.attestor.grammar.CollapsedHeapConfiguration;
import de.rwth.i2.attestor.grammar.Grammar;
import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.SelectorLabel;
import de.rwth.i2.attestor.graph.digraph.NodeLabel;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import de.rwth.i2.attestor.graph.heap.HeapConfigurationBuilder;
import de.rwth.i2.attestor.graph.heap.internal.InternalHeapConfiguration;
import de.rwth.i2.attestor.graph.heap.internal.InternalHeapConfigurationBuilder;
import de.rwth.i2.attestor.graph.morphism.Graph;
import de.rwth.i2.attestor.types.Type;
import de.rwth.i2.attestor.util.Pair;
import gnu.trove.list.array.TIntArrayList;

import java.security.InvalidParameterException;
import java.util.*;


/**
 *
 * Computes the critical pairs of a grammar on construction.
 * Provides methods to access information about the critical pairs.
 *
 * The implemented algorithm to find the critical pairs is based on the work in: "Efficient Detection of Conflicts in
 * Graph-based Model Transformation" by Leen Lambers, Hartmut Ehrig & Fernando Orejas
 * TODO: How to correctly cite resources in javadoc
 *
 * @author Johannes Schulte
 */
public class CriticalPairs {

    final Grammar underlyingGrammar;
    final Set<CriticalPair> criticalPairs;

    public CriticalPairs(Grammar grammar) {
        this.underlyingGrammar = grammar;
        this.criticalPairs = new HashSet<>();
        computeAllCriticalPairs();
    }

    private void computeAllCriticalPairs() {
        Set<CriticalPair> criticalPairs = new HashSet<>();
        // Add critical pairs for all combinations of rules

        // 1. Create a list with all *individual* grammar rules
        List<Pair<Nonterminal, CollapsedHeapConfiguration>> individualGrammarRules = new ArrayList<>();
        for (Nonterminal nonterminal : this.underlyingGrammar.getAllLeftHandSides()) {
            for (CollapsedHeapConfiguration heapConfiguration : this.underlyingGrammar.getCollapsedRightHandSidesFor(nonterminal)) {
                individualGrammarRules.add(new Pair<>(nonterminal, heapConfiguration));
            }
        }

        // 2. Iterate over all pairs of individual grammar rules and add the critical pairs for each pair
        for (int i = 0; i < individualGrammarRules.size(); i++) {
            for (int j = i; j < individualGrammarRules.size(); j++) {
                Pair<Nonterminal, CollapsedHeapConfiguration> r1 = individualGrammarRules.get(i);
                Pair<Nonterminal, CollapsedHeapConfiguration> r2 = individualGrammarRules.get(j);
                addCriticalPairsForCollapsedRule(r1, r2);
            }
        }
    }



    /**
     * This method computes all possible jointly surjective morphisms g1, g2 such that (g1: l1 -> s, g2: l2 -> s)
     * for the two right hand sides (l1, l2) of the rules r1, r2.
     * For each of these morphisms we check if it induces a critical pair.
     *
     * @param r1 The first rule
     * @param r2 The second rule
     */
    private void addCriticalPairsForCollapsedRule(Pair<Nonterminal, CollapsedHeapConfiguration> r1,
                                         Pair<Nonterminal, CollapsedHeapConfiguration> r2) {
        HeapConfiguration hc1 = r1.second().getCollapsed();
        HeapConfiguration hc2 = r2.second().getCollapsed();
        if (!(hc1 instanceof Graph) || !(hc2 instanceof  Graph)) {
            throw new IllegalArgumentException("Right side of rule is not of type 'Graph'");
        }
        Graph hc1Graph = createBipartiteGraph(hc1);
        Graph hc2Graph = createBipartiteGraph(hc2);

        TIntArrayList nodesHc1 = new TIntArrayList(hc1.countNodes());
        TIntArrayList edgesHc1 = new TIntArrayList(hc1Graph.size() - hc1.countNodes());
        TIntArrayList nodesHc2 = new TIntArrayList(hc2.countNodes());
        TIntArrayList edgesHc2 = new TIntArrayList(hc2Graph.size() - hc2.countNodes());

    }


    /**
     * Calculates the private ids of edges and nodes.
     * @param hc The input heap configuration
     * @return A pair of two TIntArrayList objects. The first contains the private ids of the nodes and the second
     * element contains the private ids of the edges
     */
    private Pair<TIntArrayList, TIntArrayList> getNodesAndEdges(InternalHeapConfiguration hc) {
        TIntArrayList nodes = new TIntArrayList(hc.countNodes());
        TIntArrayList edges = new TIntArrayList(hc.size() - hc.countNodes());
        for (int node = 0; node > hc.size(); node++) {
            NodeLabel label = hc.getNodeLabel(node);
            if (label instanceof Type) {
                nodes.add(node);
            } else if (label instanceof Nonterminal || label instanceof SelectorLabel) {
                // We don't include variable edges here, because they should not appear in RHS
                edges.add(node);
            } else {
                throw new InvalidParameterException("InternalHeapConfiguration contains unrecognized label");
            }
        }
        return new Pair<>(nodes, edges);
    }

    /**
     * Creates a copy of an InternalHeapConfiguration where all selector edges are replaced by nonterminal edges of rank
     * 2. These edges can be distinguished from the original nonterminal edges because the label of these selectors
     * edges is SelectorLabelAsNonterminal. The type of the original nonterminals stays BasicNonterminal.
     */
    private Graph createBipartiteGraph(HeapConfiguration hc) {
        HeapConfigurationBuilder builder = hc.clone().builder();
        hc.nodes().forEach(node -> {
            for (SelectorLabel label : hc.selectorLabelsOf(node)) {
                // 1. Remove the current selector
                builder.removeSelector(node, label);
                // 2. Reinsert as a bipartite selector edge
                int target = hc.selectorTargetOf(node, label);
                builder.addSelectorBipartite(node, label, target);
            }
            return true;
        });
        return (Graph) builder.build();
    }

}
