package de.rwth.i2.attestor.grammar.confluence;

import de.rwth.i2.attestor.grammar.CollapsedHeapConfiguration;
import de.rwth.i2.attestor.grammar.NamedGrammar;
import de.rwth.i2.attestor.grammar.confluence.jointMorphism.EdgeOverlapping;
import de.rwth.i2.attestor.grammar.confluence.jointMorphism.HeapConfigurationContext;
import de.rwth.i2.attestor.grammar.confluence.jointMorphism.JointHeapConfiguration;
import de.rwth.i2.attestor.grammar.confluence.jointMorphism.NodeOverlapping;
import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import de.rwth.i2.attestor.graph.heap.HeapConfigurationBuilder;
import de.rwth.i2.attestor.graph.heap.Matching;
import de.rwth.i2.attestor.graph.morphism.Graph;
import de.rwth.i2.attestor.graph.morphism.checkers.VF2IsomorphismChecker;
import de.rwth.i2.attestor.util.Pair;
import gnu.trove.list.array.TIntArrayList;

public class CriticalPair {
    private final NamedGrammar grammar;
    private final JointHeapConfiguration jointHeapConfiguration;
    private final Pair<Integer, Integer> r1ID, r2ID;
    private final HeapConfigurationContext context;
    private final Joinability joinability;

    public CriticalPair(NodeOverlapping nodeOverlapping, EdgeOverlapping edgeOverlapping, NamedGrammar grammar, Pair<Integer, Integer> r1ID, Pair<Integer, Integer> r2ID) {
        this.grammar = grammar;
        this.r1ID = r1ID;
        this.r2ID = r2ID;
        this.context = nodeOverlapping.getContext();
        VF2IsomorphismChecker checker = new VF2IsomorphismChecker();
        // 1. Compute the joint graph
        jointHeapConfiguration = new JointHeapConfiguration(edgeOverlapping, nodeOverlapping);

        // 2. Compute fully abstracted heap configuration (apply r1 first)
        HeapConfiguration fullyAbstracted1 = getCanonical1();

        // 3. Compute fully abstracted heap configuration (apply r2 first)
        HeapConfiguration fullyAbstracted2 = getCanonical2();

        // 4. Check if both fully abstracted heap configurations are isomorphic (and therefore joinable)
        Joinability joinability = null;
        if (fullyAbstracted1.nodes().equals(fullyAbstracted2.nodes())) {
            // Both fully abstracted heap configurations contain the nodes
            // Check if track morphism defines the isomorphism (strong joinable)
            HeapConfiguration fullyAbstracted1Track = setExternalNodesAccordingToIds(fullyAbstracted1);
            HeapConfiguration fullyAbstracted2Track = setExternalNodesAccordingToIds(fullyAbstracted2);

            checker.run((Graph) fullyAbstracted1Track, (Graph) fullyAbstracted2Track);
            if (checker.hasMorphism()) {
                // Strongly joinable
                joinability = Joinability.STRONGLY_JOINABLE;
            }
        }
        if (joinability == null) {
            // The critical pair is not strongly joinable -> check if it is weakly joinable
            // Check if there is ANY isomorphism
            checker.run((Graph) fullyAbstracted1, (Graph) fullyAbstracted2);
            if (checker.hasMorphism()) {
                joinability = Joinability.WEAKLY_JOINABLE;
            } else {
                joinability = Joinability.NOT_JOINABLE;
            }
        }

        this.joinability = joinability;
    }

    public JointHeapConfiguration getJointHeapConfiguration() {
        return jointHeapConfiguration;
    }

    public Joinability getJoinability() {
        return joinability;
    }

    /**
     * Returns the HeapConfiguration with rule1 applied
     */
    public HeapConfiguration getRule1Applied() {
        TIntArrayList externalIndicesMap = context.getCollapsedHc1().getOriginalToCollapsedExternalIndices();
        Pair<Nonterminal, CollapsedHeapConfiguration> rule1 = grammar.getRule(r1ID);
        return applyMatching(rule1.first(), jointHeapConfiguration.getMatching1(), externalIndicesMap);
    }

    /**
     * Returns the HeapConfiguration with rule2 applied
     */
    public HeapConfiguration getRule2Applied() {
        TIntArrayList externalIndicesMap = context.getCollapsedHc2().getOriginalToCollapsedExternalIndices();
        Pair<Nonterminal, CollapsedHeapConfiguration> rule2 = grammar.getRule(r2ID);
        return applyMatching(rule2.first(), jointHeapConfiguration.getMatching2(), externalIndicesMap);
    }

    public HeapConfiguration getCanonical1() {
        return grammar.getCanonicalizationStrategy().canonicalize(this.getRule1Applied());
    }

    public HeapConfiguration getCanonical2() {
        return grammar.getCanonicalizationStrategy().canonicalize(this.getRule2Applied());
    }

    private HeapConfiguration applyMatching(Nonterminal nt, Matching matching, TIntArrayList externalIndicesMap) {
        if (externalIndicesMap == null) {
            return jointHeapConfiguration.getHeapConfiguration().clone().builder()
                    .replaceMatching(matching, nt)
                    .build();
        } else {
            return jointHeapConfiguration.getHeapConfiguration().clone().builder()
                    .replaceMatchingWithCollapsedExternals(matching, nt, externalIndicesMap)
                    .build();
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

    public Pair<Integer, Integer> getR1ID() {
        return r1ID;
    }

    public Pair<Integer, Integer> getR2ID() {
        return r2ID;
    }

}
