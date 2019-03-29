package de.rwth.i2.attestor.grammar.confluence.completion;

import de.rwth.i2.attestor.grammar.NamedGrammar;
import de.rwth.i2.attestor.grammar.confluence.CriticalPairFinder;
import de.rwth.i2.attestor.grammar.confluence.Joinability;
import de.rwth.i2.attestor.grammar.confluence.completion.heuristics.AddRulesNewNonterminalHeuristic;
import de.rwth.i2.attestor.grammar.confluence.completion.heuristics.CompletionAbstractionBlockingHeuristic;
import de.rwth.i2.attestor.grammar.confluence.completion.heuristics.CompletionRuleRestrictionHeuristic;
import de.rwth.i2.attestor.grammar.confluence.completion.loss.NumberCriticalPairLoss;
import de.rwth.i2.attestor.grammar.confluence.completion.strategies.GreedyCompletion;
import de.rwth.i2.attestor.grammar.confluence.main.ConfluenceTool;

public class ExampleCompletionAlgorithms {

    public static CompletionState algorithm1(NamedGrammar inputGrammar) {
        return new CompletionAlgorithm()
                .setCompletionStrategy(new GreedyCompletion(0))
                .setCompletionStateLoss(new NumberCriticalPairLoss())
                .addHeuristic(new CompletionAbstractionBlockingHeuristic())
                //.addHeuristic(new AddRulesNewNonterminalHeuristic())
                .addHeuristic(new CompletionRuleRestrictionHeuristic(false, true))
                .runCompletionAlgorithm(inputGrammar);
    }

    // TODO: Remove this method
    public static void main(String[] args) {
        NamedGrammar grammar = ConfluenceTool.parseGrammar("DLList");
        CriticalPairFinder finder = new CriticalPairFinder(grammar);
        int numberInitialCriticalPairs = finder.getCriticalPairsMaxJoinability(Joinability.WEAKLY_JOINABLE).size();
        System.out.println("Number initial critical pairs: " + numberInitialCriticalPairs);
        CompletionState resultingState = algorithm1(grammar);
        System.out.println("Number remaining critical pairs: " + resultingState.getCriticalPairs().size());
    }

}
