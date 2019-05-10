package de.rwth.i2.attestor.grammar.confluence.completion.strategies;

import com.google.common.collect.ImmutableMap;
import de.rwth.i2.attestor.grammar.NamedGrammar;
import de.rwth.i2.attestor.grammar.confluence.benchmark.CompletionHeuristicStatisticCollector;
import de.rwth.i2.attestor.grammar.confluence.benchmark.StartStopTimer;
import de.rwth.i2.attestor.grammar.confluence.completion.CompletionAlgorithm;
import de.rwth.i2.attestor.grammar.confluence.completion.CompletionState;
import de.rwth.i2.attestor.grammar.confluence.completion.heuristics.CompletionHeuristic;
import de.rwth.i2.attestor.grammar.confluence.completion.loss.CompletionStateLoss;
import de.rwth.i2.attestor.grammar.confluence.completion.validity.GrammarValidity;
import org.json.JSONObject;

/**
 * Applies one heuristic as long as no further improvements can be made then moves to the next.
 * Cycles as long as improvements are possible.
 */
public class GreedyCompletion implements CompletionStrategy {
    final int maxSearchDepth;
    final StartStopTimer completeRuntime = new StartStopTimer();

    /**
     * @param maxSearchDepth The maximum number of successful heuristic applications (no maximum if set to 0)
     */
    public GreedyCompletion(int maxSearchDepth) {
        this.maxSearchDepth = maxSearchDepth;
    }

    @Override
    public CompletionState executeCompletionStrategy(NamedGrammar inputGrammar, CompletionAlgorithm completionSettings) {
        completeRuntime.startTimer();
        CompletionState currentState = new CompletionState(inputGrammar, null);
        CompletionStateLoss completionStateLoss = completionSettings.getCompletionStateLoss();
        double currentLoss = completionStateLoss.getLoss(currentState);
        int currentSearchDepth = 0;
        boolean madeProgress = true;

        // Cycle through all heuristics as long as there is still progress
        while (madeProgress) {
            madeProgress = false;

            for (CompletionHeuristic heuristic : completionSettings.getHeuristics()) {
                CompletionHeuristicStatisticCollector statisticCollector = heuristic.getStatisticCollector();
                statisticCollector.startTimer();

                boolean appliedHeuristic = true;
                while (appliedHeuristic) {
                    appliedHeuristic = false;

                    int heuristicLevel = 0;  // Keep track at which level the heuristic produces a success
                    for (CompletionState nextState : heuristic.applyHeuristic(currentState)) {
                        // Compute loss
                        double nextLoss = completionStateLoss.getLoss(nextState);
                        if (nextLoss < currentLoss) {
                            // Check if the new completion state is valid
                            boolean isValid = true;
                            for (GrammarValidity validityCheck : completionSettings.getValidityChecks()) {
                                if (!validityCheck.isValid(nextState)) {
                                    isValid = false;
                                    break;
                                }
                            }

                            if (isValid) {
                                // Update statistic
                                statisticCollector.incrementNumSuccess();
                                statisticCollector.addNumCriticalPairsRemoved(nextState.getCriticalPairs().size() - nextState.getCriticalPairs().size());
                                statisticCollector.incrementSuccessAtLevel(heuristicLevel);

                                System.out.println("Current number critical pairs: " + nextState.getCriticalPairs().size());
                                // Update state & loss
                                currentState = nextState;
                                currentLoss = nextLoss;

                                // Try to find another possible optimization using this strategy
                                appliedHeuristic = true;
                                // Cycle through all heuristics again later
                                madeProgress = true;
                                // Increment the search depth and check if we have to abort
                                currentSearchDepth++;
                                if ((maxSearchDepth > 0 && currentSearchDepth >= maxSearchDepth) || currentState.getCriticalPairs().size() == 0) {
                                    completeRuntime.stopTimer();
                                    return currentState;
                                }
                                // Reinitialize possible next states for the current heuristic
                                break;
                            } else {
                                statisticCollector.incrementNumGrammarValidityCheckFails();
                            }
                        } else {
                            statisticCollector.incrementNumLossFunctionFail();
                        }
                        heuristicLevel++;
                    }
                }

                statisticCollector.stopTimer();
            }
        }

        System.err.println("No more progress: Abort");
        completeRuntime.stopTimer();
        // Return the result
        return currentState;
    }

    @Override
    public JSONObject getDescription() {
        return new JSONObject(ImmutableMap.of(
                "name", "greedyCompletion",
                "maxSearchDepth", maxSearchDepth,
                "runtime", completeRuntime.getRuntime()
        ));
    }
}
