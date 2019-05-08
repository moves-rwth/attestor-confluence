package de.rwth.i2.attestor.grammar.confluence.benchmark;

import de.rwth.i2.attestor.grammar.NamedGrammar;
import de.rwth.i2.attestor.grammar.confluence.CriticalPairFinder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class CriticalPairDetectionBenchmarkRunner {
    String[] grammarNames = new String[] {
            "InTree",
            "InTreeLinkedLeaves",
            "LinkedTree1",
            "LinkedTree2",
            "SimpleDLL",
    };

    JSONArray runAllCriticalPairDetection() {
        JSONArray result = new JSONArray();

        for (String grammarName : grammarNames) {
            try {
                NamedGrammar grammar = BenchmarkRunner.getSeparationLogicNamedGrammar(grammarName);
                result.put(runBenchmarkForGrammar(grammar));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    JSONObject runBenchmarkForGrammar(NamedGrammar grammar) {
        CriticalPairFinder criticalPairFinder = new CriticalPairFinder(grammar);
        return criticalPairFinder.getJsonStatistic();
    }
}
