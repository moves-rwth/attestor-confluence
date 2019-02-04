package de.rwth.i2.attestor.grammar.confluence;

import de.rwth.i2.attestor.grammar.confluence.JointMorphism.JointMorphism;
import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;

public class CriticalPair {
    final Nonterminal nt1;
    final HeapConfiguration hc1;
    final Nonterminal nt2;
    final HeapConfiguration hc2;
    final JointMorphism jointMorphism;

    public CriticalPair(Nonterminal nt1, HeapConfiguration hc1, Nonterminal nt2, HeapConfiguration hc2,
                        JointMorphism morph) {
        this.nt1 = nt1;
        this.nt2 = nt2;
        this.hc1 = hc1;
        this.hc2 = hc2;
        this.jointMorphism = morph;
    }
}
