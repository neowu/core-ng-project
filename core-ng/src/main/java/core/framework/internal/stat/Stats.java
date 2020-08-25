package core.framework.internal.stat;

import core.framework.util.ASCII;
import core.framework.util.Maps;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * @author neo
 */
public class Stats {
    public final Map<String, Double> stats = Maps.newLinkedHashMap(); // to keep order in es
    public String errorCode;
    public String errorMessage;
    public Map<String, String> info;

    public String result() {
        if (errorCode == null) return "OK";
        return "WARN";
    }

    public void put(String key, double value) {
        stats.put(key, value);
    }

    public boolean checkHighUsage(double usage, double threshold, String name) {
        if (usage >= threshold) {
            NumberFormat format = NumberFormat.getPercentInstance();
            errorCode = "HIGH_" + ASCII.toUpperCase(name) + "_USAGE";
            errorMessage = name + " usage is too high, usage=" + format.format(usage);
            return true;
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
