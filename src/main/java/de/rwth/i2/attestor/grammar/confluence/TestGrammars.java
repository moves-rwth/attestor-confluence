package de.rwth.i2.attestor.grammar.confluence;

import de.rwth.i2.attestor.grammar.ConfluenceWrapperGrammar;
import de.rwth.i2.attestor.grammar.confluence.benchmark.BenchmarkRunner;
import de.rwth.i2.attestor.grammar.confluence.completion.CompletionState;
import de.rwth.i2.attestor.grammar.confluence.completion.ExampleCompletionAlgorithms;
import de.rwth.i2.attestor.grammar.confluence.completion.validity.LocalConcretizability;
import de.rwth.i2.attestor.grammar.confluence.main.ConfluenceTool;
import de.rwth.i2.attestor.grammar.typedness.GrammarTypedness;
import de.rwth.i2.attestor.io.tikzOutput.TikzExport;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;

public class TestGrammars {

    public static void main(String[] args) {
        /*
        temp("DLList");
        temp("BT");
        temp("BT_conf");
        temp("SLList");
         */
        getReportsForGrammar("SimpleDLL");
        getReportsForGrammar("InTree");
        getReportsForGrammar("InTreeLinkedLeaves");
        getReportsForGrammar("LinkedTree1");
        getReportsForGrammar("LinkedTree2");
    }

    private static void temp(String test) {
        System.out.println(test);
        ConfluenceWrapperGrammar grammar = ConfluenceTool.parseGrammar(test);
        if (LocalConcretizability.checkLocalConcretizability(grammar, new GrammarTypedness(grammar.getConcretizationGrammar()), true)) {
            System.out.println("The grammar is local concretizable");
        } else {
            System.out.println("The grammar might not be local concretizable");
        }
    }

    /**
     * Exports .tex files for grammar and critical pairs before and after completion.
     * Also outputs number of critical pairs before and after to stdout.
     */
    public static void getReportsForGrammar(String grammarName) {
        System.out.println(grammarName);
        try {
            new File("reports/" + grammarName ).mkdirs();
            ConfluenceWrapperGrammar grammar = BenchmarkRunner.getSeparationLogicNamedGrammar(grammarName);
            String fileName = "reports/" + grammarName + "/{0}_" + grammarName + ".tex";
            TikzExport export;

            if (LocalConcretizability.checkLocalConcretizability(grammar, new GrammarTypedness(grammar.getConcretizationGrammar()), true)) {
                System.out.println("The grammar is local concretizable");
            } else {
                System.out.println("The grammar might not be local concretizable");
            }

            // Export initial grammar
            export = new TikzExport(MessageFormat.format(fileName, "initial_grammar"), true);
            export.exportGrammar(grammar, true);
            export.finishExport();

            // Get initial critical pairs
            CriticalPairFinder finder = new CriticalPairFinder(grammar);
            Collection<CriticalPair> initialCriticalPairs = finder.getCriticalPairsMaxJoinability(Joinability.WEAKLY_JOINABLE);
            System.out.println("Number initial critical pairs: " + initialCriticalPairs.size());

            export = new TikzExport(MessageFormat.format(fileName, "initial_critical_pairs"), true);
            export.exportCriticalPairs(initialCriticalPairs);
            export.finishExport();

            // Run completion
            CompletionState completionResult = ExampleCompletionAlgorithms.combinedAlgorithm1().runCompletionAlgorithm(grammar);

            // Get resulting critical pairs
            Collection<CriticalPair> resultingCriticalPairs = completionResult.getCriticalPairs();
            System.out.println("Number final critical pairs: " + resultingCriticalPairs.size());
            export = new TikzExport(MessageFormat.format(fileName, "resulting_critical_pairs"), true);
            export.exportCriticalPairs(resultingCriticalPairs);
            export.finishExport();

            // Get resulting grammar
            ConfluenceWrapperGrammar resultingGrammar = completionResult.getGrammar();
            export = new TikzExport(MessageFormat.format(fileName, "resulting_grammar"), true);
            export.exportGrammar(resultingGrammar, true);
            export.finishExport();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
