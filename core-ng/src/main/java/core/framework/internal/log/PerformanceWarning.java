package core.framework.internal.log;

public record PerformanceWarning(String operation, int maxOperations, long maxElapsed,
                                 int maxReads, int maxTotalReads, int maxTotalWrites,
                                 long maxReadBytes, long maxTotalReadBytes) {
}
