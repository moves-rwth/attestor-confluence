package de.rwth.i2.attestor.grammar;

import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;

import java.util.Objects;

public class StandaloneGrammarRule implements GrammarRule {
    private final CollapsedHeapConfiguration collapsedHeapConfiguration;
    private final Nonterminal nonterminal;
    private final boolean isDeactivated;

    public StandaloneGrammarRule(Nonterminal nonterminal, HeapConfiguration heapConfiguration) {
        this.nonterminal = nonterminal;
        this.collapsedHeapConfiguration = new CollapsedHeapConfiguration(heapConfiguration, heapConfiguration, null);
        this.isDeactivated = false;
    }

    public StandaloneGrammarRule(Nonterminal nonterminal, CollapsedHeapConfiguration collapsedHeapConfiguration, boolean isDeactivated) {
        this.nonterminal = nonterminal;
        this.collapsedHeapConfiguration = collapsedHeapConfiguration;
        this.isDeactivated = isDeactivated;
    }

    public StandaloneGrammarRule(Nonterminal nonterminal, CollapsedHeapConfiguration collapsedHeapConfiguration) {
        this(nonterminal, collapsedHeapConfiguration, false);
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
    public boolean deactivatedForAbstraction() {
        return isDeactivated;
    }

    @Override
    public String toString() {
        return "(" + this.hashCode() + ")";
    }
}
