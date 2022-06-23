package core.framework.internal.log;

import core.framework.log.IOWarning;
import core.framework.log.Markers;
import core.framework.util.ASCII;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import java.util.Map;

/**
 * @author neo
 */
public final class WarningContext {
    private static final Logger LOGGER = LoggerFactory.getLogger(WarningContext.class);
    private static final WarningConfig DEFAULT_CONFIG = new WarningConfig(2000, 2000, 10_000, 10_000);
    public boolean suppressSlowSQLWarning;
    private Map<String, WarningConfig> configs;

    public void initialize(IOWarning[] warnings) {
        if (warnings.length > 0) {
            @SuppressWarnings({"unchecked", "rawtypes"})
            Map.Entry<String, WarningConfig>[] entries = new Map.Entry[warnings.length];
            for (int i = 0; i < warnings.length; i++) {
                IOWarning warning = warnings[i];
                int maxOperations = warning.maxOperations() > 0 ? warning.maxOperations() : DEFAULT_CONFIG.maxOperations;
                int maxRows = warning.maxFetch() > 0 ? warning.maxFetch() : DEFAULT_CONFIG.maxFetch;
                int maxReads = warning.maxReads() > 0 ? warning.maxReads() : DEFAULT_CONFIG.maxReads;
                int maxWrites = warning.maxWrites() > 0 ? warning.maxWrites() : DEFAULT_CONFIG.maxWrites;
                entries[i] = Map.entry(warning.operation(), new WarningConfig(maxOperations, maxRows, maxReads, maxWrites));
            }
            configs = Map.ofEntries(entries);
        }
    }

    public void initialize(WarningContext parentContext) {
        configs = parentContext.configs;
    }

    public void validate(String operation, int count, int currentReadEntries, int totalReadEntries, int totalWriteEntries) {
        WarningConfig config = configs == null ? DEFAULT_CONFIG : configs.getOrDefault(operation, DEFAULT_CONFIG);
        if (count > config.maxOperations) {
            LOGGER.warn(errorCode(operation), "too many operations, operation={}, count={}", operation, count);
        }
        if (currentReadEntries > config.maxFetch) {
            LOGGER.warn(errorCode(operation), "fetched too many entries, operation={}, entries={}", operation, currentReadEntries);
        }
        if (totalReadEntries > config.maxReads) {
            LOGGER.warn(errorCode(operation), "read too many entries, operation={}, entries={}", operation, totalReadEntries);
        }
        if (totalWriteEntries > config.maxWrites) {
            LOGGER.warn(errorCode(operation), "wrote too many entries, operation={}, entries={}", operation, totalWriteEntries);
        }
    }

    private Marker errorCode(String operation) {
        return Markers.errorCode("LARGE_" + ASCII.toUpperCase(operation) + "_IO");
    }

    record WarningConfig(int maxOperations, int maxFetch, int maxReads, int maxWrites) {
    }
}
