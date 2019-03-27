package de.rwth.i2.attestor.grammar;

public class GrammarRule {
    public final int NO_COLLAPSED_RULE_IDX = -1;
    private int originalRuleIdx;
    private int collapsedRuleIdx;

    public GrammarRule(int originalRuleIdx, int collapsedRuleIdx) {
        this.originalRuleIdx = originalRuleIdx;
        this.collapsedRuleIdx = collapsedRuleIdx;
    }

    public int getOriginalRuleIdx() {
        return originalRuleIdx;
    }

    public int getCollapsedRuleIdx() {
        return collapsedRuleIdx;
    }

    public boolean isOriginalRule() {
        return collapsedRuleIdx == NO_COLLAPSED_RULE_IDX;
    }
}
