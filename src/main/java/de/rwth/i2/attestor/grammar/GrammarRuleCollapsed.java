package de.rwth.i2.attestor.grammar;

import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;

import java.util.Objects;

public class GrammarRuleCollapsed implements GrammarRule {
    private final GrammarRuleOriginal originalRule;
    private final CollapsedHeapConfiguration collapsedHeapConfiguration;
    private final boolean isDeactivated;
    private final int collapsedRuleIdx;

    public GrammarRuleCollapsed(GrammarRuleOriginal originalRule, int collapsedRuleIdx, CollapsedHeapConfiguration cHC, boolean isDeactivated) {
        this.originalRule = originalRule;
        this.collapsedHeapConfiguration = cHC;
        this.isDeactivated = isDeactivated;
        this.collapsedRuleIdx = collapsedRuleIdx;
    }

    @Override
    public boolean deactivatedForAbstraction() {
        return isDeactivated;
    }

    @Override
    public Nonterminal getNonterminal() {
        return originalRule.getNonterminal();
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
    public NamedGrammar getGrammar() {
        return originalRule.getGrammar();
    }

    @Override
    public int getOriginalRuleIdx() {
        return originalRule.getOriginalRuleIdx();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getGrammar().getGrammarName(), getOriginalRuleIdx(), getCollapsedRuleIdx());
    }

    public int getCollapsedRuleIdx() {
        return collapsedRuleIdx;
    }
}
