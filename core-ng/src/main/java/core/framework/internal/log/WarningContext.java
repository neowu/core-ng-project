package core.framework.internal.log;

import core.framework.log.IOWarning;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static core.framework.log.Markers.errorCode;

/**
 * @author neo
 */
public final class WarningContext {
    private static final Logger LOGGER = LoggerFactory.getLogger(WarningContext.class);

    public static PerformanceWarning @Nullable [] warnings(IOWarning[] warnings) {
        if (warnings.length == 0) return null;

        var results = new PerformanceWarning[warnings.length];
        for (int i = 0; i < warnings.length; i++) {
            IOWarning warning = warnings[i];
            String operation = warning.operation();
            PerformanceWarning defaultWarning = defaultWarning(operation);

            int maxOperations = warning.maxOperations();
            if (maxOperations < 0 && defaultWarning != null) maxOperations = defaultWarning.maxOperations();

            long maxElapsed = warning.maxElapsedInMs() * 1_000_000L;
            if (maxElapsed < 0 && defaultWarning != null) maxElapsed = defaultWarning.maxElapsed();

            int maxReads = warning.maxReads();
            if (maxReads < 0 && defaultWarning != null) maxReads = defaultWarning.maxReads();

            int maxTotalReads = warning.maxTotalReads();
            if (maxTotalReads < 0 && defaultWarning != null) maxTotalReads = defaultWarning.maxTotalReads();

            int maxTotalWrites = warning.maxTotalWrites();
            if (maxTotalWrites < 0 && defaultWarning != null) maxTotalWrites = defaultWarning.maxTotalWrites();

            long maxReadBytes = warning.maxReadBytes();
            if (maxReadBytes < 0 && defaultWarning != null) maxReadBytes = defaultWarning.maxReadBytes();

            long maxTotalReadBytes = warning.maxTotalReadBytes();
            if (maxTotalReadBytes < 0 && defaultWarning != null) maxTotalReadBytes = defaultWarning.maxTotalReadBytes();

            results[i] = new PerformanceWarning(operation, maxOperations, maxElapsed, maxReads, maxTotalReads, maxTotalWrites, maxReadBytes, maxTotalReadBytes);
        }
        return results;
    }

    @Nullable
    static PerformanceWarning defaultWarning(String operation) {
        return switch (operation) {
            case "db" -> new PerformanceWarning("db", 2000, Duration.ofSeconds(2).toNanos(), 2_000, 10_000, 10_000, -1, -1);
            case "redis" -> new PerformanceWarning("redis", 2000, Duration.ofMillis(500).toNanos(), 1_000, 10_000, 10_000, -1, -1);
            case "elasticsearch" -> new PerformanceWarning("elasticsearch", 2000, Duration.ofSeconds(5).toNanos(), 2_000, 10_000, 10_000, -1, -1);
            case "mongo" -> new PerformanceWarning("mongo", 2000, Duration.ofSeconds(5).toNanos(), 2_000, 10_000, 10_000, -1, -1);
            case "cache" -> new PerformanceWarning("cache", 1000, Duration.ofSeconds(1).toNanos(), 1_000, 1_000, -1, 500_000, 2_000_000);
            case "http" -> new PerformanceWarning("http", 200, Duration.ofSeconds(10).toNanos(), -1, -1, -1, 1_000_000, 10_000_000);
            default -> null;
        };
    }

    long maxProcessTimeInNano;

    public void maxProcessTimeInNano(long maxProcessTimeInNano) {
        this.maxProcessTimeInNano = maxProcessTimeInNano;
        LOGGER.debug("maxProcessTime={}", maxProcessTimeInNano);
    }

    public void checkMaxProcessTime(long elapsed) {
        if (maxProcessTimeInNano > 0 && elapsed > maxProcessTimeInNano) {
            LOGGER.warn(errorCode("SLOW_PROCESS"), "action took longer than of max process time, maxProcessTime={}, elapsed={}", Duration.ofNanos(maxProcessTimeInNano), Duration.ofNanos(elapsed));
        }
    }
}
