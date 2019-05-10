package de.rwth.i2.attestor.grammar.confluence.benchmark;

import de.rwth.i2.attestor.grammar.NamedGrammar;
import de.rwth.i2.attestor.grammar.confluence.completion.CompletionAlgorithm;
import de.rwth.i2.attestor.grammar.confluence.completion.ExampleCompletionAlgorithms;
import de.rwth.i2.attestor.grammar.confluence.main.ConfluenceTool;
import org.json.JSONArray;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class CompletionBenchmarkRunner {
    static String[] completionGrammarNames = new String[] {
        "InTree",
        "InTreeLinkedLeaves",
        "LinkedTree1",
        "LinkedTree2",
        "SimpleDLL",
    };

    static String[] predefinedGrammarNames = new String[] {
        "DLList"
    };


    /**
     * Returns all method marked with the BenchmarkCompletionAlgorithm annotation
     */
    private static Iterable<CompletionAlgorithm> getCompletionAlgorithms() {
        List<CompletionAlgorithm> result = new ArrayList<>();
        for (Method m : ExampleCompletionAlgorithms.class.getDeclaredMethods()) {

            if (m.isAnnotationPresent(BenchmarkCompletionAlgorithm.class)) {
                try {
                    result.add((CompletionAlgorithm) m.invoke(null));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    static void runBenchmarksForGrammar(NamedGrammar grammar, JSONArray result) {
        for (CompletionAlgorithm completionAlgorithm : getCompletionAlgorithms()) {
            completionAlgorithm.runCompletionAlgorithm(grammar);
            result.put(completionAlgorithm.getStatistic());
        }
    }

    static JSONArray runAllCompletionBenchmarks() {
        JSONArray benchmarkResults = new JSONArray();
        for (String grammarName : completionGrammarNames) {
            try {
                NamedGrammar grammar = BenchmarkRunner.getSeparationLogicNamedGrammar(grammarName);
                runBenchmarksForGrammar(grammar, benchmarkResults);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }

        for (String grammarName : predefinedGrammarNames) {
            NamedGrammar grammar = ConfluenceTool.parseGrammar(grammarName);
            runBenchmarksForGrammar(grammar, benchmarkResults);
        }
        return benchmarkResults;
    }

    public static void main(String[] args) {
        runAllCompletionBenchmarks();
    }

}
