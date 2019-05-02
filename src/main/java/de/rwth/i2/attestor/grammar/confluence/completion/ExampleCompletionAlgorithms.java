package de.rwth.i2.attestor.grammar.confluence.completion;

import de.rwth.i2.attestor.grammar.NamedGrammar;
import de.rwth.i2.attestor.grammar.confluence.CriticalPairFinder;
import de.rwth.i2.attestor.grammar.confluence.Joinability;
import de.rwth.i2.attestor.grammar.confluence.TestGrammars;
import de.rwth.i2.attestor.grammar.confluence.completion.heuristics.*;
import de.rwth.i2.attestor.grammar.confluence.completion.loss.NumberCriticalPairLoss;
import de.rwth.i2.attestor.grammar.confluence.completion.strategies.GreedyCompletion;
import de.rwth.i2.attestor.grammar.confluence.completion.validity.LocalConcretizability;
import de.rwth.i2.attestor.grammar.confluence.main.ConfluenceTool;
import de.rwth.i2.attestor.io.tikzOutput.TikzExport;

import java.io.IOException;


public class ExampleCompletionAlgorithms {

    public static CompletionState algorithm1(NamedGrammar inputGrammar) {
        return new CompletionAlgorithm()
                .setCompletionStrategy(new GreedyCompletion(0))
                .setCompletionStateLoss(new NumberCriticalPairLoss())
                //.addHeuristic(new AddRuleHandleWithSubgraphHeuristic())
                //.addHeuristic(new CompletionAbstractionBlockingHeuristic())
                .addHeuristic(new AddRulesNewNonterminalHeuristic())
                .addHeuristic(new JoinGeneratedNonterminalsHeuristic())
                .addHeuristic(new SingleNonterminalRuleAddingHeuristic())
                //.addHeuristic(new CompletionRuleRestrictionHeuristic(false, true))
                .addGrammarValidityCheck(new LocalConcretizability())
                .runCompletionAlgorithm(inputGrammar);
    }

    // TODO: Remove this method
    public static void main(String[] args) {
        NamedGrammar grammar = null; // = ConfluenceTool.parseGrammar("DLList");
        try {
            grammar = TestGrammars.getSeparationLogicNamedGrammar("LinkedTree1");
        } catch (IOException e) {
            e.printStackTrace();
        }
        CriticalPairFinder finder = new CriticalPairFinder(grammar);

        try {
            TikzExport exporter = new TikzExport("reports/initial-critical-pairs.tex", true);
            exporter.exportCriticalPairs(finder.getCriticalPairsMaxJoinability(Joinability.WEAKLY_JOINABLE));
            exporter.finishExport();
        } catch (IOException e) {
            System.err.println("IO Exception occurred");
        }

        int numberInitialCriticalPairs = finder.getCriticalPairsMaxJoinability(Joinability.WEAKLY_JOINABLE).size();
        System.out.println("Number initial critical pairs: " + numberInitialCriticalPairs);
        CompletionState resultingState = algorithm1(grammar);
        System.out.println("Number remaining critical pairs: " + resultingState.getCriticalPairs().size());

        try {
            TikzExport exporter = new TikzExport("reports/remaining-critical-pairs.tex", true);
            exporter.exportCriticalPairs(resultingState.getCriticalPairs());
            exporter.finishExport();
        } catch (IOException e) {
            System.err.println("IO Exception occurred");
        }

        try {
            TikzExport exporter = new TikzExport("reports/grammar-completed.tex", true);
            exporter.exportGrammar(resultingState.getGrammar(), true);
            exporter.finishExport();
        } catch (IOException e) {
            System.err.println("IO Exception occurred");
        }
    }
}
