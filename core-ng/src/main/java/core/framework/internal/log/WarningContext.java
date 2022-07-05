package core.framework.internal.log;

import core.framework.log.IOWarning;
import core.framework.util.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.Map;

import static core.framework.log.Markers.errorCode;
import static core.framework.util.ASCII.toUpperCase;

/**
 * @author neo
 */
public final class WarningContext {
    private static final Logger LOGGER = LoggerFactory.getLogger(WarningContext.class);

    // use static constant mapping for both performance and simplicity
    private static final Map<String, PerformanceWarning> DEFAULT_WARNINGS = Map.of(
        "db", new PerformanceWarning(2000, Duration.ofSeconds(5), 2000, 10_000, 10_000),
        "redis", new PerformanceWarning(2000, Duration.ofMillis(500), 1000, 10_000, 10_000),
        "elasticsearch", new PerformanceWarning(2000, Duration.ofSeconds(5), 2000, 10_000, 10_000),
        "mongo", new PerformanceWarning(2000, Duration.ofSeconds(5), 2000, 10_000, 10_000)
    );

    @Nullable
    public static Map<String, PerformanceWarning> warnings(IOWarning[] warnings) {
        if (warnings.length <= 0) return null;
        Map<String, PerformanceWarning> results = Maps.newHashMapWithExpectedSize(warnings.length);
        for (IOWarning warning : warnings) {
            PerformanceWarning defaultWarning = DEFAULT_WARNINGS.get(warning.operation());

            int maxOperations = warning.maxOperations();
            if (maxOperations < 0 && defaultWarning != null) maxOperations = defaultWarning.maxOperations;

            Duration maxElapsed = null;
            if (warning.maxElapsedInMs() > 0) maxElapsed = Duration.ofMillis(warning.maxElapsedInMs());
            if (maxElapsed == null && defaultWarning != null && defaultWarning.maxElapsed > 0) maxElapsed = Duration.ofNanos(defaultWarning.maxElapsed);

            int maxReads = warning.maxReads();
            if (maxReads < 0 && defaultWarning != null) maxReads = defaultWarning.maxReads;

            int maxTotalReads = warning.maxTotalReads();
            if (maxTotalReads < 0 && defaultWarning != null) maxTotalReads = defaultWarning.maxTotalReads;

            int maxTotalWrites = warning.maxTotalWrites();
            if (maxTotalWrites < 0 && defaultWarning != null) maxTotalWrites = defaultWarning.maxTotalWrites;
            results.put(warning.operation(), new PerformanceWarning(maxOperations, maxElapsed, maxReads, maxTotalReads, maxTotalWrites));
        }
        return results;
    }

    public boolean suppressSlowSQLWarning;
    public Map<String, PerformanceWarning> warnings;
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

    public void checkSingleIO(String operation, long elapsed, int readEntries) {
        PerformanceWarning warning = warning(operation);
        if (warning == null) return;

        if (warning.maxReads > 0 && readEntries > warning.maxReads) {
            LOGGER.warn(errorCode("HIGH_" + toUpperCase(operation) + "_IO"), "read too many entries once, operation={}, entries={}", operation, readEntries);
        }
        if (warning.maxElapsed > 0 && elapsed > warning.maxElapsed) {
            LOGGER.warn(errorCode("SLOW_" + toUpperCase(operation)), "slow operation, operation={}, elapsed={}", operation, Duration.ofNanos(elapsed));
        }
    }

    public void checkTotalIO(String operation, int count, int readEntries, int writeEntries) {
        PerformanceWarning warning = warning(operation);
        if (warning == null) return;

        if (warning.maxOperations > 0 && count > warning.maxOperations) {
            LOGGER.warn(errorCode("HIGH_" + toUpperCase(operation) + "_IO"), "too many operations, operation={}, count={}", operation, count);
        }
        if (warning.maxTotalReads > 0 && readEntries > warning.maxTotalReads) {
            LOGGER.warn(errorCode("HIGH_" + toUpperCase(operation) + "_IO"), "read too many entries, operation={}, entries={}", operation, readEntries);
        }
        if (warning.maxTotalWrites > 0 && writeEntries > warning.maxTotalWrites) {
            LOGGER.warn(errorCode("HIGH_" + toUpperCase(operation) + "_IO"), "write too many entries, operation={}, entries={}", operation, writeEntries);
        }
    }

    @Nullable
    private PerformanceWarning warning(String operation) {
        PerformanceWarning warning = null;
        if (warnings != null) warning = warnings.get(operation);
        if (warning == null) warning = DEFAULT_WARNINGS.get(operation);
        return warning;
    }
}
