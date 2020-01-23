package core.framework.internal.stat;

import core.framework.util.Maps;

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

    public void warn(String errorCode, String errorMessage) {
        if (this.errorCode == null) {
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
        }
    }

    public void put(String key, double value) {
        stats.put(key, value);
    }
}
