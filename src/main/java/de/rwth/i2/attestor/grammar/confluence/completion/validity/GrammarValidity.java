package de.rwth.i2.attestor.grammar.confluence.completion.validity;

import de.rwth.i2.attestor.grammar.confluence.completion.CompletionState;

/**
 * An interface for all classes that allow to set certain conditions on what grammar modifications are considered to be valid.
 */
public interface GrammarValidity {

    boolean isValid(CompletionState oldCompletionState, CompletionState newCompletionState);

}
