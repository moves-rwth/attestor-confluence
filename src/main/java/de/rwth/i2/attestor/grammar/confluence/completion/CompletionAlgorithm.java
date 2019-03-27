package de.rwth.i2.attestor.grammar.confluence.completion;

import de.rwth.i2.attestor.grammar.NamedGrammar;
import de.rwth.i2.attestor.grammar.confluence.completion.heuristics.CompletionHeuristic;
import de.rwth.i2.attestor.grammar.confluence.completion.loss.CompletionStateLoss;
import de.rwth.i2.attestor.grammar.confluence.completion.strategies.CompletionStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CompletionAlgorithm {
    private List<CompletionHeuristic> heuristics;
    private CompletionStateLoss completionStateLoss;
    private CompletionStrategy completionStrategy;
    private int maxSearchDepth;

    public CompletionAlgorithm() {
        this.heuristics = new ArrayList<>();
        this.maxSearchDepth = 0;
    }

    // Builder style setters

    public CompletionAlgorithm setMaxSearchDepth(int maxSearchDepth) {
        this.maxSearchDepth = maxSearchDepth;
        return this;
    }

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

    public int getMaxSearchDepth() {
        return maxSearchDepth;
    }

    // Run algorithm

    public CompletionState runCompletionAlgorithm(NamedGrammar inputGrammar) {
        if (heuristics.size() == 0 || completionStateLoss == null || completionStrategy == null) {
            throw new IllegalStateException("The completion algorithm is missing a necessary parameter.");
        }
        return completionStrategy.executeCompletionStrategy(inputGrammar, this);
    }

}
