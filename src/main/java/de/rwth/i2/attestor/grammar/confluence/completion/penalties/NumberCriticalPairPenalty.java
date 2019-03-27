package de.rwth.i2.attestor.grammar.confluence.completion.penalties;

import de.rwth.i2.attestor.grammar.confluence.completion.CompletionState;

public class NumberCriticalPairPenalty implements CompletionStatePenalty {
    @Override
    public int getPenalty(CompletionState state) {
        return state.getCriticalPairs().size();
    }
}
