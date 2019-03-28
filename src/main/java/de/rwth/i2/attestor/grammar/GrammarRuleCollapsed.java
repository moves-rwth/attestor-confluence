package de.rwth.i2.attestor.grammar;

import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;

import java.util.Objects;

public class GrammarRuleCollapsed implements GrammarRule {
    private final GrammarRuleOriginal originalRule;
    private final CollapsedHeapConfiguration collapsedHeapConfiguration;
    private final int collapsedRuleIdx;
    private final RuleStatus status;


    public GrammarRuleCollapsed(GrammarRuleOriginal originalRule, int collapsedRuleIdx, CollapsedHeapConfiguration cHC, RuleStatus status) {
        if (status == RuleStatus.CONFLUENCE_GENERATED) {
            throw new IllegalArgumentException("Collapsed rules cannot have state CONFLUENCE_GENERATED");
        }
        this.originalRule = originalRule;
        this.collapsedHeapConfiguration = cHC;
        this.collapsedRuleIdx = collapsedRuleIdx;
        this.status = status;
    }

    GrammarRuleCollapsed flipActivation(GrammarRuleOriginal newOriginal) {
        RuleStatus newStatus;
        switch (status) {
            case ACTIVE:  // Rule should be inactivated
                newStatus = RuleStatus.INACTIVE;
                break;
            case INACTIVE:  // Rule should be activated
                newStatus = RuleStatus.ACTIVE;
                break;
            case CONFLUENCE_GENERATED:
                throw new IllegalStateException("Collapsed rules should not be CONFLUENCE_GENERATED");
            default:
                throw new IllegalStateException("Invalid status");
        }
        return new GrammarRuleCollapsed(newOriginal, collapsedRuleIdx, collapsedHeapConfiguration, newStatus);
    }

    GrammarRuleCollapsed attachToOriginalRule(GrammarRuleOriginal newOriginal) {
        return new GrammarRuleCollapsed(newOriginal, collapsedRuleIdx, collapsedHeapConfiguration, status);
    }

    @Override
    public RuleStatus getRuleStatus() {
        return status;
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
