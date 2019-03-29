package de.rwth.i2.attestor.grammar.confluence.completion;

import de.rwth.i2.attestor.grammar.NamedGrammar;
import de.rwth.i2.attestor.grammar.confluence.completion.heuristics.CompletionHeuristic;
import de.rwth.i2.attestor.grammar.confluence.completion.loss.CompletionStateLoss;
import de.rwth.i2.attestor.grammar.confluence.completion.strategies.CompletionStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The CompletionAlgorithm class creates a custom combination of completion heuristics, loss function,
 * completion strategy.
 * After all necessary options have been set the algorithm is executed with the 'runCompletionAlgorithm' method.
 *
 * Before calling 'runCompletionAlgorithm' one must first call at least:
 * - setCompletionStateLoss
 * - setCompletionStrategy
 * - addHeuristic (at least once, but it is possible to add multiple heuristics)
 *
 */
public class CompletionAlgorithm {
    private List<CompletionHeuristic> heuristics;
    private CompletionStateLoss completionStateLoss;
    private CompletionStrategy completionStrategy;

    public CompletionAlgorithm() {
        this.heuristics = new ArrayList<>();
    }

    // Builder style setters

    public CompletionAlgorithm setCompletionStateLoss(CompletionStateLoss penalty) {
        this.completionStateLoss = penalty;
        return this;
    }

    public CompletionAlgorithm setCompletionStrategy(CompletionStrategy strategy) {
        this.completionStrategy = strategy;
        return this;
    }

    public CompletionAlgorithm addHeuristic(CompletionHeuristic heuristic) {
        this.heuristics.add(heuristic);
        return this;
    }

    // Getters

    public List<CompletionHeuristic> getHeuristics() {
        return Collections.unmodifiableList(heuristics);
    }

    public CompletionStateLoss getCompletionStateLoss() {
        return completionStateLoss;
    }

    public CompletionStrategy getCompletionStrategy() {
        return completionStrategy;
    }

    // Run algorithm

    /**
     * @throws IllegalStateException 'setCompletionStateLoss', 'setCompletionStrategy' and 'addHeuristic'
     * must be called at least once before calling this method
     */
    public CompletionState runCompletionAlgorithm(NamedGrammar inputGrammar) {
        if (heuristics.size() == 0 || completionStateLoss == null || completionStrategy == null) {
            throw new IllegalStateException("The completion algorithm is missing a necessary parameter.");
        }
        return completionStrategy.executeCompletionStrategy(inputGrammar, this);
    }

}
