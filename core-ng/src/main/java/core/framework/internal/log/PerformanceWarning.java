package core.framework.internal.log;

import core.framework.log.IOWarning;

import java.time.Duration;

/**
 * @author neo
 */
public final class PerformanceWarning {
    public static PerformanceWarning[] of(IOWarning[] warnings) {
        if (warnings.length <= 0) return null;

        var results = new PerformanceWarning[warnings.length];
        for (int i = 0; i < warnings.length; i++) {
            IOWarning warning = warnings[i];
            results[i] = new PerformanceWarning(warning.operation(),
                warning.maxOperations(),
                warning.maxElapsedInMs() > 0 ? Duration.ofMillis(warning.maxElapsedInMs()) : null,
                warning.maxReads(),
                warning.maxTotalReads(),
                warning.maxTotalWrites());
        }
        return results;
    }

    public final String operation;
    public final int maxOperations;
    public final long maxElapsed;
    public final int maxReads;
    public final int maxTotalReads;
    public final int maxTotalWrites;

    public PerformanceWarning(String operation, int maxOperations, Duration maxElapsed, int maxReads, int maxTotalReads, int maxTotalWrites) {
        this.operation = operation;
        this.maxOperations = maxOperations;
        this.maxElapsed = maxElapsed == null ? -1 : maxElapsed.toNanos();
        this.maxReads = maxReads;
        this.maxTotalReads = maxTotalReads;
        this.maxTotalWrites = maxTotalWrites;
    }
}
