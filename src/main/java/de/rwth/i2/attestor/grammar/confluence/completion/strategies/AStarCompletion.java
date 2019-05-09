package de.rwth.i2.attestor.grammar.confluence.completion.strategies;

import com.google.common.collect.ImmutableMap;
import de.rwth.i2.attestor.grammar.NamedGrammar;
import de.rwth.i2.attestor.grammar.confluence.completion.CompletionAlgorithm;
import de.rwth.i2.attestor.grammar.confluence.completion.CompletionState;
import org.json.JSONObject;

/**
 * A more complex completion strategy that might find better solutions than the greedy approach.
 */
public class AStarCompletion implements CompletionStrategy {
    @Override
    public CompletionState executeCompletionStrategy(NamedGrammar inputGrammar, CompletionAlgorithm completionSettings) {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public JSONObject getDescription() {
        return new JSONObject(ImmutableMap.of(
                "name", "aStar"
        ));
    }
}
