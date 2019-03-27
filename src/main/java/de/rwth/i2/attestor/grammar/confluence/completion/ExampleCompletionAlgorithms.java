package de.rwth.i2.attestor.grammar.confluence.completion;

import de.rwth.i2.attestor.grammar.NamedGrammar;
import de.rwth.i2.attestor.grammar.confluence.completion.heuristics.AddRulesNewNonterminalHeuristic;
import de.rwth.i2.attestor.grammar.confluence.completion.heuristics.CompletionRuleRestrictionHeuristic;
import de.rwth.i2.attestor.grammar.confluence.completion.loss.NumberCriticalPairLoss;
import de.rwth.i2.attestor.grammar.confluence.completion.strategies.GreedyCompletion;

public class ExampleCompletionAlgorithms {

    public CompletionState algorithm1(NamedGrammar inputGrammar) {
        return new CompletionAlgorithm()
                .setMaxSearchDepth(0)
                .setCompletionStrategy(new GreedyCompletion())
                .setCompletionStateLoss(new NumberCriticalPairLoss())
                .addHeuristic(new AddRulesNewNonterminalHeuristic())
                .addHeuristic(new CompletionRuleRestrictionHeuristic())
                .runCompletionAlgorithm(inputGrammar);
    }

}
