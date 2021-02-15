package de.rwth.i2.attestor.grammar.confluence.completion.strategies;

import de.rwth.i2.attestor.grammar.ConfluenceWrapperGrammar;
import de.rwth.i2.attestor.grammar.confluence.completion.CompletionAlgorithm;
import de.rwth.i2.attestor.grammar.confluence.completion.CompletionState;
import org.json.JSONObject;

/**
 * A completion strategy specifies how the different heuristics should be applied in order to find a completion state
 * with minimal loss.
 */
public interface CompletionStrategy {

    /**
     * Executes the completion strategy with the given settings for the inputGrammar
     */
    CompletionState executeCompletionStrategy(ConfluenceWrapperGrammar inputGrammar, CompletionAlgorithm completionSettings);


    JSONObject getDescription();
}
