package core.framework.internal.log;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class WarningContextTest {
    private WarningContext context;

    @BeforeEach
    void createWarningContext() {
        context = new WarningContext();
    }

    @Test
    void checkMaxProcessTime() {
        context.maxProcessTimeInNano = -1;
        context.checkMaxProcessTime(100);

        context.maxProcessTimeInNano = 100;
        context.checkMaxProcessTime(81);
    }

    @Test
    void checkSingleIO() {
        var logManager = new LogManager();
        ActionLog actionLog = logManager.begin("begin", null);
        context.checkSingleIO("db", Duration.ofSeconds(10).toNanos(), 1000);
        actionLog.end("end");

        assertThat(actionLog.errorCode()).isEqualTo("SLOW_DB");
        assertThat(actionLog.errorMessage).startsWith("slow operation, operation=db, elapsed=PT10S");
    }

    @Test
    void checkTotalIO() {
        var logManager = new LogManager();
        ActionLog actionLog = logManager.begin("begin", null);
        context.checkTotalIO("db", 1000, 20_000, 20_000);
        actionLog.end("end");

        assertThat(actionLog.errorCode()).isEqualTo("HIGH_DB_IO");
        assertThat(actionLog.errorMessage).startsWith("read too many entries, operation=db, entries=20000");
    }
}
