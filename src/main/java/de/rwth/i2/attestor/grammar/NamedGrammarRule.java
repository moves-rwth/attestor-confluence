package de.rwth.i2.attestor.grammar;

import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;

import java.util.Objects;

public class NamedGrammarRule implements GrammarRule {
    public final int NO_COLLAPSED_RULE_IDX = -1;
    private final int originalRuleIdx;
    private final int collapsedRuleIdx;
    private final NamedGrammar grammar;

    public NamedGrammarRule(NamedGrammar grammar, int originalRuleIdx) {
        this.originalRuleIdx = originalRuleIdx;
        this.collapsedRuleIdx = NO_COLLAPSED_RULE_IDX;
        this.grammar = grammar;
    }

    public NamedGrammarRule(NamedGrammar grammar, int originalRuleIdx, int collapsedRuleIdx) {
        this.originalRuleIdx = originalRuleIdx;
        this.collapsedRuleIdx = collapsedRuleIdx;
        this.grammar = grammar;
    }

    public int getOriginalRuleIdx() {
        return originalRuleIdx;
    }

    public int getCollapsedRuleIdx() {
        return collapsedRuleIdx;
    }

    @Override
    public Nonterminal getNonterminal() {
        return grammar.getNonterminal(originalRuleIdx);
    }

    @Override
    public boolean isOriginalRule() {
        return collapsedRuleIdx == NO_COLLAPSED_RULE_IDX;
    }

    @Override
    public HeapConfiguration getHeapConfiguration() {
        if (isOriginalRule()) {
            return grammar.getHeapConfiguration(originalRuleIdx);
        } else {
            return grammar.getCollapsedRhs(originalRuleIdx, collapsedRuleIdx).getCollapsed();
        }
    }

    @Override
    public CollapsedHeapConfiguration getCollapsedHeapConfiguration() {
        if (isOriginalRule()) {
            HeapConfiguration hc = getHeapConfiguration();
            return new CollapsedHeapConfiguration(hc, hc, null);
        } else {
            return grammar.getCollapsedRhs(originalRuleIdx, collapsedRuleIdx);
        }
    }

    @Override
    public boolean deactivatedForAbstraction() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        if (isOriginalRule()) {
            return Integer.toString(originalRuleIdx);
        } else {
            return originalRuleIdx + "." + collapsedRuleIdx;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(grammar.getGrammarName(), originalRuleIdx, collapsedRuleIdx);
    }
}
