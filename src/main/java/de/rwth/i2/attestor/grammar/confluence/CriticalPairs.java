package de.rwth.i2.attestor.grammar.confluence;

import de.rwth.i2.attestor.grammar.Grammar;
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
        List<Pair<Nonterminal, HeapConfiguration>> individualGrammarRules = new ArrayList<>();
        for (Nonterminal nonterminal : this.underlyingGrammar.getAllLeftHandSides()) {
            for (HeapConfiguration heapConfiguration : this.underlyingGrammar.getRightHandSidesFor(nonterminal)) {
                individualGrammarRules.add(new Pair<>(nonterminal, heapConfiguration));
            }
        }

        // 2. Iterate over all pairs of individual grammar rules and add the critical pairs for each pair
        for (int i = 0; i < individualGrammarRules.size(); i++) {
            for (int j = i; j < individualGrammarRules.size(); j++) {
                Pair<Nonterminal, HeapConfiguration> r1 = individualGrammarRules.get(i);
                Pair<Nonterminal, HeapConfiguration> r2 = individualGrammarRules.get(j);
                addCriticalPairsForRule(r1, r2);
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
    private void addCriticalPairsForRule(Pair<Nonterminal, HeapConfiguration> r1,
                                         Pair<Nonterminal, HeapConfiguration> r2) {
        HeapConfiguration hc1 = r1.second();
        HeapConfiguration hc2 = r2.second();

        Queue<JointMorphism> remainingJointMorphisms = new ArrayDeque<>();
        // Initialize with all joint morphisms that share exactly one node between hc1 and hc2
        remainingJointMorphisms.addAll(new JointMorphism(hc1, hc2).getFollowingJointMorphisms());
        while (!remainingJointMorphisms.isEmpty()) {
            JointMorphism currentJointMorphism = remainingJointMorphisms.remove();
            switch (currentJointMorphism.isJointMorphismCompatible(hc1, hc2)) {
                case COMPATIBLE:
                    criticalPairs.add(new CriticalPair(r1.first(), hc1,
                            r2.first(), hc2, currentJointMorphism));
                case NOT_COMPATIBLE_YET:  // Note: Also applies to case "COMPATIBLE" NO BREAK!!!
                    // Add following joint morphisms
                    remainingJointMorphisms.addAll(currentJointMorphism.getFollowingJointMorphisms());
                    break;
                case INCOMPATIBLE:
                    // Do nothing
                    break;
            }
        }

    }

}
