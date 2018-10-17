package core.framework.impl.resource;

import core.framework.internal.stat.Metrics;

import java.util.Map;

/**
 * @author neo
 */
public class PoolMetrics implements Metrics {
    private final Pool<?> pool;

    public PoolMetrics(Pool<?> pool) {
        this.pool = pool;
    }

    @Override
    public void collect(Map<String, Double> stats) {
        stats.put(statName("total_count"), (double) pool.totalCount());
        stats.put(statName("active_count"), (double) pool.activeCount());
    }

    String statName(String statName) {
        return "pool_" + pool.name + '_' + statName;
    }
}
