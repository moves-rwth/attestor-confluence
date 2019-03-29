package de.rwth.i2.attestor.grammar;

import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;

import java.util.*;

public class GrammarRuleOriginal implements GrammarRule {
    private final String grammarName;
    private final int originalRuleIdx;
    private final Nonterminal nonterminal;
    private final HeapConfiguration hc;
    private final List<GrammarRuleCollapsed> collapsedRules;
    private final RuleStatus ruleStatus;

    /**
     * Creates a new rule that does not belong to any grammar.
     * TODO: How should new rules creation work?
     */
    @Deprecated
    public GrammarRuleOriginal(String grammarName, Nonterminal nonterminal, HeapConfiguration hc) {
        this.grammarName = grammarName;
        this.originalRuleIdx = -1;
        this.nonterminal = nonterminal;
        this.hc = hc;
        this.collapsedRules = Collections.emptyList();
        ruleStatus = RuleStatus.CONFLUENCE_GENERATED;
    }

    GrammarRuleOriginal(String grammarName, int originalRuleIdx, Nonterminal nonterminal, HeapConfiguration hc, List<GrammarRuleCollapsed> collapsedRules, RuleStatus ruleStatus) {
        this.grammarName = grammarName;
        this.originalRuleIdx = originalRuleIdx;
        this.nonterminal = nonterminal;
        this.hc = hc;
        this.collapsedRules = collapsedRules;
        this.ruleStatus = ruleStatus;
    }

    public List<GrammarRuleCollapsed> getCollapsedRules() {
        return Collections.unmodifiableList(collapsedRules);
    }

    public GrammarRuleOriginal changeRuleActivation(Collection<GrammarRule> flipActivation) {
        if (flipActivation.size() == 0) {
            return this;
        }

        RuleStatus newStatus;

        if (flipActivation.contains(this)) {
            // Deactivate or remove rule
            switch (getRuleStatus()) {
                case ACTIVE:
                    newStatus = RuleStatus.INACTIVE;
                    break;
                case INACTIVE:
                    newStatus = RuleStatus.ACTIVE;
                    break;
                case CONFLUENCE_GENERATED:
                    // Remove this rule (don't add it to newOriginalGrammarRules)
                    return null;
                default:
                    throw new IllegalStateException();

            }
        } else {
            // Don't change activation status
            newStatus = getRuleStatus();
        }

        List<GrammarRuleCollapsed> newCollapsedRules = new ArrayList<>();
        GrammarRuleOriginal newOriginalRule = new GrammarRuleOriginal(getGrammarName(), originalRuleIdx, nonterminal, hc, newCollapsedRules, newStatus);

        for (GrammarRuleCollapsed oldCollapsedRule : collapsedRules) {
            if (flipActivation.contains(oldCollapsedRule)) {
                newCollapsedRules.add(oldCollapsedRule.flipActivation());
            } else {
                // Don't change activation status
                newCollapsedRules.add(oldCollapsedRule);
            }
        }

        return newOriginalRule;
    }

    @Override
    public int getOriginalRuleIdx() {
        return originalRuleIdx;
    }

    @Override
    public RuleStatus getRuleStatus() {
        return ruleStatus;
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
    public String toString() {
        return Integer.toString(originalRuleIdx);
    }

    @Override
    public String getGrammarName() {
        return grammarName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(grammarName, originalRuleIdx, -1);
    }
}
