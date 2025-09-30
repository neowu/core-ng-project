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
        stat = new PerformanceStat(new PerformanceWarning("db", 100, Duration.ofSeconds(5), 1000, 10_000, 10_000));
        logManager = new LogManager();
    }

    @Test
    void checkSingleIO() {
        logManager.run("test", null, actionLog -> {
            stat.checkSingleIO(Duration.ofSeconds(10).toNanos(), 1000);

            assertThat(actionLog.errorCode()).isEqualTo("SLOW_DB");
            assertThat(actionLog.errorMessage).startsWith("slow operation, operation=db, elapsed=PT10S");
            return null;
        });
    }

    @Test
    void checkTotalIO() {
        logManager.run("test", null, actionLog -> {
            stat.readEntries = 20_000;
            stat.writeEntries = 20_000;
            stat.checkTotalIO();

            assertThat(actionLog.errorCode()).isEqualTo("HIGH_DB_IO");
            assertThat(actionLog.errorMessage).startsWith("read too many entries, operation=db, entries=20000");
            return null;
        });
    }
}
