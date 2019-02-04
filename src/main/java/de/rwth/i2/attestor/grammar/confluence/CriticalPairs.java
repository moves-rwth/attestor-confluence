package de.rwth.i2.attestor.grammar.confluence;

import de.rwth.i2.attestor.grammar.CollapsedHeapConfiguration;
import de.rwth.i2.attestor.grammar.Grammar;
import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import de.rwth.i2.attestor.graph.morphism.Graph;
import de.rwth.i2.attestor.util.Pair;

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
        Graph hc1Graph = (Graph) hc1;
        Graph hc2Graph = (Graph) hc2;

        // TODO


    }

}
