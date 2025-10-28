package core.framework.internal.log;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class PerformanceStatTest {
    private PerformanceStat stat;
    private LogManager logManager;

    @BeforeEach
    void createPerformanceStat() {
        stat = new PerformanceStat(new PerformanceWarning("db", 100, Duration.ofSeconds(5).toNanos(), 1000, 10_000, 10_000, 10_000, 10_000));
        logManager = new LogManager();
    }

    @Test
    void checkSingleIO() {
        logManager.run("test", null, actionLog -> {
            stat.checkSingleIO(Duration.ofSeconds(10).toNanos(), 1_000, 1_000);

            assertThat(actionLog.errorCode()).isEqualTo("SLOW_DB");
            assertThat(actionLog.errorMessage).startsWith("slow operation, operation=db, elapsed=PT10S");
            return null;
        });

        logManager.run("test", null, actionLog -> {
            stat.checkSingleIO(Duration.ofMillis(10).toNanos(), 100, 11_000);

            assertThat(actionLog.errorCode()).isEqualTo("HIGH_DB_IO");
            assertThat(actionLog.errorMessage).startsWith("read too many bytes once, operation=db, bytes=11000");
            return null;
        });
    }

    @Test
    void checkTotalIOWithReadTooManyEntries() {
        logManager.run("test", null, actionLog -> {
            stat.readEntries = 20_000;
            stat.writeEntries = 20_000;
            stat.checkTotalIO();

            assertThat(actionLog.errorCode()).isEqualTo("HIGH_DB_IO");
            assertThat(actionLog.errorMessage).startsWith("read too many entries, operation=db, entries=20000");
            return null;
        });
    }

    @Test
    void checkTotalIOWithReadTooManyBytes() {
        logManager.run("test", null, actionLog -> {
            stat.readBytes = 20_000;
            stat.checkTotalIO();

            assertThat(actionLog.errorCode()).isEqualTo("HIGH_DB_IO");
            assertThat(actionLog.errorMessage).startsWith("read too many bytes, operation=db, bytes=20000");
            return null;
        });
    }
}
