package de.rwth.i2.attestor.grammar.confluence;

import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import de.rwth.i2.attestor.graph.morphism.Morphism;

public class CriticalPair {
    HeapConfiguration commonRhs;
    HeapConfiguration abstraction1;
    HeapConfiguration abstraction2;
    Morphism morphism1;
    Morphism morphism2;
    // TODO: Should we also explicitly store which two rules the critical pair is based on?

}
