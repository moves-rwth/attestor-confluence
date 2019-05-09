package de.rwth.i2.attestor.grammar.confluence.completion.validity;

import de.rwth.i2.attestor.grammar.GrammarRuleOriginal;
import de.rwth.i2.attestor.grammar.confluence.completion.CompletionState;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;

/**
 * An interface for all classes that allow to set certain conditions on what grammar modifications are considered to be valid.
 */
public interface GrammarValidity {

    boolean isValid(CompletionState newCompletionState);

    /**
     * @return The rules that are included in newCompletionState but not in oldCompletionState
     */
    static Collection<GrammarRuleOriginal> getNewRules(CompletionState oldCompletionState, CompletionState newCompletionState) {
        Collection<GrammarRuleOriginal> newRules = new ArrayList<>();
        int oldMaxRuleIdx = oldCompletionState.getGrammar().getMaxOriginalRuleIdx();

        for (GrammarRuleOriginal grammarRule : newCompletionState.getGrammar().getOriginalGrammarRules()) {
            if (grammarRule.getOriginalRuleIdx() > oldMaxRuleIdx) {
                // The rule is not contained in the oldCompletionState
                newRules.add(grammarRule);
            }
        }
        return newRules;
    }

    JSONObject getDescription();

}
