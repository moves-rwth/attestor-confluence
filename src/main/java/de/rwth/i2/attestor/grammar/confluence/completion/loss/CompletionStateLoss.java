package de.rwth.i2.attestor.grammar.confluence.completion.loss;

import de.rwth.i2.attestor.grammar.confluence.completion.CompletionState;
import org.json.JSONObject;

/**
 * Classes implementing this interface represent a way to compute a metric to evaluate how "close" a modified grammar
 * is to being confluent.
 *
 * Things that an implementing class might take into account:
 * - Number of critical pairs: This should be the main factor
 * - Number of external nodes in right hand sides: It is more desirable to have fewer external nodes in grammar rules
 * - Number of grammar rules
 */
public interface CompletionStateLoss {

    /**
     * Returns an integer representing the quality of the given completion state (remaining critical pairs & current list of grammar rules)
     * A smaller value means that the completion state is closer to being confluent or that the state is more desirable.
     */
    double getLoss(CompletionState state);

    JSONObject getDescription();
}
