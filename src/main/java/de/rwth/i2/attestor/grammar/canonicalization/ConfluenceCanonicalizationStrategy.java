package de.rwth.i2.attestor.grammar.canonicalization;

import de.rwth.i2.attestor.grammar.NamedGrammar;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;

public class ConfluenceCanonicalizationStrategy extends GeneralCanonicalizationStrategy {
    private final NamedGrammar grammar;

    public ConfluenceCanonicalizationStrategy(NamedGrammar grammar, CanonicalizationHelper helper) {
        super(grammar.getAbstractionGrammar(), helper);
        this.grammar = grammar;
    }

    @Override
    public HeapConfiguration canonicalize(HeapConfiguration heapConfiguration) {
        if (grammar.blockHeapAbstraction(heapConfiguration)) {
            // The abstraction is blocked to avoid a critical pair
            return heapConfiguration;
        }
        // TODO: Check if the heap configuration should be blocked
        return super.canonicalize(heapConfiguration);
    }

}
