package core.framework.internal.resource;

import core.framework.internal.stat.Metrics;
import core.framework.internal.stat.Stats;

/**
 * @author neo
 */
public class PoolMetrics implements Metrics {
    private final Pool<?> pool;

    public PoolMetrics(Pool<?> pool) {
        this.pool = pool;
    }

    @Override
    public void collect(Stats stats) {
        stats.put(statName("total_count"), pool.totalCount());
        stats.put(statName("active_count"), pool.activeCount());
    }

    String statName(String statName) {
        return "pool_" + pool.name + '_' + statName;
    }
}
