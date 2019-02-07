package de.rwth.i2.attestor.grammar.confluence;

import de.rwth.i2.attestor.grammar.confluence.jointMorphism.JointMorphism;
import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;

public class CriticalPair {
    private final Nonterminal nt1, nt2;
    private final HeapConfiguration hc1, hc2;
    private final JointMorphism nodeJointMorphism;
    private final Joinability joinability;

    public CriticalPair(Nonterminal nt1, HeapConfiguration hc1, Nonterminal nt2, HeapConfiguration hc2,
                        JointMorphism morph, Joinability joinability) {
        this.nt1 = nt1;
        this.nt2 = nt2;
        this.hc1 = hc1;
        this.hc2 = hc2;
        this.nodeJointMorphism = morph;
        this.joinability = joinability;
    }

    public enum Joinability {
        STRONGLY_JOINABLE, WEAKLY_JOINABLE, NOT_JOINABLE;
    }
}
