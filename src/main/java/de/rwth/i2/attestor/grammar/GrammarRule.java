package de.rwth.i2.attestor.grammar;

/**
 * TODO: Should this object optionally contain the rule itself and maybe a link to the contained grammar
 * TODO: Maybe introduce "standalone grammar rule" and "rule from grammar" subclasses
 */
public class GrammarRule {
    public final int NO_COLLAPSED_RULE_IDX = -1;
    private final int originalRuleIdx;
    private final int collapsedRuleIdx;

    public GrammarRule(int originalRuleIdx) {
        this.originalRuleIdx = originalRuleIdx;
        this.collapsedRuleIdx = NO_COLLAPSED_RULE_IDX;
    }

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
