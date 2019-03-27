package de.rwth.i2.attestor.grammar.confluence.completion.strategies;

import de.rwth.i2.attestor.grammar.NamedGrammar;
import de.rwth.i2.attestor.grammar.confluence.completion.CompletionAlgorithm;
import de.rwth.i2.attestor.grammar.confluence.completion.CompletionState;
import de.rwth.i2.attestor.grammar.confluence.completion.heuristics.CompletionHeuristic;
import de.rwth.i2.attestor.grammar.confluence.completion.loss.CompletionStateLoss;

/**
 * Applies one heuristic as long as no further improvements can be made then moves to the next.
 * Cycles as long as improvements are possible.
 */
public class GreedyCompletion implements CompletionStrategy {

    @Override
    public CompletionState executeCompletionStrategy(NamedGrammar inputGrammar, CompletionAlgorithm completionSettings) {
        CompletionState currentState = new CompletionState(inputGrammar);
        CompletionStateLoss completionStateLoss = completionSettings.getCompletionStateLoss();
        int currentLoss = completionStateLoss.getLoss(currentState);
        int currentSearchDepth = 0;
        boolean madeProgress = true;

        // Cycle through all heuristics as long as there is still progress
        while (madeProgress) {
            madeProgress = false;

            for (CompletionHeuristic heuristic : completionSettings.getHeuristics()) {
                boolean appliedHeuristic = true;
                while (appliedHeuristic) {
                    appliedHeuristic = false;
                    for (CompletionState nextState : heuristic.applyHeuristic(currentState)) {
                        // Check if no more problematic critical pairs
                        if (nextState.getCriticalPairs().size() == 0) {
                            return nextState;
                        }
                        // Compute loss
                        int nextLoss = completionStateLoss.getLoss(nextState);
                        if (nextLoss < currentLoss) {
                            // Update state
                            currentState = nextState;
                            // Try to find another possible optimization using this strategy
                            appliedHeuristic = true;
                            // Cycle through all heuristics again later
                            madeProgress = true;
                            // Increment the search depth and check if we have to abort
                            currentSearchDepth++;
                            if (currentSearchDepth >= completionSettings.getMaxSearchDepth()) {
                                return currentState;
                            }
                            // Reinitialize possible next states for the current heuristic
                            break;
                        }
                    }
                }
            }
        }

        // Return the result
        return currentState;
    }
}
