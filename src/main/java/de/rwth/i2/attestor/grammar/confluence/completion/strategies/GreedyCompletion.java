package de.rwth.i2.attestor.grammar.confluence.completion.strategies;

import de.rwth.i2.attestor.grammar.NamedGrammar;
import de.rwth.i2.attestor.grammar.confluence.completion.CompletionResult;
import de.rwth.i2.attestor.grammar.confluence.completion.CompletionSettings;

/**
 * Executes the completion heuristics in the following cycle:
 * removeConcreteCriticalPairs -> ruleAdding -> ruleRestriction
 *
 * Applies one heuristic as long as no further improvements can be made then moves to the next.
 * Cycles as long as improvements are possible.
 */
public class GreedyCompletion implements CompletionStrategy {

    @Override
    public CompletionResult executeCompletionAlgorithm(NamedGrammar inputGrammar, CompletionSettings completionSettings) {
        // TODO: Implement
        return null;
    }
}
