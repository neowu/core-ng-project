package core.framework.internal.web.http;

import core.framework.internal.util.LRUMap;
import core.framework.util.Maps;
import core.framework.web.exception.TooManyRequestsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author neo
 */
public class RateControl {
    private final Logger logger = LoggerFactory.getLogger(RateControl.class);
    private final int maxEntries;

    public Map<String, RateConfig> config;
    private LRUMap<String, Rate> rates;

    public RateControl(int maxEntries) {
        this.maxEntries = maxEntries;
    }

    // config is always called during initialization, so no concurrency issue
    public void config(String group, int maxPermits, int fillRate, TimeUnit unit) {
        synchronized (this) {
            if (config == null) {
                config = Maps.newHashMap();
                rates = new LRUMap<>(maxEntries);
            }
        }
        double fillRatePerNano = ratePerNano(fillRate, unit);
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

    double ratePerNano(int rate, TimeUnit unit) {
        return rate / (double) unit.toNanos(1);
    }

    boolean acquire(String group, String clientIP) {
        RateConfig config = this.config.get(group);

        if (config == null) {
            logger.warn("can not find group, group={}", group);
            return true;    // skip if group is not defined
        }

        String key = group + "/" + clientIP;
        Rate rate;
        synchronized (this) {
            rate = rates.computeIfAbsent(key, k -> new Rate(config.maxPermits));
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
