package core.framework.mongo.impl;

import com.mongodb.event.ConnectionCheckedInEvent;
import com.mongodb.event.ConnectionCheckedOutEvent;
import com.mongodb.event.ConnectionClosedEvent;
import com.mongodb.event.ConnectionCreatedEvent;
import com.mongodb.event.ConnectionPoolListener;
import core.framework.internal.stat.Metrics;
import core.framework.internal.stat.Stats;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author neo
 */
public class MongoConnectionPoolMetrics implements ConnectionPoolListener, Metrics {
    private final AtomicInteger total = new AtomicInteger(0);
    private final AtomicInteger active = new AtomicInteger(0);
    private final String statPrefix;

    public MongoConnectionPoolMetrics(String name) {
        statPrefix = "pool_mongo" + (name == null ? "" : '-' + name);
    }

    @Override
    public void collect(Stats stats) {
        stats.put(statName("total_count"), total.get());
        stats.put(statName("active_count"), active.get());
    }

    String statName(String statName) {
        return statPrefix + '_' + statName;
    }

    @Override
    public void connectionCheckedOut(ConnectionCheckedOutEvent event) {
        active.incrementAndGet();
    }

    @Override
    public void connectionCheckedIn(ConnectionCheckedInEvent event) {
        active.decrementAndGet();
    }

    @Override
    public void connectionCreated(ConnectionCreatedEvent event) {
        total.incrementAndGet();
    }

    @Override
    public void connectionClosed(ConnectionClosedEvent event) {
        total.decrementAndGet();
    }
}
