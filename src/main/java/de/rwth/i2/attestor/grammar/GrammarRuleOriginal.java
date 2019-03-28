package de.rwth.i2.attestor.grammar;

import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class GrammarRuleOriginal implements GrammarRule {
    private final NamedGrammar grammar;
    private final int originalRuleIdx;
    private final Nonterminal nonterminal;
    private final HeapConfiguration hc;
    private final List<GrammarRuleCollapsed> collapsedRules;
    private final boolean deactivatedForAbstraction;

    GrammarRuleOriginal(NamedGrammar grammar, int originalRuleIdx, Nonterminal nonterminal, HeapConfiguration hc, List<GrammarRuleCollapsed> collapsedRules, boolean deactivatedForAbstraction) {
        this.grammar = grammar;
        this.originalRuleIdx = originalRuleIdx;
        this.nonterminal = nonterminal;
        this.hc = hc;
        this.collapsedRules = collapsedRules;
        this.deactivatedForAbstraction = false;
    }

    public List<GrammarRuleCollapsed> getCollapsedRules() {
        return Collections.unmodifiableList(collapsedRules);
    }

    @Override
    public int getOriginalRuleIdx() {
        return originalRuleIdx;
    }

    @Override
    public boolean deactivatedForAbstraction() {
        return deactivatedForAbstraction;
    }

    @Override
    public Nonterminal getNonterminal() {
        return nonterminal;
    }

    @Override
    public HeapConfiguration getHeapConfiguration() {
        return hc;
    }

    @Override
    public CollapsedHeapConfiguration getCollapsedHeapConfiguration() {
        return new CollapsedHeapConfiguration(hc, hc, null);
    }

    @Override
    public NamedGrammar getGrammar() {
        return grammar;
    }

    @Override
    public String toString() {
        return Integer.toString(originalRuleIdx);
    }

    @Override
    public int hashCode() {
        return Objects.hash(grammar.getGrammarName(), originalRuleIdx, -1);
    }
}
