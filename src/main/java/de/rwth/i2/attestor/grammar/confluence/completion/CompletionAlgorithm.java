package de.rwth.i2.attestor.grammar.confluence.completion;

import de.rwth.i2.attestor.grammar.NamedGrammar;
import de.rwth.i2.attestor.grammar.confluence.completion.heuristics.CompletionHeuristic;
import de.rwth.i2.attestor.grammar.confluence.completion.penalties.CompletionStatePenalty;
import de.rwth.i2.attestor.grammar.confluence.completion.strategies.CompletionStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CompletionAlgorithm {
    private List<CompletionHeuristic> heuristics;
    private CompletionStatePenalty completionStatePenalty;
    private CompletionStrategy completionStrategy;
    private int searchDepth;

    public CompletionAlgorithm() {
        this.heuristics = new ArrayList<>();
    }

    // Builder style setters

    public CompletionAlgorithm setSearchDepth(int searchDepth) {
        this.searchDepth = searchDepth;
        return this;
    }

    public CompletionAlgorithm setCompletionStatePenalty(CompletionStatePenalty penalty) {
        this.completionStatePenalty = penalty;
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

    public CompletionStatePenalty getCompletionStatePenalty() {
        return completionStatePenalty;
    }

    public CompletionStrategy getCompletionStrategy() {
        return completionStrategy;
    }

    public int getSearchDepth() {
        return searchDepth;
    }

    // Run algorithm

    public CompletionState runCompletionAlgorithm(NamedGrammar inputGrammar) {
        // TODO: Maybe check that all neccessary values have been set (throw exception if not)
        return completionStrategy.executeCompletionAlgorithm(inputGrammar, this);
    }

}
