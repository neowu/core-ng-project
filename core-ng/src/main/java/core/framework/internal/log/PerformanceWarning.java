package core.framework.internal.log;

import java.time.Duration;

/**
 * @author neo
 */
public final class PerformanceWarning {
    public final int maxOperations;
    public final long maxElapsed;
    public final int maxReads;
    public final int maxTotalReads;
    public final int maxTotalWrites;

    public PerformanceWarning(int maxOperations, Duration maxElapsed, int maxReads, int maxTotalReads, int maxTotalWrites) {
        this.maxOperations = maxOperations;
        this.maxElapsed = maxElapsed == null ? -1 : maxElapsed.toNanos();
        this.maxReads = maxReads;
        this.maxTotalReads = maxTotalReads;
        this.maxTotalWrites = maxTotalWrites;
    }
}
