package core.framework.internal.web.http;

import core.framework.internal.util.LRUMap;
import core.framework.util.Maps;
import core.framework.web.exception.TooManyRequestsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author neo
 */
public class RateControl {
    private final Logger logger = LoggerFactory.getLogger(RateControl.class);

    private final ReentrantLock lock = new ReentrantLock();
    private Map<String, RateConfig> config;
    private Map<String, Rate> rates;

    public void maxEntries(int entries) {
        lock.lock();
        try {
            rates = new LRUMap<>(entries);
        } finally {
            lock.unlock();
        }
    }

    // config is always called during initialization, so no concurrency issue
    public void config(String group, int maxPermits, int fillRate, Duration interval) {
        if (config == null) config = Maps.newHashMap();
        double fillRatePerNano = ratePerNano(fillRate, interval);
        RateConfig previous = config.put(group, new RateConfig(maxPermits, fillRatePerNano));
        if (previous != null) throw new Error("found duplicate group, group=" + group);
    }

    public void validateRate(String group, String clientIP) {
        logger.debug("acquire, group={}, clientIP={}", group, clientIP);
        boolean acquired = acquire(group, clientIP);
        if (!acquired) {
            throw new TooManyRequestsException("rate exceeded");
        }
    }

    public boolean hasGroup(String group) {
        return config != null && config.containsKey(group);
    }

    double ratePerNano(int rate, Duration interval) {
        return rate / (double) interval.toNanos();
    }

    boolean acquire(String group, String clientIP) {
        RateConfig config = this.config.get(group);

        if (config == null) {
            logger.warn("can not find group, group={}", group);
            return true;    // skip if group is not defined
        }

        String key = group + "/" + clientIP;
        Rate rate;
        lock.lock();
        try {
            rate = rates.computeIfAbsent(key, k -> new Rate(config.maxPermits));
        } finally {
            lock.unlock();
        }
        long currentTime = System.nanoTime();
        return rate.acquire(currentTime, config.maxPermits, config.fillRatePerNano);
    }

    record RateConfig(int maxPermits, double fillRatePerNano) {
    }

    static final class Rate {
        private final ReentrantLock lock = new ReentrantLock();
        double currentPermits;
        long lastUpdateTime;

        Rate(int currentPermits) {
            this.currentPermits = currentPermits;
            this.lastUpdateTime = System.nanoTime();
        }

        // under multi-thread condition, the order of acquires are not determined, currentTime can be earlier than lastUpdateTime (e.g. lastUpdateTime was updated by a later acquire first)
        boolean acquire(long currentTime, int maxPermits, double fillRatePerNano) {
            lock.lock();
            try {
                long timeElapsed = Math.max(0, currentTime - lastUpdateTime);
                currentPermits = Math.min(maxPermits, currentPermits + fillRatePerNano * timeElapsed);
                lastUpdateTime = lastUpdateTime + timeElapsed;

                if (currentPermits >= 1) {
                    currentPermits -= 1;
                    return true;
                } else {
                    return false;
                }
            } finally {
                lock.unlock();
            }
        }
    }
}
