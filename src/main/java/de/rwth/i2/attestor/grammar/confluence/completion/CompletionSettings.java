package de.rwth.i2.attestor.grammar.confluence.completion;

import de.rwth.i2.attestor.grammar.confluence.completion.heuristics.CompletionRuleAddingHeuristic;
import de.rwth.i2.attestor.grammar.confluence.completion.heuristics.CompletionRuleRestrictionHeuristic;
import de.rwth.i2.attestor.grammar.confluence.completion.penalties.CompletionStatePenalty;

import java.util.Collection;

public class CompletionSettings {
    CompletionRuleRestrictionHeuristic ruleRestriction;
    Collection<CompletionRuleAddingHeuristic> ruleAddingHeuristics;
    boolean removeConcreteCriticalPairs;
    CompletionStatePenalty completionStatePenalty;

    public CompletionSettings(CompletionRuleRestrictionHeuristic ruleRestriction,
                              Collection<CompletionRuleAddingHeuristic> ruleAddingHeuristics, boolean removeConcreteCriticalPairs,
                              CompletionStatePenalty completionStatePenalty) {
        this.ruleRestriction = ruleRestriction;
        this.ruleAddingHeuristics = ruleAddingHeuristics;
        this.removeConcreteCriticalPairs = removeConcreteCriticalPairs;
        this.completionStatePenalty = completionStatePenalty;
    }

    public CompletionRuleRestrictionHeuristic getRuleRestriction() {
        return ruleRestriction;
    }

    public Collection<CompletionRuleAddingHeuristic> getRuleAddingHeuristics() {
        return ruleAddingHeuristics;
    }

    public boolean getRemoveConcreteCriticalPairs() {
        return removeConcreteCriticalPairs;
    }

    public CompletionStatePenalty getCompletionStatePenalty() {
        return completionStatePenalty;
    }
}
