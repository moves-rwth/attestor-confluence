package de.rwth.i2.attestor.grammar.confluence.completion;

import de.rwth.i2.attestor.grammar.NamedGrammar;
import de.rwth.i2.attestor.grammar.confluence.completion.heuristics.AddNewNonterminalRuleHeuristic;
import de.rwth.i2.attestor.grammar.confluence.completion.heuristics.CompletionRuleRestrictionHeuristic;
import de.rwth.i2.attestor.grammar.confluence.completion.penalties.NumberCriticalPairPenalty;
import de.rwth.i2.attestor.grammar.confluence.completion.strategies.GreedyCompletion;

public class ExampleCompletionAlgorithms {

    public CompletionState algorithm1(NamedGrammar inputGrammar) {
        return new CompletionAlgorithm()
                .setSearchDepth(0)
                .setCompletionStrategy(new GreedyCompletion())
                .setCompletionStatePenalty(new NumberCriticalPairPenalty())
                .addHeuristic(new AddNewNonterminalRuleHeuristic())
                .addHeuristic(new CompletionRuleRestrictionHeuristic())
                .runCompletionAlgorithm(inputGrammar);
    }

}
