package de.rwth.i2.attestor.grammar.confluence.completion.validity;

import de.rwth.i2.attestor.grammar.GrammarRuleOriginal;
import de.rwth.i2.attestor.grammar.NamedGrammar;
import de.rwth.i2.attestor.grammar.confluence.completion.CompletionState;
import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO: Can removing rules lead to not local concretizability?
 */
public class LocalConcretizability implements GrammarValidity {

    /**
     * Assuming the old grammar was locally concretizable is the new grammar locally concretizable?
     * Note: Might return false negatives (returns false even though locally concretizable)
     *
     * For all tentacles of all LHS of newRules:
     * 1. Calculate the set of outgoing selectors for each RHS at the current tentacle
     * 2. If there is a selector edge that is not in one RHS, check if the RHS can create the selector edge using further rule applications -> violation
     */
    @Override
    public boolean isValid(CompletionState oldCompletionState, CompletionState newCompletionState) {
        NamedGrammar oldGrammar = oldCompletionState.getGrammar();
        Collection<GrammarRuleOriginal> newGrammarRules = GrammarValidity.getNewRules(oldCompletionState, newCompletionState);
        if (newGrammarRules.size() == 0) {
            // No rules were added -> Local concretizability remains intact (TODO is this correct?)
        } else {
            // Some rules were added -> Check if they don't violate local concretizability
            Map<Nonterminal, Collection<HeapConfiguration>> rulesByNonterminal = new HashMap<>();

            for (GrammarRuleOriginal rule : newGrammarRules) {
                // TODO: Fill rulesByNonterminal
            }

            // TODO: Implement

            throw new UnsupportedOperationException("Not implemented yet");
        }
    }
}
