package de.rwth.i2.attestor.grammar;

import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import de.rwth.i2.attestor.util.Pair;

/**
 * TODO: Should this object optionally contain the rule itself and maybe a link to the contained grammar
 * TODO: Maybe introduce "standalone grammar rule" and "rule from grammar" subclasses
 */
public class GrammarRule {
    public final int NO_COLLAPSED_RULE_IDX = -1;
    private final int originalRuleIdx;
    private final int collapsedRuleIdx;
    private final NamedGrammar grammar;

    public GrammarRule(NamedGrammar grammar, int originalRuleIdx) {
        this.originalRuleIdx = originalRuleIdx;
        this.collapsedRuleIdx = NO_COLLAPSED_RULE_IDX;
        this.grammar = grammar;
    }

    public GrammarRule(NamedGrammar grammar, int originalRuleIdx, int collapsedRuleIdx) {
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

    public boolean isOriginalRule() {
        return collapsedRuleIdx == NO_COLLAPSED_RULE_IDX;
    }

    public Nonterminal getNonterminal() {
        return grammar.getNonterminal(originalRuleIdx);
    }

    public HeapConfiguration getHeapConfiguration() {
        if (collapsedRuleIdx == NO_COLLAPSED_RULE_IDX) {
            return grammar.getHeapConfiguration(originalRuleIdx);
        } else {
            return grammar.getCollapsedRhs(originalRuleIdx, collapsedRuleIdx).getCollapsed();
        }
    }
}
