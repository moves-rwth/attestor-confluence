package de.rwth.i2.attestor.grammar.confluence.benchmark;

import org.json.JSONObject;

import java.util.Map;

public class CompletionHeuristicStatisticCollector {
    private StartStopTimer timer = new StartStopTimer();
    private int numSuccess = 0;
    private int numUnsuccessful = 0;
    private int numNotLocalConcretizable = 0;
    private int numCriticalPairsRemoved = 0;

    // Saves at what level the heuristic was successful (first try, second try, ...)
    private Map<Integer, Integer> successAtLevel;

    public void startTimer() {
        timer.startTimer();
    }

    public void stopTimer() {
        timer.stopTimer();
    }

    public void incrementNumSuccess() {
        numSuccess++;
    }

    public void incrementNumUnsuccessful() {
        numUnsuccessful++;
    }

    public void incrementNumNotLocalConcretizable() {
        numNotLocalConcretizable++;
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
        result.put("numUnsuccessful", numUnsuccessful);
        result.put("numNotLocalConcretizable", numNotLocalConcretizable);
        result.put("numCriticalPairsRemoved", numCriticalPairsRemoved);
        result.put("successAtLevel", new JSONObject(successAtLevel));
        return result;
    }

}
