package core.framework.impl.log.stat;

import java.util.Map;

/**
 * @author neo
 */
public interface StatsCollector {
    void collect(Map<String, Double> stats);
}
