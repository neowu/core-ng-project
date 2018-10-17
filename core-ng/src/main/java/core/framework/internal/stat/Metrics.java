package core.framework.internal.stat;

import java.util.Map;

/**
 * @author neo
 */
public interface Metrics {
    void collect(Map<String, Double> stats);
}
