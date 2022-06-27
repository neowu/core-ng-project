package core.framework.internal.log;

import core.framework.log.IOWarning;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static core.framework.log.Markers.errorCode;
import static core.framework.util.ASCII.toUpperCase;

/**
 * @author neo
 */
public final class WarningContext {
    private static final Logger LOGGER = LoggerFactory.getLogger(WarningContext.class);
    private static final Map<String, IOWarningConfig> DEFAULT_CONFIGS = new HashMap<>();

    public static void put(String operation, int maxOperations, Duration maxElapsed, int maxReads, int maxTotalReads, int maxTotalWrites) {
        DEFAULT_CONFIGS.put(operation, new IOWarningConfig(maxOperations, maxElapsed.toNanos(), maxReads, maxTotalReads, maxTotalWrites));
    }

    public boolean suppressSlowSQLWarning;
    long maxProcessTimeInNano;
    private Map<String, IOWarningConfig> configs;

    public void initialize(IOWarning[] warnings) {
        configs = new HashMap<>();
        if (warnings.length > 0) {
            for (IOWarning warning : warnings) {
                int maxElapsedInMs = warning.maxElapsedInMs();
                long maxElapsed = maxElapsedInMs > 0 ? maxElapsedInMs * 1_000_000L : -1;
                var config = new IOWarningConfig(warning.maxOperations(), maxElapsed, warning.maxReads(), warning.maxTotalReads(), warning.maxTotalWrites());
                configs.put(warning.operation(), config);
            }
        }
    }

    public void initialize(WarningContext parentContext) {
        configs = parentContext.configs;
    }

    public void maxProcessTimeInNano(long maxProcessTimeInNano) {
        this.maxProcessTimeInNano = maxProcessTimeInNano;
        LOGGER.debug("maxProcessTime={}", maxProcessTimeInNano);
    }

    public void checkSingleIO(String operation, long elapsed, int readEntries) {
        IOWarningConfig config = config(operation);
        if (config == null) return;

        if (config.maxReads > 0 && readEntries > config.maxReads) {
            LOGGER.warn(errorCode("HIGH_" + toUpperCase(operation) + "_IO"), "read too many entries once, operation={}, entries={}", operation, readEntries);
        }
        if (config.maxElapsed > 0 && elapsed > config.maxElapsed) {
            LOGGER.warn(errorCode("SLOW_" + toUpperCase(operation)), "slow operation, operation={}, elapsed={}", operation, Duration.ofNanos(elapsed));
        }
    }

    public void checkTotalIO(String operation, int count, int totalReadEntries, int totalWriteEntries) {
        IOWarningConfig config = config(operation);
        if (config == null) return;

        if (config.maxOperations > 0 && count > config.maxOperations) {
            LOGGER.warn(errorCode("HIGH_" + toUpperCase(operation) + "_IO"), "too many operations, operation={}, count={}", operation, count);
        }
        if (config.maxTotalReads > 0 && totalReadEntries > config.maxTotalReads) {
            LOGGER.warn(errorCode("HIGH_" + toUpperCase(operation) + "_IO"), "read too many entries, operation={}, entries={}", operation, totalReadEntries);
        }
        if (config.maxTotalWrites > 0 && totalWriteEntries > config.maxTotalWrites) {
            LOGGER.warn(errorCode("HIGH_" + toUpperCase(operation) + "_IO"), "write too many entries, operation={}, entries={}", operation, totalWriteEntries);
        }
    }

    public void checkMaxProcessTime(long elapsed) {
        if (maxProcessTimeInNano > 0 && elapsed > maxProcessTimeInNano) {
            LOGGER.warn(errorCode("SLOW_PROCESS"), "action took longer than of max process time, maxProcessTime={}, elapsed={}", Duration.ofNanos(maxProcessTimeInNano), Duration.ofNanos(elapsed));
        }
    }

    @Nullable
    private IOWarningConfig config(String operation) {
        if (configs != null) {
            IOWarningConfig config = configs.get(operation);
            if (config != null) return config;
        }
        return DEFAULT_CONFIGS.get(operation);
    }

    record IOWarningConfig(int maxOperations, long maxElapsed, int maxReads, int maxTotalReads, int maxTotalWrites) {
    }
}
