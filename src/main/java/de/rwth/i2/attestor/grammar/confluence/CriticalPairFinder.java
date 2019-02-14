package de.rwth.i2.attestor.grammar.confluence;

import de.rwth.i2.attestor.grammar.CollapsedHeapConfiguration;
import de.rwth.i2.attestor.grammar.Grammar;
import de.rwth.i2.attestor.grammar.confluence.jointMorphism.*;
import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import de.rwth.i2.attestor.util.Pair;

import java.util.*;


/**
 *
 * Computes the critical pairs of a grammar on construction.
 * Provides methods to access information about the critical pairs.
 *
 * The implemented algorithm to find the critical pairs is based on the work in: "Efficient Detection of Conflicts in
 * Graph-based Model Transformation" by Leen Lambers, Hartmut Ehrig & Fernando Orejas
 * TODO: How to correctly cite in javadoc
 *
 * @author Johannes Schulte
 */
public class CriticalPairFinder {

    final Grammar underlyingGrammar;
    final Set<CriticalPair> criticalPairs;

    public CriticalPairFinder(Grammar grammar) {
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
        HeapConfigurationContext context = new HeapConfigurationContext(hc1, hc2);


        for (Overlapping eOverlapping : EdgeOverlapping.getEdgeOverlapping(context)) {
            EdgeOverlapping edgeOverlapping = (EdgeOverlapping) eOverlapping;
            // Check if the current edgeOverlapping allows for compatible node overlappings
            if (edgeOverlapping.isEdgeOverlappingValid()) {
                for (Overlapping nodeOverlapping : NodeOverlapping.getNodeOverlapping(context, edgeOverlapping)) {
                    // Found a compatible overlapping

                    // 1. Compute the joint graph
                    JointHeapConfiguration jointHeapConfiguration = new JointHeapConfiguration(context,
                            (NodeOverlapping) nodeOverlapping, edgeOverlapping);

                    // 2. Compute fully abstracted heap configuration (apply r1 first)
                    // TODO

                    // 3. Compute fully abstracted heap configuration (apply r2 first)
                    // TODO

                    // 4. Check if both fully abstracted heap configurations are isomorphic
                    // TODO
                }
            }
        }
    }

}
