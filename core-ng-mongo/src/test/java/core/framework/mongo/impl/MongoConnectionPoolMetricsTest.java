package core.framework.mongo.impl;

import com.mongodb.event.ConnectionCheckedInEvent;
import com.mongodb.event.ConnectionCheckedOutEvent;
import com.mongodb.event.ConnectionClosedEvent;
import com.mongodb.event.ConnectionCreatedEvent;
import core.framework.internal.stat.Stats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

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

        var event = mock(ConnectionCreatedEvent.class);
        metrics.connectionCreated(event);
        metrics.connectionCreated(event);

        metrics.collect(stats);
        assertThat(stats.stats).containsEntry("pool_mongo_active_count", 0.0);
        assertThat(stats.stats).containsEntry("pool_mongo_total_count", 2.0);

        metrics.connectionClosed(mock(ConnectionClosedEvent.class));
        metrics.connectionCheckedOut(mock(ConnectionCheckedOutEvent.class));

        metrics.collect(stats);
        assertThat(stats.stats).containsEntry("pool_mongo_active_count", 1.0);
        assertThat(stats.stats).containsEntry("pool_mongo_total_count", 1.0);

        metrics.connectionCheckedIn(mock(ConnectionCheckedInEvent.class));
        metrics.collect(stats);
        assertThat(stats.stats).containsEntry("pool_mongo_active_count", 0.0);
        assertThat(stats.stats).containsEntry("pool_mongo_total_count", 1.0);
    }
}
