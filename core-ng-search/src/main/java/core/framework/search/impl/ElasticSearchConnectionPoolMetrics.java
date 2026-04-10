package core.framework.search.impl;

import core.framework.internal.stat.Metrics;
import core.framework.internal.stat.Stats;
import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.core5.pool.ConnPoolStats;
import org.apache.hc.core5.pool.PoolStats;
import org.jspecify.annotations.Nullable;

public class ElasticSearchConnectionPoolMetrics implements Metrics {
    private final String statPrefix;
    @Nullable
    ConnPoolStats<HttpRoute> poolStats;

    public ElasticSearchConnectionPoolMetrics(@Nullable String name) {
        statPrefix = "pool_es" + (name == null ? "" : '-' + name);
    }

    @Override
    public void collect(Stats stats) {
        if (poolStats != null) {
            PoolStats connectionPoolStats = poolStats.getTotalStats();
            stats.put(statName("total_count"), connectionPoolStats.getMax());
            stats.put(statName("active_count"), connectionPoolStats.getLeased());
        }
    }

    String statName(String statName) {
        return statPrefix + '_' + statName;
    }
}
