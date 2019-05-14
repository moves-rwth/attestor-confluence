package de.rwth.i2.attestor.grammar.confluence.completion;

import de.rwth.i2.attestor.grammar.NamedGrammar;
import de.rwth.i2.attestor.grammar.confluence.CriticalPairFinder;
import de.rwth.i2.attestor.grammar.confluence.Joinability;
import de.rwth.i2.attestor.grammar.confluence.benchmark.BenchmarkCompletionAlgorithm;
import de.rwth.i2.attestor.grammar.confluence.benchmark.BenchmarkRunner;
import de.rwth.i2.attestor.grammar.confluence.completion.heuristics.*;
import de.rwth.i2.attestor.grammar.confluence.completion.loss.NumberCriticalPairLoss;
import de.rwth.i2.attestor.grammar.confluence.completion.strategies.GreedyCompletion;
import de.rwth.i2.attestor.grammar.confluence.completion.validity.CheckDataStructureGrammar;
import de.rwth.i2.attestor.grammar.confluence.completion.validity.LocalConcretizability;
import de.rwth.i2.attestor.io.tikzOutput.TikzExport;

import java.io.IOException;

public class ExampleCompletionAlgorithms {

    // Completion Algorithms with Single

    //@BenchmarkCompletionAlgorithm // Does not currently work -> So it is not included in the benchmark
    public static CompletionAlgorithm ruleHandleWithSubgraph() {
        return new CompletionAlgorithm("ruleHandleWithSubgraph")
                .setCompletionStrategy(new GreedyCompletion(0))
                .setCompletionStateLoss(new NumberCriticalPairLoss())
                .addHeuristic(new AddRuleHandleWithSubgraphHeuristic())
                .addGrammarValidityCheck(new LocalConcretizability())
                .addGrammarValidityCheck(new CheckDataStructureGrammar());
    }

    @BenchmarkCompletionAlgorithm
    public static CompletionAlgorithm completionAbstractionBlocking() {
        return new CompletionAlgorithm("completionAbstractionBlocking")
                .setCompletionStrategy(new GreedyCompletion(0))
                .setCompletionStateLoss(new NumberCriticalPairLoss())
                .addHeuristic(new CompletionAbstractionBlockingHeuristic())
                .addGrammarValidityCheck(new LocalConcretizability())
                .addGrammarValidityCheck(new CheckDataStructureGrammar());
    }

    @BenchmarkCompletionAlgorithm
    public static CompletionAlgorithm addRulesNewNonterminalHeuristic() {
        return new CompletionAlgorithm("addRulesNewNonterminalHeuristic")
                .setCompletionStrategy(new GreedyCompletion(0))
                .setCompletionStateLoss(new NumberCriticalPairLoss())
                .addHeuristic(new AddRulesNewNonterminalHeuristic())
                .addGrammarValidityCheck(new LocalConcretizability())
                .addGrammarValidityCheck(new CheckDataStructureGrammar());
    }

    @BenchmarkCompletionAlgorithm
    public static CompletionAlgorithm joinGeneratedNonterminals() {
        return new CompletionAlgorithm("joinGeneratedNonterminals")
                .setCompletionStrategy(new GreedyCompletion(0))
                .setCompletionStateLoss(new NumberCriticalPairLoss())
                .addHeuristic(new AddRulesNewNonterminalHeuristic())
                .addHeuristic(new JoinGeneratedNonterminalsHeuristic())
                .addGrammarValidityCheck(new LocalConcretizability())
                .addGrammarValidityCheck(new CheckDataStructureGrammar());
    }

    @BenchmarkCompletionAlgorithm
    public static CompletionAlgorithm singleNonterminalRuleAddingHeuristic() {
        return new CompletionAlgorithm("singleNonterminalRuleAddingHeuristic")
                .setCompletionStrategy(new GreedyCompletion(0))
                .setCompletionStateLoss(new NumberCriticalPairLoss())
                .addHeuristic(new AddRulesNewNonterminalHeuristic())
                .addHeuristic(new SingleNonterminalRuleAddingHeuristic())
                .addGrammarValidityCheck(new LocalConcretizability())
                .addGrammarValidityCheck(new CheckDataStructureGrammar());
    }

    @BenchmarkCompletionAlgorithm
    public static CompletionAlgorithm ruleRestriction() {
        return new CompletionAlgorithm("ruleRestriction")
                .setCompletionStrategy(new GreedyCompletion(0))
                .setCompletionStateLoss(new NumberCriticalPairLoss())
                .addHeuristic(new CompletionRuleRestrictionHeuristic(false, true))
                .addGrammarValidityCheck(new LocalConcretizability())
                .addGrammarValidityCheck(new CheckDataStructureGrammar());
    }

    // Combined completion algorithms

    @BenchmarkCompletionAlgorithm
    public static CompletionAlgorithm onlyRuleAdding() {
        return new CompletionAlgorithm("onlyRuleAdding")
                .setCompletionStrategy(new GreedyCompletion(0))
                .setCompletionStateLoss(new NumberCriticalPairLoss())
                .addHeuristic(new AddRulesNewNonterminalHeuristic())
                .addHeuristic(new JoinGeneratedNonterminalsHeuristic())
                .addHeuristic(new SingleNonterminalRuleAddingHeuristic())
                .addGrammarValidityCheck(new LocalConcretizability())
                .addGrammarValidityCheck(new CheckDataStructureGrammar());
    }

    @BenchmarkCompletionAlgorithm
    public static CompletionAlgorithm onlyRuleAddingNotLocalConcretizable() {
        return new CompletionAlgorithm("onlyRuleAddingNotLocalConcretizable")
                .setCompletionStrategy(new GreedyCompletion(0))
                .setCompletionStateLoss(new NumberCriticalPairLoss())
                .addHeuristic(new AddRulesNewNonterminalHeuristic())
                .addHeuristic(new JoinGeneratedNonterminalsHeuristic())
                .addHeuristic(new SingleNonterminalRuleAddingHeuristic())
                .addGrammarValidityCheck(new CheckDataStructureGrammar());
    }

    @BenchmarkCompletionAlgorithm
    public static CompletionAlgorithm combinedAlgorithm1() {
        return new CompletionAlgorithm("combinedAlgorithm1")
                .setCompletionStrategy(new GreedyCompletion(0))
                .setCompletionStateLoss(new NumberCriticalPairLoss())
                .addHeuristic(new CompletionAbstractionBlockingHeuristic())
                .addHeuristic(new AddRulesNewNonterminalHeuristic())
                .addHeuristic(new JoinGeneratedNonterminalsHeuristic())
                .addHeuristic(new SingleNonterminalRuleAddingHeuristic())
                .addHeuristic(new CompletionRuleRestrictionHeuristic(false, true))
                .addGrammarValidityCheck(new LocalConcretizability())
                .addGrammarValidityCheck(new CheckDataStructureGrammar());
    }

    @BenchmarkCompletionAlgorithm
    public static CompletionAlgorithm combinedAlgorithm2() {  // Main difference to combinedAlgorithm1 is that rule restriction has higher priority
        return new CompletionAlgorithm("combinedAlgorithm2")
                .setCompletionStrategy(new GreedyCompletion(0))
                .setCompletionStateLoss(new NumberCriticalPairLoss())
                .addHeuristic(new CompletionAbstractionBlockingHeuristic())
                .addHeuristic(new CompletionRuleRestrictionHeuristic(false, true))
                .addHeuristic(new AddRulesNewNonterminalHeuristic())
                .addHeuristic(new JoinGeneratedNonterminalsHeuristic())
                .addHeuristic(new SingleNonterminalRuleAddingHeuristic())
                .addGrammarValidityCheck(new LocalConcretizability())
                .addGrammarValidityCheck(new CheckDataStructureGrammar());
    }

    @BenchmarkCompletionAlgorithm
    public static CompletionAlgorithm combinedAlgorithm1NoLocalConcretizabilityCheck() {
        return new CompletionAlgorithm("combinedAlgorithm1NoLocalConcretizabilityCheck")
                .setCompletionStrategy(new GreedyCompletion(0))
                .setCompletionStateLoss(new NumberCriticalPairLoss())
                .addHeuristic(new CompletionAbstractionBlockingHeuristic())
                .addHeuristic(new AddRulesNewNonterminalHeuristic())
                .addHeuristic(new JoinGeneratedNonterminalsHeuristic())
                .addHeuristic(new SingleNonterminalRuleAddingHeuristic())
                .addHeuristic(new CompletionRuleRestrictionHeuristic(false, true))
                .addGrammarValidityCheck(new CheckDataStructureGrammar());
    }

    @BenchmarkCompletionAlgorithm
    public static CompletionAlgorithm combinedAlgorithm2NoLocalConcretizabilityCheck() {  // Main difference to combinedAlgorithm1 is that rule restriction has higher priority
        return new CompletionAlgorithm("combinedAlgorithm2NoLocalConcretizabilityCheck")
                .setCompletionStrategy(new GreedyCompletion(0))
                .setCompletionStateLoss(new NumberCriticalPairLoss())
                .addHeuristic(new CompletionAbstractionBlockingHeuristic())
                .addHeuristic(new CompletionRuleRestrictionHeuristic(false, true))
                .addHeuristic(new AddRulesNewNonterminalHeuristic())
                .addHeuristic(new JoinGeneratedNonterminalsHeuristic())
                .addHeuristic(new SingleNonterminalRuleAddingHeuristic())
                .addGrammarValidityCheck(new CheckDataStructureGrammar());
    }



    // TODO: Remove this method
    public static void main(String[] args) {
        NamedGrammar grammar = null; // = ConfluenceTool.parseGrammar("DLList");
        try {
            grammar = BenchmarkRunner.getSeparationLogicNamedGrammar("LinkedTree1");
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
        CompletionState resultingState = combinedAlgorithm1().runCompletionAlgorithm(grammar);
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
