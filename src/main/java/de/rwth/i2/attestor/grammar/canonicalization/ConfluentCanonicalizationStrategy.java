package de.rwth.i2.attestor.grammar.canonicalization;

import de.rwth.i2.attestor.grammar.NamedGrammar;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;

/**
 * A modyfied canonicalization strategy
 */
public class ConfluentCanonicalizationStrategy extends GeneralCanonicalizationStrategy {
    private final NamedGrammar grammar;

    public ConfluentCanonicalizationStrategy(NamedGrammar grammar, CanonicalizationHelper canonicalizationHelper) {
        super(grammar.getAbstractionGrammar(), canonicalizationHelper);
        this.grammar = grammar;
    }

    @Override
    public HeapConfiguration canonicalize(HeapConfiguration heapConfiguration) {
        if (grammar.blockHeapAbstraction(heapConfiguration)) {
            return heapConfiguration;
        } else {
            return super.canonicalize(heapConfiguration);
        }
    }
}
