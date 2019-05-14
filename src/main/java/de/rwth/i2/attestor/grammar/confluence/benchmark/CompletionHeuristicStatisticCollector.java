package de.rwth.i2.attestor.grammar.confluence.benchmark;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CompletionHeuristicStatisticCollector {
    private StartStopTimer timer = new StartStopTimer();
    private int numSuccess = 0;
    private int numLossFunctionFail = 0;
    private int numGrammarValidityCheckFail = 0;

    // Saves for each heuristic invocation how many tries were needed to successfully apply the heuristic
    // The key is the number tries required and the value is the count of how often this occurred
    private Map<Integer,Integer> numTriesPerSuccessfulHeuristicInvocation = new HashMap<>();

    // Same as above for unsuccessful heuristic application
    private Map<Integer,Integer> numTriesPerUnsuccessfulHeuristicInvocation = new HashMap<>();

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

    public void saveSuccessAtTry(int numTries) {
        numTriesPerSuccessfulHeuristicInvocation.merge(numTries, 1, Integer::sum);
    }

    public void saveFailureAtTry(int numTries) {
        numTriesPerUnsuccessfulHeuristicInvocation.merge(numTries, 1, Integer::sum);
    }

    public JSONObject getJsonResult() {
        JSONObject result = new JSONObject();
        result.put("time", timer.getRuntime());
        result.put("numSuccess", numSuccess);
        result.put("numLossFunctionFail", numLossFunctionFail);
        result.put("numGrammarValidityCheckFail", numGrammarValidityCheckFail);
        result.put("numTriesPerSuccessfulHeuristicInvocation", new JSONObject(numTriesPerSuccessfulHeuristicInvocation));
        result.put("numTriesPerUnsuccessfulHeuristicInvocation", new JSONObject(numTriesPerUnsuccessfulHeuristicInvocation));
        return result;
    }

}
