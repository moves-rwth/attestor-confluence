package de.rwth.i2.attestor.grammar.confluence.completion.strategies;

import de.rwth.i2.attestor.grammar.NamedGrammar;
import de.rwth.i2.attestor.grammar.confluence.completion.CompletionResult;
import de.rwth.i2.attestor.grammar.confluence.completion.CompletionSettings;

public interface CompletionStrategy {

    /**
     * Executes the completion strategy with the given settings for the inputGrammar
     */
    CompletionResult executeCompletionAlgorithm(NamedGrammar inputGrammar, CompletionSettings completionSettings);

}
