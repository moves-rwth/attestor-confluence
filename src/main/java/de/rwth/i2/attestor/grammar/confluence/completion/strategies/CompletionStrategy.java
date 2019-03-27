package de.rwth.i2.attestor.grammar.confluence.completion.strategies;

import de.rwth.i2.attestor.grammar.NamedGrammar;
import de.rwth.i2.attestor.grammar.confluence.completion.CompletionAlgorithm;
import de.rwth.i2.attestor.grammar.confluence.completion.CompletionState;

public interface CompletionStrategy {

    /**
     * Executes the completion strategy with the given settings for the inputGrammar
     */
    CompletionState executeCompletionAlgorithm(NamedGrammar inputGrammar, CompletionAlgorithm completionSettings);

}
