package de.rwth.i2.attestor.grammar.confluence.benchmark;

import de.rwth.i2.attestor.grammar.NamedGrammar;
import de.rwth.i2.attestor.grammar.confluence.CriticalPairFinder;
import de.rwth.i2.attestor.grammar.confluence.Joinability;
import de.rwth.i2.attestor.grammar.confluence.completion.CompletionAlgorithm;
import de.rwth.i2.attestor.grammar.confluence.completion.CompletionState;
import de.rwth.i2.attestor.grammar.confluence.completion.ExampleCompletionAlgorithms;
import de.rwth.i2.attestor.grammar.confluence.main.ConfluenceTool;
import de.rwth.i2.attestor.io.tikzOutput.TikzExport;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
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
        CriticalPairFinder initialCriticalPairs = new CriticalPairFinder(grammar);
        int initialNumberCriticalPairs = initialCriticalPairs.getCriticalPairsMaxJoinability(Joinability.WEAKLY_JOINABLE).size();

        for (CompletionAlgorithm completionAlgorithm : getCompletionAlgorithms()) {
            System.out.println("Start completion benchmark. Grammar: " + grammar.getGrammarName() + " Completion Algorithm: " + completionAlgorithm.getAlgorithmIdentifier());
            CompletionState resultingCompletionState = completionAlgorithm.runCompletionAlgorithm(grammar);
            JSONObject benchmarkResult = new JSONObject();
            benchmarkResult.put("initialNumberCriticalPairs", initialNumberCriticalPairs);
            benchmarkResult.put("algorithmStatistic", completionAlgorithm.getStatistic());
            try {
                StringWriter stringWriter = new StringWriter();
                TikzExport export = new TikzExport(stringWriter, true, false);
                export.exportGrammar(resultingCompletionState.getGrammar(), true);
                export.finishExport();
                benchmarkResult.put("completedGrammarTex", stringWriter.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                StringWriter stringWriter = new StringWriter();
                TikzExport export = new TikzExport(stringWriter, true, false);
                export.exportCriticalPairs(resultingCompletionState.getCriticalPairs());
                export.finishExport();
                benchmarkResult.put("resultingCriticalPairsTex", stringWriter.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }

            result.put(benchmarkResult);
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
        JSONArray result = runAllCompletionBenchmarks();
        try {
            new File("reports/json").mkdirs();
            BufferedWriter writer = Files.newBufferedWriter(Paths.get("reports/json/completion.json"));
            result.write(writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
