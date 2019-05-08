package de.rwth.i2.attestor.grammar.confluence.benchmark;

import de.rwth.i2.attestor.grammar.NamedGrammar;
import de.rwth.i2.attestor.grammar.confluence.completion.CompletionAlgorithm;
import de.rwth.i2.attestor.grammar.confluence.completion.ExampleCompletionAlgorithms;
import org.json.JSONArray;

import java.io.IOException;
import java.util.function.Supplier;

public class CompletionBenchmarkRunner {
    String[] completionGrammarNames = new String[] {
        "InTree",
        "InTreeLinkedLeaves",
        "LinkedTree1",
        "LinkedTree2",
        "SimpleDLL",
    };

    Supplier<CompletionAlgorithm>[] completionAlgorithms = new Supplier[] {
      ExampleCompletionAlgorithms::algorithm1
    };


    JSONArray runAllCompletionBenchmarks() {
        JSONArray benchmarkResults = new JSONArray();
        for (String grammarName : completionGrammarNames) {
            try {
                NamedGrammar grammar = BenchmarkRunner.getSeparationLogicNamedGrammar(grammarName);
                for (Supplier<CompletionAlgorithm> algorithmSupplier : completionAlgorithms) {
                    CompletionAlgorithm completionAlgorithm = algorithmSupplier.get();
                    completionAlgorithm.runCompletionAlgorithm(grammar);
                    benchmarkResults.put(completionAlgorithm.getStatistic());
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return benchmarkResults;
    }

}
