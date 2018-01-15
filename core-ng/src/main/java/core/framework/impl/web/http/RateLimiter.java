package core.framework.impl.web.http;

import core.framework.util.Exceptions;
import core.framework.util.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author neo
 */
class RateLimiter {
    private final Logger logger = LoggerFactory.getLogger(RateLimiter.class);
    private final Map<String, RateConfig> config = Maps.newHashMap();
    private final LRUMap<String, Rate> rates;

    RateLimiter(int maxEntries) {
        rates = new LRUMap<>(maxEntries);
    }

    public void config(String group, int maxPermits, int fillRate, TimeUnit unit) {
        double fillRatePerNano = ratePerNano(fillRate, unit);
        RateConfig previous = config.put(group, new RateConfig(maxPermits, fillRatePerNano));
        if (previous != null) throw Exceptions.error("found duplicate group, group={}", group);
    }

    double ratePerNano(int rate, TimeUnit unit) {
        return rate / (double) unit.toNanos(1);
    }

    public boolean acquire(String group, String clientIP) {
        RateConfig config = this.config.get(group);

        if (config == null) {
            logger.warn("can not find group, group={}", group);
            return true;    // skip if group is not defined
        }

        String key = group + "/" + clientIP;
        Rate rate;
        synchronized (this) {
            rate = this.rates.computeIfAbsent(key, k -> new Rate(config.maxPermits));
        }
        long currentTime = System.nanoTime();
        return rate.acquire(currentTime, config.maxPermits, config.fillRatePerNano);
    }

    static final class RateConfig {
        final int maxPermits;
        final double fillRatePerNano;

        RateConfig(int maxPermits, double fillRatePerNano) {
            this.maxPermits = maxPermits;
            this.fillRatePerNano = fillRatePerNano;
        }
    }

    static final class Rate {
        volatile double currentPermits;
        volatile long lastUpdateTime;

        Rate(int currentPermits) {
            this.currentPermits = currentPermits;
            this.lastUpdateTime = System.nanoTime();
        }

        boolean acquire(long currentTime, int maxPermits, double fillRatePerNano) {
            synchronized (this) {
                long timeElapsed = currentTime - lastUpdateTime;
                currentPermits = Math.min(maxPermits, currentPermits + fillRatePerNano * timeElapsed);
                lastUpdateTime = currentTime;

                if (currentPermits >= 1) {
                    currentPermits -= 1;
                    return true;
                } else {
                    return false;
                }
            }
        }
    }
}
