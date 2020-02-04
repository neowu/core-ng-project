package core.framework.internal.stat;

import core.framework.util.ASCII;
import core.framework.util.Maps;

import java.text.NumberFormat;
import java.util.Map;

/**
 * @author neo
 */
public class Stats {
    public final Map<String, Double> stats = Maps.newLinkedHashMap(); // to keep order in es
    public String errorCode;
    public String errorMessage;

    public String result() {
        if (errorCode == null) return "OK";
        return "WARN";
    }

    public void put(String key, double value) {
        stats.put(key, value);
    }

    public void checkHighUsage(double used, double max, double threshold, String name) {
        double usage = used / max;
        if (usage >= threshold) {
            NumberFormat format = NumberFormat.getPercentInstance();
            errorCode = "HIGH_" + ASCII.toUpperCase(name) + "_USAGE";
            errorMessage = name + " usage is too high, usage=" + format.format(usage);
        }
    }

    public void checkHighUsage(double usagePercent, double threshold, String name) {
        if (usagePercent >= threshold) {
            NumberFormat format = NumberFormat.getPercentInstance();
            errorCode = "HIGH_" + ASCII.toUpperCase(name) + "_USAGE";
            errorMessage = name + " usage is too high, usage=" + format.format(usagePercent);
        }
    }
}
