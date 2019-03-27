package de.rwth.i2.attestor.grammar.confluence.completion.penalties;

import de.rwth.i2.attestor.grammar.confluence.completion.CompletionState;

/**
 * This that an implementing class might take into account:
 * - Number of critical pairs: This should be the main factor
 * - Number of nodes in right hand sides: It is more desirable to have fewer external nodes in grammar rules
 */
public interface CompletionStatePenalty {

    /**
     * Returns an integer representing the quality of the given completion state (remaining critical pairs & current list of grammar rules)
     * A smaller value means that the completion state is closer to being confluent or that the state is more desirable.
     */
    int getPenalty(CompletionState state);
}
