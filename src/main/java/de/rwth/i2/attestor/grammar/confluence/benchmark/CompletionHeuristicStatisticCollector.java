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
    // If the heuristic could not be applied a negative value is added containing the number of tries that were checked.
    private ArrayList<Integer> numTriesPerHeuristicInvocation = new ArrayList<>();

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
        numTriesPerHeuristicInvocation.add(numTries);
    }

    public void saveFailureAtTry(int numTries) {
        numTriesPerHeuristicInvocation.add(-numTries);
    }

    public JSONObject getJsonResult() {
        JSONObject result = new JSONObject();
        result.put("time", timer.getRuntime());
        result.put("numSuccess", numSuccess);
        result.put("numLossFunctionFail", numLossFunctionFail);
        result.put("numGrammarValidityCheckFail", numGrammarValidityCheckFail);
        result.put("numTriesPerHeuristicInvocation", new JSONArray(numTriesPerHeuristicInvocation));
        return result;
    }

}
