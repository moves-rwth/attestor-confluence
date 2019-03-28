package de.rwth.i2.attestor.grammar;

import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;

import java.util.*;

public class GrammarRuleOriginal implements GrammarRule {


    private final NamedGrammar grammar;
    private final int originalRuleIdx;
    private final Nonterminal nonterminal;
    private final HeapConfiguration hc;
    private final List<GrammarRuleCollapsed> collapsedRules;
    private final RuleStatus ruleStatus;

    /**
     * Creates a new rule that does not belong to any grammar.
     */
    public GrammarRuleOriginal(Nonterminal nonterminal, HeapConfiguration hc) {
        this.grammar = null;
        this.originalRuleIdx = -1;
        this.nonterminal = nonterminal;
        this.hc = hc;
        this.collapsedRules = Collections.emptyList();
        ruleStatus = RuleStatus.CONFLUENCE_GENERATED;
    }

    GrammarRuleOriginal(NamedGrammar grammar, int originalRuleIdx, Nonterminal nonterminal, HeapConfiguration hc, List<GrammarRuleCollapsed> collapsedRules, RuleStatus ruleStatus) {
        this.grammar = grammar;
        this.originalRuleIdx = originalRuleIdx;
        this.nonterminal = nonterminal;
        this.hc = hc;
        this.collapsedRules = collapsedRules;
        this.ruleStatus = ruleStatus;
    }

    public List<GrammarRuleCollapsed> getCollapsedRules() {
        return Collections.unmodifiableList(collapsedRules);
    }

    public GrammarRuleOriginal changeRuleActivation(NamedGrammar newNamedGrammar, Collection<GrammarRule> flipActivation) {
        List<GrammarRuleCollapsed> newCollapsedRules = new ArrayList<>();
        GrammarRuleOriginal newOriginalRule;

        if (flipActivation.contains(this)) {
            RuleStatus newStatus;
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
            newOriginalRule = new GrammarRuleOriginal(newNamedGrammar, originalRuleIdx, nonterminal, hc, newCollapsedRules, newStatus);
        } else {
            // Don't change activation status, just attach to new grammar
            newOriginalRule = this.attachToGrammar(newNamedGrammar, originalRuleIdx);
        }

        for (GrammarRuleCollapsed oldCollapsedRule : collapsedRules) {
            if (flipActivation.contains(oldCollapsedRule)) {
                newCollapsedRules.add(oldCollapsedRule.flipActivation(newOriginalRule));
            } else {
                // Don't change activation status, just attach to new original rule
                newCollapsedRules.add(oldCollapsedRule.attachToOriginalRule(newOriginalRule));
            }
        }

        return newOriginalRule;
    }

    /**
     * Returns a grammar rule that is attached to a different grammar. Only supported for CONFLUENCE_GENERATED rules without any collapsed rules
     */
    public GrammarRuleOriginal attachToGrammar(NamedGrammar grammar, int originalRuleIdx) {
        if (ruleStatus != RuleStatus.CONFLUENCE_GENERATED || collapsedRules.size() != 0) {
            throw new IllegalArgumentException();
        }
        return new GrammarRuleOriginal(grammar, originalRuleIdx, nonterminal, hc, Collections.emptyList(), RuleStatus.CONFLUENCE_GENERATED);
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
