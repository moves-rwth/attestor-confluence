package de.rwth.i2.attestor.grammar.confluence.completion.loss;

import de.rwth.i2.attestor.grammar.confluence.completion.CompletionState;

public class NumberCriticalPairLoss implements CompletionStateLoss {
    @Override
    public int getLoss(CompletionState state) {
        return state.getCriticalPairs().size();
    }
}
