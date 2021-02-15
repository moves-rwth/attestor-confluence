package de.rwth.i2.attestor.grammar.confluence.benchmark;

import de.rwth.i2.attestor.grammar.ConfluenceWrapperGrammar;
import de.rwth.i2.attestor.grammar.confluence.CriticalPair;
import de.rwth.i2.attestor.grammar.confluence.CriticalPairFinder;
import de.rwth.i2.attestor.grammar.confluence.Joinability;
import de.rwth.i2.attestor.grammar.confluence.main.ConfluenceTool;
import de.rwth.i2.attestor.io.tikzOutput.TikzExport;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

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
                ConfluenceWrapperGrammar grammar = BenchmarkRunner.getSeparationLogicNamedGrammar(grammarName);
                result.put(runBenchmarkForGrammar(grammar));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for (String grammarName : defaultGrammars) {
            ConfluenceWrapperGrammar grammar = ConfluenceTool.parseGrammar(grammarName);
            result.put(runBenchmarkForGrammar(grammar));
        }
        return result;
    }

    static JSONObject runBenchmarkForGrammar(ConfluenceWrapperGrammar grammar) {
        CriticalPairFinder criticalPairFinder = new CriticalPairFinder(grammar);
        JSONObject benchmarkResult = new JSONObject();
        benchmarkResult.put("resultData", criticalPairFinder.getJsonStatistic());
        benchmarkResult.put("grammarName", grammar.getGrammarName());
        int numberStronglyJoinable = 0;
        int numberWeaklyJoinable = 0;
        int numberNotJoinable = 0;

        for (CriticalPair criticalPair : criticalPairFinder.getCriticalPairs()) {
            switch (criticalPair.getJoinability()) {
                case WEAKLY_JOINABLE:
                    numberWeaklyJoinable++;
                    break;
                case STRONGLY_JOINABLE:
                    numberStronglyJoinable++;
                    break;
                case NOT_JOINABLE:
                    numberNotJoinable++;
                    break;
            }
        }

        benchmarkResult.put("numberWeaklyJoinable", numberWeaklyJoinable);
        benchmarkResult.put("numberStronglyJoinable", numberStronglyJoinable);
        benchmarkResult.put("numberNotJoinable", numberNotJoinable);

        String folder = "reports/initialPDFS/"+grammar.getGrammarName();
        new File(folder).mkdirs();
        try {
            TikzExport export = new TikzExport(folder + "/all_critical_pairs_" + grammar.getGrammarName() + ".tex", true);
            export.exportCriticalPairs(criticalPairFinder.getCriticalPairs());
            export.finishExport();

            export = new TikzExport(folder + "/problematic_critical_pairs_" + grammar.getGrammarName() + ".tex", true);
            export.exportCriticalPairs(criticalPairFinder.getCriticalPairsMaxJoinability(Joinability.WEAKLY_JOINABLE));
            export.finishExport();

            export = new TikzExport(folder + "/initial_grammar_" + grammar.getGrammarName() + ".tex", true);
            export.exportGrammar(grammar, true);
            export.finishExport();

        } catch (Exception e) {

        }

        return benchmarkResult;
    }

    static String getDateTime() {
        String pattern = "yyyy-MM-dd_HH_mm_ss_SSS";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.format(new Date());
    }

    public static void main(String[] args) {
        JSONArray result = new JSONArray();

        for (Object obj : runAllCriticalPairDetection()) {
            result.put((JSONObject) obj);
        }

        // Output to file
        try {
            new File("reports/critical_pairs").mkdirs();

            BufferedWriter writer = Files.newBufferedWriter(Paths.get("reports/critical_pairs/criticalPairDetection__" + getDateTime() + ".json"));
            result.write(writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
