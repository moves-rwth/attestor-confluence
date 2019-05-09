package de.rwth.i2.attestor.grammar.confluence.benchmark;

import de.rwth.i2.attestor.grammar.NamedGrammar;
import de.rwth.i2.attestor.grammar.confluence.CriticalPairFinder;
import de.rwth.i2.attestor.grammar.confluence.main.ConfluenceTool;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CriticalPairDetectionBenchmarkRunner {
    static String[] grammarNames = new String[] {
            "InTree",
            "InTreeLinkedLeaves",
            "LinkedTree1",
            "LinkedTree2",
            "SimpleDLL",
    };

    static String[] defaultGrammars = new String[] {
            "BT",
            "DLList",
            "SLList"
    };

    static JSONArray runAllCriticalPairDetection() {
        JSONArray result = new JSONArray();

        for (String grammarName : grammarNames) {
            try {
                NamedGrammar grammar = BenchmarkRunner.getSeparationLogicNamedGrammar(grammarName);
                result.put(runBenchmarkForGrammar(grammar));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for (String grammarName : defaultGrammars) {
            NamedGrammar grammar = ConfluenceTool.parseGrammar(grammarName);
            result.put(runBenchmarkForGrammar(grammar));
        }
        return result;
    }

    static JSONObject runBenchmarkForGrammar(NamedGrammar grammar) {
        CriticalPairFinder criticalPairFinder = new CriticalPairFinder(grammar);
        JSONObject benchmarkResult = new JSONObject();
        benchmarkResult.put("resultData", criticalPairFinder.getJsonStatistic());
        benchmarkResult.put("grammarName", grammar.getGrammarName());
        return benchmarkResult;
    }

    public static void main(String[] args) {
        JSONArray result = runAllCriticalPairDetection();
        // Output to file
        try {
            new File("reports/json").mkdirs();

            BufferedWriter writer = Files.newBufferedWriter(Paths.get("reports/json/criticalPairDetection.json"));
            result.write(writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
