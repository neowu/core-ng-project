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
    public final Map<String, Double> stats = new HashMap<>(); // no need to keep insertion order, kibana will sort all keys on display
    public Severity severity;
    public String errorCode;
    public String errorMessage;
    public Map<String, String> info;

    public String result() {
        if (errorCode == null) return "OK";
        return severity == Severity.ERROR ? "ERROR" : "WARN";   // default to warning
    }

    public void put(String key, double value) {
        stats.put(key, value);
    }

    public boolean checkHighUsage(double usage, double threshold, String name) {
        if (usage <= threshold) return false;

        if (errorCode == null) {    // do not overwrite existing warning, framework will check resource in order of importance
            NumberFormat format = NumberFormat.getPercentInstance();
            errorCode = "HIGH_" + ASCII.toUpperCase(name) + "_USAGE";
            errorMessage = name + " usage is too high, usage=" + format.format(usage);
        }

        return true;
    }

    public void info(String key, String value) {
        if (info == null) {
            info = new HashMap<>();
        }
        info.put(key, value);
    }
}
