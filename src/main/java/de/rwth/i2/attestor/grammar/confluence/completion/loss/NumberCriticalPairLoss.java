package de.rwth.i2.attestor.grammar.confluence.completion.loss;

import com.google.common.collect.ImmutableMap;
import de.rwth.i2.attestor.grammar.confluence.completion.CompletionState;
import org.json.JSONObject;

import java.util.Map;

/**
 * A simple loss function that just takes the number of critical pairs into account.
 */
public class NumberCriticalPairLoss implements CompletionStateLoss {
    @Override
    public double getLoss(CompletionState state) {
        return state.getCriticalPairs().size();
    }

    @Override
    public JSONObject getDescription() {
        return new JSONObject(ImmutableMap.of(
                "name", "numberCriticalPairLoss"
        ));
    }
}
