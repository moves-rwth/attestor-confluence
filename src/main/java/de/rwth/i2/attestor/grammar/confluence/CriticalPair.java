package de.rwth.i2.attestor.grammar.confluence;

import de.rwth.i2.attestor.grammar.confluence.jointMorphism.JointHeapConfiguration;
import de.rwth.i2.attestor.graph.Nonterminal;

public class CriticalPair {
    private final Nonterminal nt1, nt2;
    private final JointHeapConfiguration jointHeapConfiguration;
    private final Joinability joinability;

    public CriticalPair(Nonterminal nt1, Nonterminal nt2, JointHeapConfiguration jointHeapConfiguration, Joinability joinability) {
        this.nt1 = nt1;
        this.nt2 = nt2;
        this.jointHeapConfiguration = jointHeapConfiguration;
        this.joinability = joinability;
    }

    public Nonterminal getNt1() {
        return nt1;
    }

    public Nonterminal getNt2() {
        return nt2;
    }

    public JointHeapConfiguration getJointHeapConfiguration() {
        return jointHeapConfiguration;
    }

    public Joinability getJoinability() {
        return joinability;
    }

    public enum Joinability {
        STRONGLY_JOINABLE, WEAKLY_JOINABLE, NOT_JOINABLE
    }
}
