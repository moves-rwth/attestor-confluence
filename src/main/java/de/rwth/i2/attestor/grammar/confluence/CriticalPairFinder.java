package de.rwth.i2.attestor.grammar.confluence;

import de.rwth.i2.attestor.grammar.AbstractionOptions;
import de.rwth.i2.attestor.grammar.CollapsedHeapConfiguration;
import de.rwth.i2.attestor.grammar.Grammar;
import de.rwth.i2.attestor.grammar.canonicalization.CanonicalizationHelper;
import de.rwth.i2.attestor.grammar.canonicalization.CanonicalizationStrategy;
import de.rwth.i2.attestor.grammar.canonicalization.EmbeddingCheckerProvider;
import de.rwth.i2.attestor.grammar.canonicalization.GeneralCanonicalizationStrategy;
import de.rwth.i2.attestor.grammar.canonicalization.defaultGrammar.DefaultCanonicalizationHelper;
import de.rwth.i2.attestor.grammar.confluence.jointMorphism.*;
import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import de.rwth.i2.attestor.graph.heap.HeapConfigurationBuilder;
import de.rwth.i2.attestor.graph.heap.Matching;
import de.rwth.i2.attestor.graph.morphism.Graph;
import de.rwth.i2.attestor.graph.morphism.MorphismOptions;
import de.rwth.i2.attestor.graph.morphism.checkers.VF2IsomorphismChecker;
import de.rwth.i2.attestor.util.Pair;
import gnu.trove.list.array.TIntArrayList;

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
 *
 * TODO: Add option to early abort if critical pairs already Strong joinable or weak joinable
 *
 * @author Johannes Schulte
 */
public class CriticalPairFinder {

    private CriticalPair.Joinability joinabilityResult;
    final private Collection<CriticalPair> criticalPairs;
    final private Grammar underlyingGrammar;
    final private CanonicalizationStrategy canonicalizationStrategy;
    final private VF2IsomorphismChecker checker;

    public CriticalPairFinder(Grammar grammar) {
        this.underlyingGrammar = grammar;
        this.criticalPairs = new ArrayList<>();
        this.checker = new VF2IsomorphismChecker();
        this.joinabilityResult = CriticalPair.Joinability.STRONGLY_JOINABLE;
        MorphismOptions options = new AbstractionOptions()
                .setAdmissibleAbstraction(false)
                .setAdmissibleConstants(false)
                .setAdmissibleMarkings(false);

        EmbeddingCheckerProvider provider = new EmbeddingCheckerProvider(options);
        CanonicalizationHelper canonicalizationHelper = new DefaultCanonicalizationHelper(provider);
        canonicalizationStrategy = new GeneralCanonicalizationStrategy(grammar, canonicalizationHelper);
        computeAllCriticalPairs();
    }

    private void computeAllCriticalPairs() {
        // Add critical pairs for all combinations of rules

        // 1. Create a list with all *individual* grammar rules
        List<Pair<Nonterminal, CollapsedHeapConfiguration>> individualGrammarRules = new ArrayList<>();
        for (Nonterminal nonterminal : this.underlyingGrammar.getAllLeftHandSides()) {
            // Add collapsed rules
            for (CollapsedHeapConfiguration collapsedHeapConfiguration : this.underlyingGrammar.getCollapsedRightHandSidesFor(nonterminal)) {
                individualGrammarRules.add(new Pair<>(nonterminal, collapsedHeapConfiguration));
            }

            // Add original rules
            // TODO: Are the original rules never contained in the collapsed rules?
            for (HeapConfiguration heapConfiguration : this.underlyingGrammar.getRightHandSidesFor(nonterminal)) {
                individualGrammarRules.add(new Pair<>(nonterminal, HeapConfigurationContext.convertToCollapsedHeapConfiguration(heapConfiguration)));
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
        Nonterminal nt1 = r1.first();
        Nonterminal nt2 = r2.first();
        CollapsedHeapConfiguration hc1 = r1.second();
        CollapsedHeapConfiguration hc2 = r2.second();
        HeapConfigurationContext context = new HeapConfigurationContext(hc1, hc2);


        for (Overlapping eOverlapping : EdgeOverlapping.getEdgeOverlapping(context)) {
            EdgeOverlapping edgeOverlapping = (EdgeOverlapping) eOverlapping;
            // Check if the current edgeOverlapping allows for compatible node overlappings
            if (edgeOverlapping.isEdgeOverlappingValid()) {
                for (Overlapping nOverlapping : NodeOverlapping.getNodeOverlapping(edgeOverlapping)) {
                    // Found a compatible overlapping
                    NodeOverlapping nodeOverlapping = (NodeOverlapping) nOverlapping;

                    // Check that the rule applications are not independent (They should share at least one internal node)
                    if (!nodeOverlapping.isNodeOverlappingIndependent()) {
                        // 1. Compute the joint graph
                        JointHeapConfiguration jointHeapConfiguration = new JointHeapConfiguration(edgeOverlapping, nodeOverlapping, nt1, nt2);
                        HeapConfiguration hc = jointHeapConfiguration.getHeapConfiguration();

                        // 2. Compute fully abstracted heap configuration (apply r1 first)
                        HeapConfiguration fullyAbstracted1 = canonicalizationStrategy.canonicalize(jointHeapConfiguration.getRule1Applied());

                        // 3. Compute fully abstracted heap configuration (apply r2 first)
                        HeapConfiguration fullyAbstracted2 = canonicalizationStrategy.canonicalize(jointHeapConfiguration.getRule2Applied());

                        // 4. Check if both fully abstracted heap configurations are isomorphic (and therefore joinable)
                        CriticalPair.Joinability joinability = null;
                        if (fullyAbstracted1.nodes().equals(fullyAbstracted2.nodes())) {
                            // Both fully abstracted heap configurations contain the nodes
                            // Check if track morphism defines the isomorphism (strong joinable)
                            HeapConfiguration fullyAbstracted1Track = setExternalNodesAccordingToIds(fullyAbstracted1);
                            HeapConfiguration fullyAbstracted2Track = setExternalNodesAccordingToIds(fullyAbstracted2);

                            checker.run((Graph) fullyAbstracted1Track, (Graph) fullyAbstracted2Track);
                            if (checker.hasMorphism()) {
                                // Strongly joinable
                                joinability = CriticalPair.Joinability.STRONGLY_JOINABLE;
                            }
                        }
                        if (joinability == null) {
                            // The critical pair is not strongly joinable -> check if it is weakly joinable
                            // Check if there is ANY isomorphism
                            checker.run((Graph) fullyAbstracted1, (Graph) fullyAbstracted2);
                            if (checker.hasMorphism()) {
                                joinability = CriticalPair.Joinability.WEAKLY_JOINABLE;
                            } else {
                                joinability = CriticalPair.Joinability.NOT_JOINABLE;
                            }
                        }
                        criticalPairs.add(new CriticalPair(jointHeapConfiguration, joinability));
                        joinabilityResult = joinabilityResult.getCollectiveJoinability(joinability);
                    }
                }
            }
        }
    }

    private static HeapConfiguration setExternalNodesAccordingToIds(HeapConfiguration hc) {
        // Clone the HeapConfiguration
        HeapConfigurationBuilder builder = hc.clone().builder();
        // TODO: Assumes the ids in hc.nodes() are in ascending order. Is this ok?
        hc.nodes().forEach(hcId -> {
            builder.setExternal(hcId);
            return true;
        });
        return builder.build();
    }

    public Collection<CriticalPair> getCriticalPairs() {
        return criticalPairs;
    }

    public CriticalPair.Joinability getJoinabilityResult() {
        return joinabilityResult;
    }
}
