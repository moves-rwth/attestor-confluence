package de.rwth.i2.attestor.grammar;

import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.util.Pair;

import java.util.HashSet;
import java.util.Set;

/**
 * A grammar class that only has a single collapsed Rule
 */
public class SubsetGrammar extends NamedGrammar {
    private Pair<Integer, Integer> collapsedRule;

    public SubsetGrammar(NamedGrammar grammar, Pair<Integer, Integer> collapsedRule) {
        super(grammar, grammar.getGrammarName());
        this.collapsedRule = collapsedRule;
    }

    @Override
    public int numberCollapsedRules(int originalRuleIdx) {
        if (collapsedRule == null || originalRuleIdx != collapsedRule.first()) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public CollapsedHeapConfiguration getCollapsedRhs(int originalRuleIdx, int collapsedRuleIdx) {
        if (originalRuleIdx != collapsedRule.first() || collapsedRuleIdx != 0) {
            throw new IllegalArgumentException("Rule not in subset grammar");
        }
        return super.getCollapsedRhs(collapsedRule.first(), collapsedRule.second());
    }

    @Override
    public Set<CollapsedHeapConfiguration> getCollapsedRightHandSidesFor(Nonterminal nonterminal) {
        Set<CollapsedHeapConfiguration> result = new HashSet<>();
        if (collapsedRule == null) {
            return result;
        } else {
            Nonterminal nonterminalInSubset = getOriginalRule(collapsedRule.first()).first();
            if (collapsedRule != null && nonterminalInSubset.getLabel().equals(nonterminal.getLabel())) {
                result.add(getCollapsedRhs(collapsedRule.first(), collapsedRule.second()));
            }
            return result;
        }
    }
}
