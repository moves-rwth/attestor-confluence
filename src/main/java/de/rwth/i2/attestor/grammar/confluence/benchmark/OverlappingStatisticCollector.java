package de.rwth.i2.attestor.grammar.confluence.benchmark;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class OverlappingStatisticCollector {
    StartStopTimer runtime = new StartStopTimer();
    Map<Integer, Integer> pruningLevels = new HashMap<>();

    public void logPruning(int level) {
        // Increment value for
        pruningLevels.merge(level, 1, Integer::sum);
    }

    public void startTimer() {
        runtime.startTimer();
    }

    public void stopTimer() {
        runtime.stopTimer();
    }

    public JSONObject getJsonStatistic() {
        JSONObject result = new JSONObject();
        result.put("pruningLevels", new JSONObject(pruningLevels));
        result.put("time", runtime.getRuntime());
        return result;
    }
}
