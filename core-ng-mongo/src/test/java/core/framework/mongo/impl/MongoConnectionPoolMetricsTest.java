package core.framework.mongo.impl;

import core.framework.internal.stat.Stats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class MongoConnectionPoolMetricsTest {
    private MongoConnectionPoolMetrics metrics;

    @BeforeEach
    void createMongoConnectionPoolMetrics() {
        metrics = new MongoConnectionPoolMetrics(null);
    }

    @Test
    void collect() {
        var stats = new Stats();
        metrics.connectionCreated(null);
        metrics.connectionCreated(null);

        metrics.collect(stats);
        assertThat(stats.stats).containsEntry("pool_mongo_active_count", 0.0);
        assertThat(stats.stats).containsEntry("pool_mongo_total_count", 2.0);

        metrics.connectionClosed(null);
        metrics.connectionCheckedOut(null);

        metrics.collect(stats);
        assertThat(stats.stats).containsEntry("pool_mongo_active_count", 1.0);
        assertThat(stats.stats).containsEntry("pool_mongo_total_count", 1.0);

        metrics.connectionCheckedIn(null);
        metrics.collect(stats);
        assertThat(stats.stats).containsEntry("pool_mongo_active_count", 0.0);
        assertThat(stats.stats).containsEntry("pool_mongo_total_count", 1.0);
    }
}
