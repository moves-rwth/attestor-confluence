package de.rwth.i2.attestor.grammar.confluence.completion.validity;

import de.rwth.i2.attestor.grammar.confluence.completion.CompletionState;


/**
 * TODO: We might implement two different language equivalence checks.
 * Reject all grammars for which we cannot guarantee that they are language equivalent
 * Note: Might reject language equivalent grammars
 *
 * TODO: variation: Reject all grammars for which we have found a proof that they are not language equivalent  (Might accept not language equivalent grammars)
 * TODO Is the variation really helpful for anything?
 *
 */
public class LanguageEquivalence implements GrammarValidity {
    @Override
    public boolean isValid(CompletionState newCompletionState) {
        throw new UnsupportedOperationException("Language equivalence check not implemented");
    }
}
