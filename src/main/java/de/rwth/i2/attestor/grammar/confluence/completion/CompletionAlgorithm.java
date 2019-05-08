package de.rwth.i2.attestor.grammar.confluence.completion;

import de.rwth.i2.attestor.grammar.NamedGrammar;
import de.rwth.i2.attestor.grammar.confluence.completion.heuristics.CompletionHeuristic;
import de.rwth.i2.attestor.grammar.confluence.completion.loss.CompletionStateLoss;
import de.rwth.i2.attestor.grammar.confluence.completion.strategies.CompletionStrategy;
import de.rwth.i2.attestor.grammar.confluence.completion.validity.GrammarValidity;
import org.json.JSONObject;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

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
 * Optionally it is possible to call 'addGrammarValidityCheck' to rule out grammars that don't fulfill some conditions.
 *
 */
public class CompletionAlgorithm {
    private final String algorithmIdentifier; // An identifier used to identify the completion
    private final List<CompletionHeuristic> heuristics;
    private final List<GrammarValidity> validityChecks;
    private CompletionStateLoss completionStateLoss;
    private CompletionStrategy completionStrategy;

    public CompletionAlgorithm(String algorithmIdentifier) {
        this.algorithmIdentifier = algorithmIdentifier;
        this.heuristics = new ArrayList<>();
        this.validityChecks = new ArrayList<>();
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

    public CompletionAlgorithm addGrammarValidityCheck(GrammarValidity validityCheck) {
        this.validityChecks.add(validityCheck);
        return this;
    }

    // Getters

    public List<CompletionHeuristic> getHeuristics() {
        return Collections.unmodifiableList(heuristics);
    }

    public List<GrammarValidity> getValidityChecks() {
        return Collections.unmodifiableList(validityChecks);
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

    public String getAlgorithmIdentifier() {
        return algorithmIdentifier;
    }

    public JSONObject getStatistic() {
        throw new NotImplementedException();
    }

}
