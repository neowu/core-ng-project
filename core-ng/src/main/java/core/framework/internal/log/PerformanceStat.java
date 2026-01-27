package core.framework.internal.log;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static core.framework.log.Markers.errorCode;
import static core.framework.util.ASCII.toUpperCase;

/**
 * @author neo
 */
class PerformanceStat {
    private static final Logger LOGGER = LoggerFactory.getLogger(PerformanceStat.class);
    @Nullable
    final PerformanceWarning warning;

    int count;
    long totalElapsed;
    int readEntries;    // rows/docs/keys
    int writeEntries;
    long readBytes;
    long writeBytes;

    PerformanceStat(@Nullable PerformanceWarning warning) {
        this.warning = warning;
    }

    TrackResult track(long elapsed, int readEntries, int writeEntries, long readBytes, long writeBytes) {
        count += 1;
        totalElapsed += elapsed;

        this.readEntries += readEntries;
        this.writeEntries += writeEntries;

        this.readBytes += readBytes;
        this.writeBytes += writeBytes;

        boolean slow = checkSingleIO(elapsed, readEntries, readBytes);
        return new TrackResult(count, slow);
    }

    // return if slow
    boolean checkSingleIO(long elapsed, int readEntries, long readBytes) {
        if (warning == null) return false;

        boolean slow = false;
        if (warning.maxElapsed() > 0 && elapsed > warning.maxElapsed()) {
            LOGGER.warn(errorCode("SLOW_" + toUpperCase(warning.operation())), "slow operation, operation={}, elapsed={}", warning.operation(), Duration.ofNanos(elapsed));
            slow = true;
        }
        if (warning.maxReads() > 0 && readEntries > warning.maxReads()) {
            LOGGER.warn(errorCode("HIGH_" + toUpperCase(warning.operation()) + "_IO"), "read too many entries once, operation={}, entries={}", warning.operation(), readEntries);
        }
        if (warning.maxReadBytes() > 0 && readBytes > warning.maxReadBytes()) {
            LOGGER.warn(errorCode("HIGH_" + toUpperCase(warning.operation()) + "_IO"), "read too many bytes once, operation={}, bytes={}", warning.operation(), readBytes);
        }
        return slow;
    }

    void checkTotalIO() {
        if (warning == null) return;

        if (warning.maxOperations() > 0 && count > warning.maxOperations()) {
            LOGGER.warn(errorCode("HIGH_" + toUpperCase(warning.operation()) + "_IO"), "too many operations, operation={}, count={}", warning.operation(), count);
        }
        if (warning.maxTotalReads() > 0 && readEntries > warning.maxTotalReads()) {
            LOGGER.warn(errorCode("HIGH_" + toUpperCase(warning.operation()) + "_IO"), "read too many entries, operation={}, entries={}", warning.operation(), readEntries);
        }
        if (warning.maxTotalWrites() > 0 && writeEntries > warning.maxTotalWrites()) {
            LOGGER.warn(errorCode("HIGH_" + toUpperCase(warning.operation()) + "_IO"), "write too many entries, operation={}, entries={}", warning.operation(), writeEntries);
        }
        if (warning.maxTotalReadBytes() > 0 && readBytes > warning.maxTotalReadBytes()) {
            LOGGER.warn(errorCode("HIGH_" + toUpperCase(warning.operation()) + "_IO"), "read too many bytes, operation={}, bytes={}", warning.operation(), readBytes);
        }
    }
}
