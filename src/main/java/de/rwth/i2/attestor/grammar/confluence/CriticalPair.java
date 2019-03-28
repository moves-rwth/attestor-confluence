package de.rwth.i2.attestor.grammar.confluence;

import de.rwth.i2.attestor.grammar.GrammarRule;
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
import gnu.trove.list.array.TIntArrayList;

import java.util.Objects;

public class CriticalPair {
    private final NamedGrammar grammar;
    private final JointHeapConfiguration jointHeapConfiguration;
    private final GrammarRule r1, r2;
    private final HeapConfigurationContext context;
    private final Joinability joinability;

    public CriticalPair(NodeOverlapping nodeOverlapping, EdgeOverlapping edgeOverlapping, NamedGrammar grammar, GrammarRule r1, GrammarRule r2) {
        this.grammar = grammar;
        this.r1 = r1;
        this.r2 = r2;
        this.context = nodeOverlapping.getContext();
        VF2IsomorphismChecker checker = new VF2IsomorphismChecker();
        // 1. Compute the joint graph
        jointHeapConfiguration = new JointHeapConfiguration(edgeOverlapping, nodeOverlapping);

        // 2. Compute fully abstracted heap configuration (apply r1 first)
        HeapConfiguration fullyAbstracted1 = getCanonical1();

        // 3. Compute fully abstracted heap configuration (apply r2 first)
        HeapConfiguration fullyAbstracted2 = getCanonical2();

        // 4. Check if both fully abstracted heap configurations are isomorphic (and therefore joinable)

        // Check if track morphism defines the isomorphism (strongly joinable)
        TIntArrayList publicIdIntersection = new TIntArrayList(fullyAbstracted1.nodes()); // TODO: Copy probably unnecessary
        publicIdIntersection.retainAll(fullyAbstracted2.nodes());

        HeapConfiguration fullyAbstracted1Track = setNodesExternal(fullyAbstracted1, publicIdIntersection);
        HeapConfiguration fullyAbstracted2Track = setNodesExternal(fullyAbstracted2, publicIdIntersection);

        checker.run((Graph) fullyAbstracted1Track, (Graph) fullyAbstracted2Track);
        if (checker.hasMorphism()) {
            // Strongly joinable
            this.joinability = Joinability.STRONGLY_JOINABLE;
        } else {
            // The critical pair is not strongly joinable -> check if it is weakly joinable
            // Check if there is ANY isomorphism
            checker.run((Graph) fullyAbstracted1, (Graph) fullyAbstracted2);
            if (checker.hasMorphism()) {
                this.joinability = Joinability.WEAKLY_JOINABLE;
            } else {
                this.joinability = Joinability.NOT_JOINABLE;
            }
        }
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
        return applyMatching(r1.getNonterminal(), jointHeapConfiguration.getMatching1(), externalIndicesMap);
    }

    /**
     * Returns the HeapConfiguration with rule2 applied
     */
    public HeapConfiguration getRule2Applied() {
        TIntArrayList externalIndicesMap = context.getCollapsedHc2().getOriginalToCollapsedExternalIndices();
        return applyMatching(r2.getNonterminal(), jointHeapConfiguration.getMatching2(), externalIndicesMap);
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

    private static HeapConfiguration setNodesExternal(HeapConfiguration hc, TIntArrayList nodes) {
        // Clone the HeapConfiguration
        HeapConfigurationBuilder builder = hc.clone().builder();
        nodes.forEach(hcId -> {
            builder.setExternal(hcId);
            return true;
        });
        return builder.build();
    }

    public GrammarRule getR1() {
        return r1;
    }

    public GrammarRule getR2() {
        return r2;
    }

    @Override
    public int hashCode() {
        // Note: Should also include the joint head configuration and its two rule applications, but then hash code does not stay the same over multiple runs
        return Objects.hash(r1, r2, joinability.getValue());
    }
}
