package de.rwth.i2.attestor.grammar.confluence.completion.heuristics;

import com.google.common.collect.ImmutableMap;
import de.rwth.i2.attestor.grammar.confluence.benchmark.CompletionHeuristicStatisticCollector;
import de.rwth.i2.attestor.grammar.confluence.completion.CompletionState;
import org.json.JSONObject;

/**
 * Interface for all completion heuristics. A completion heuristic returns all possible ways the heuristic can be applied
 * in the current state.
 * A heuristic tries to modify the grammar so it is "closer" to being confluent.
 *
 * A heuristic is not allowed to remove and add rules in the same step. If rules are added their original rule idx must
 * be larger than the other original rule indices.
 */
public interface CompletionHeuristic {
    CompletionHeuristicStatisticCollector statistic = new CompletionHeuristicStatisticCollector();

    /**
     * @param state The state on which the heuristic should be applied
     * @return All possible immediate successors of the input state according to the heuristic
     */
    Iterable<CompletionState> applyHeuristic(CompletionState state);

    String getIdentifier();

    default JSONObject getStatistic() {
        return statistic.getJsonResult();
    }

    default JSONObject getSettings() {
        return new JSONObject();
    }

    /**
     * @return A summary of the settings of the heuristic and benchmark statistics
     */
    default JSONObject getSummary() {
        return new JSONObject(ImmutableMap.of(
                "identifier", getIdentifier(),
                "settings", getSettings(),
                "statistic", getStatistic()
        ));
    }

    default CompletionHeuristicStatisticCollector getStatisticCollector() {
        return statistic;
    }

}
