package de.rwth.i2.attestor.grammar.confluence.completion.validity;

import de.rwth.i2.attestor.grammar.NamedGrammar;
import de.rwth.i2.attestor.grammar.confluence.completion.CompletionState;

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
        NamedGrammar newGrammar = newCompletionState.getGrammar();
        if (oldGrammar.getOriginalGrammarRules().size() < newGrammar.getOriginalGrammarRules().size()) {
            // Some rules were added -> Check if they keep local concretizability

            // TODO: Implement

            throw new UnsupportedOperationException("Not implemented yet");
        } else {
            // No rules were added -> Local concretizability remains intact (TODO is this correct?)
            return true;
        }
    }
}
