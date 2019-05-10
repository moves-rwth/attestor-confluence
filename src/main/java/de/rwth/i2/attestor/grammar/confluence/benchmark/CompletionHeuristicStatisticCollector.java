package de.rwth.i2.attestor.grammar.confluence.benchmark;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CompletionHeuristicStatisticCollector {
    private StartStopTimer timer = new StartStopTimer();
    private int numSuccess = 0;
    private int numLossFunctionFail = 0;
    private int numGrammarValidityCheckFail = 0;
    private int numCriticalPairsRemoved = 0;

    // Saves at what level the heuristic was successful (first try, second try, ...)
    private Map<Integer, Integer> successAtLevel = new HashMap<>();

    public void startTimer() {
        timer.startTimer();
    }

    public void stopTimer() {
        timer.stopTimer();
    }

    public void incrementNumSuccess() {
        numSuccess++;
    }

    public void incrementNumLossFunctionFail() {
        numLossFunctionFail++;
    }

    public void incrementNumGrammarValidityCheckFails() {
        numGrammarValidityCheckFail++;
    }

    public void addNumCriticalPairsRemoved(int criticalPairsRemoved) {
        numCriticalPairsRemoved += criticalPairsRemoved;
    }

    public void incrementSuccessAtLevel(int level) {
        successAtLevel.merge(level, 1, Integer::sum);
    }

    public JSONObject getJsonResult() {
        JSONObject result = new JSONObject();
        result.put("time", timer.getRuntime());
        result.put("numSuccess", numSuccess);
        result.put("numLossFunctionFail", numLossFunctionFail);
        result.put("numGrammarValidityCheckFail", numGrammarValidityCheckFail);
        result.put("numCriticalPairsRemoved", numCriticalPairsRemoved);
        result.put("successAtLevel", new JSONObject(successAtLevel));
        return result;
    }

}
