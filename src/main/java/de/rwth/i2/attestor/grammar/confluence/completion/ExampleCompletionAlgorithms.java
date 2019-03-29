package de.rwth.i2.attestor.grammar.confluence.completion;

import de.rwth.i2.attestor.grammar.NamedGrammar;
import de.rwth.i2.attestor.grammar.confluence.completion.heuristics.AddRulesNewNonterminalHeuristic;
import de.rwth.i2.attestor.grammar.confluence.completion.heuristics.CompletionRuleRestrictionHeuristic;
import de.rwth.i2.attestor.grammar.confluence.completion.loss.NumberCriticalPairLoss;
import de.rwth.i2.attestor.grammar.confluence.completion.strategies.GreedyCompletion;

public class ExampleCompletionAlgorithms {

    public CompletionState algorithm1(NamedGrammar inputGrammar) {
        return new CompletionAlgorithm()
                .setCompletionStrategy(new GreedyCompletion(0))
                .setCompletionStateLoss(new NumberCriticalPairLoss())
                .addHeuristic(new AddRulesNewNonterminalHeuristic())
                .addHeuristic(new CompletionRuleRestrictionHeuristic(false))
                .runCompletionAlgorithm(inputGrammar);
    }

}
