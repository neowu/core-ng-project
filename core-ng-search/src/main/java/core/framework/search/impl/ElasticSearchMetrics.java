package core.framework.search.impl;

import core.framework.internal.stat.Metrics;
import core.framework.internal.stat.Stats;
import org.jspecify.annotations.Nullable;

public class ElasticSearchMetrics implements Metrics {
    private final String statPrefix;
    private final LogInstrumentation instrumentation;

    public ElasticSearchMetrics(@Nullable String name, LogInstrumentation instrumentation) {
        statPrefix = "es" + (name == null ? "" : '-' + name);
        this.instrumentation = instrumentation;
    }

    @Override
    public void collect(Stats stats) {
        stats.put(statName("active_requests"), instrumentation.activeRequests.max());
    }

    String statName(String statName) {
        return statPrefix + '_' + statName;
    }
}
