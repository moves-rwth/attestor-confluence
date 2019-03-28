package de.rwth.i2.attestor.grammar;

import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;

public class StandaloneGrammarRule implements GrammarRule {
    private CollapsedHeapConfiguration collapsedHeapConfiguration;
    private Nonterminal nonterminal;

    public StandaloneGrammarRule(Nonterminal nonterminal, HeapConfiguration heapConfiguration) {
        this.nonterminal = nonterminal;
        this.collapsedHeapConfiguration = new CollapsedHeapConfiguration(heapConfiguration, heapConfiguration, null);
    }

    public StandaloneGrammarRule(Nonterminal nonterminal, CollapsedHeapConfiguration collapsedHeapConfiguration) {
        this.nonterminal = nonterminal;
        this.collapsedHeapConfiguration = collapsedHeapConfiguration;
    }

    @Override
    public boolean isOriginalRule() {
        return collapsedHeapConfiguration.getOriginalToCollapsedExternalIndices() != null;
    }

    @Override
    public Nonterminal getNonterminal() {
        return nonterminal;
    }

    @Override
    public HeapConfiguration getHeapConfiguration() {
        return collapsedHeapConfiguration.getCollapsed();
    }

    @Override
    public CollapsedHeapConfiguration getCollapsedHeapConfiguration() {
        return collapsedHeapConfiguration;
    }

    @Override
    public String toString() {
        return "Standalone(" + this.hashCode() + ")";
    }
}
