package de.rwth.i2.attestor.grammar.confluence.benchmark;

import de.rwth.i2.attestor.grammar.NamedGrammar;
import de.rwth.i2.attestor.grammar.confluence.CriticalPairFinder;
import de.rwth.i2.attestor.grammar.confluence.Joinability;
import de.rwth.i2.attestor.grammar.confluence.completion.CompletionAlgorithm;
import de.rwth.i2.attestor.grammar.confluence.completion.CompletionState;
import de.rwth.i2.attestor.grammar.confluence.completion.ExampleCompletionAlgorithms;
import de.rwth.i2.attestor.grammar.confluence.main.ConfluenceTool;
import de.rwth.i2.attestor.io.tikzOutput.TikzExport;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

    static Iterable<NamedGrammar> getBenchmarkGrammars() {
        ArrayList<NamedGrammar> grammars = new ArrayList<>();
        for (String grammarName : completionGrammarNames) {
            try {
                NamedGrammar grammar = BenchmarkRunner.getSeparationLogicNamedGrammar(grammarName);
                grammars.add(grammar);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }

        for (String grammarName : predefinedGrammarNames) {
            NamedGrammar grammar = ConfluenceTool.parseGrammar(grammarName);
            grammars.add(grammar);
        }
        return grammars;
    }

    static void runBenchmarkForGrammarAndAlgorithm(NamedGrammar grammar, CompletionAlgorithm algorithm) {
        String fileName = getFileName(grammar.getGrammarName(), algorithm.getAlgorithmIdentifier());
        CriticalPairFinder initialCriticalPairs = new CriticalPairFinder(grammar);
        int initialNumberCriticalPairs = initialCriticalPairs.getCriticalPairsMaxJoinability(Joinability.WEAKLY_JOINABLE).size();

        System.out.println("Start completion benchmark. Grammar: " + grammar.getGrammarName() + " Completion Algorithm: " + algorithm.getAlgorithmIdentifier());

        // Run benchmark
        CompletionState resultingCompletionState = algorithm.runCompletionAlgorithm(grammar);

        // Get benchmark results
        JSONObject benchmarkResult = new JSONObject();
        benchmarkResult.put("grammarName", grammar.getGrammarName());
        benchmarkResult.put("initialNumberCriticalPairs", initialNumberCriticalPairs);
        benchmarkResult.put("finalNumberCriticalPairs", resultingCompletionState.getCriticalPairs().size());
        benchmarkResult.put("algorithmStatistic", algorithm.getStatistic());

        // Save resulting grammar
        try {
            StringWriter stringWriter = new StringWriter();
            TikzExport export = new TikzExport(stringWriter, true, false);
            export.exportGrammar(resultingCompletionState.getGrammar(), true);
            export.finishExport();
            benchmarkResult.put("completedGrammarTex", stringWriter.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Save remaining critical pairs
        try {
            StringWriter stringWriter = new StringWriter();
            TikzExport export = new TikzExport(stringWriter, true, false);
            export.exportCriticalPairs(resultingCompletionState.getCriticalPairs());
            export.finishExport();
            benchmarkResult.put("resultingCriticalPairsTex", stringWriter.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Save benchmark results to file
        try {
            new File("reports/json").mkdirs();
            BufferedWriter writer = Files.newBufferedWriter(Paths.get("reports/json/" + fileName + ".json"));
            benchmarkResult.write(writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static String getFileName(String grammarIdentifier, String algorithmIdentifier) {
        String pattern = "yyyy-MM-dd_HH_mm_ss_SSS";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

        String date = simpleDateFormat.format(new Date());
        return grammarIdentifier + "__" + algorithmIdentifier + "__" + date;
    }

    static void runAllCompletionBenchmarks(int i) {
        ExecutorService executor = Executors.newFixedThreadPool(i);
        for (NamedGrammar grammar :  getBenchmarkGrammars()) {
            for (CompletionAlgorithm algorithm : getCompletionAlgorithms()) {
                executor.execute(() -> runBenchmarkForGrammarAndAlgorithm(grammar, algorithm));
            }
        }
        executor.shutdown();
        try {
            executor.awaitTermination(-1, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {

        runAllCompletionBenchmarks(1);

    }

}
