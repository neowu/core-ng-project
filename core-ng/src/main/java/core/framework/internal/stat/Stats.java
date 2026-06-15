package core.framework.internal.stat;

import core.framework.log.Severity;
import core.framework.util.ASCII;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * @author neo
 */
public class Stats {
    // stats are collected every 10s (see LogConfig), so 6 cycles == 1 minute
    private static final int HIGH_USAGE_ESCALATION_COUNT = 6;

    public final Map<String, Double> stats = new HashMap<>(); // no need to keep insertion order, kibana sorts all keys on display

    // name -> high usage count
    private final Map<String, Integer> highUsageHistory;

    public Severity severity;
    public String errorCode;
    public String errorMessage;
    public Map<String, String> info;

    public Stats(Map<String, Integer> highUsageHistory) {
        this.highUsageHistory = highUsageHistory;
    }

    public String result() {
        if (errorCode == null) return "OK";
        return severity == Severity.ERROR ? "ERROR" : "WARN";   // default to warning
    }

    public void put(String key, double value) {
        stats.put(key, value);
    }

    // return if it's escalated error
    @SuppressWarnings("MoveVariableInsideIf")   // line 48 is false positive
    public boolean checkHighUsage(double usage, double threshold, String name) {
        if (usage <= threshold) {
            highUsageHistory.remove(name);
            return false;
        }

        Integer count = highUsageHistory.merge(name, 1, Integer::sum);

        if (errorCode == null) {    // do not overwrite existing warning, framework will check resource in order of importance
            NumberFormat format = NumberFormat.getPercentInstance();
            errorCode = "HIGH_" + ASCII.toUpperCase(name) + "_USAGE";
            errorMessage = name + " usage is too high, usage=" + format.format(usage);

            if (count < HIGH_USAGE_ESCALATION_COUNT) {
                severity = Severity.WARN;
            } else {
                severity = Severity.ERROR;
                highUsageHistory.remove(name);
                return true;
            }
        }

        return false;
    }

    public void info(String key, String value) {
        if (info == null) {
            info = new HashMap<>();
        }
        info.put(key, value);
    }
}
