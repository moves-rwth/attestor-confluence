package de.rwth.i2.attestor.grammar.confluence.completion.loss;

import de.rwth.i2.attestor.grammar.confluence.completion.CompletionState;

/**
 * A simple loss function that just takes the number of critical pairs into account.
 */
public class NumberCriticalPairLoss implements CompletionStateLoss {
    @Override
    public double getLoss(CompletionState state) {
        return state.getCriticalPairs().size();
    }
}
