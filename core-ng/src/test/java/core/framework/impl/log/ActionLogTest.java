package core.framework.impl.log;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author neo
 */
class ActionLogTest {
    private ActionLog log;

    @BeforeEach
    void createActionLog() {
        log = new ActionLog("begin");
    }

    @Test
    void contextValueIsTooLong() {
        Error error = assertThrows(Error.class, () -> log.context("key", longString(1001)));
        assertTrue(error.getMessage().startsWith("context value is too long"));
    }

    @Test
    void duplicateContextKey() {
        Error error = assertThrows(Error.class, () -> {
            log.context("key", "value1");
            log.context("key", "value2");
        });
        assertThat(error.getMessage()).contains("found duplicate context key");
    }

    @Test
    void flushTraceLogWithTrace() {
        log.trace = true;

        assertTrue(log.flushTraceLog());
    }

    @Test
    void flushTraceLogWithWarning() {
        log.process(new LogEvent("logger", null, LogLevel.WARN, null, null, null));

        assertTrue(log.flushTraceLog());
    }

    @Test
    void result() {
        assertEquals("OK", log.result());

        log.process(new LogEvent("logger", null, LogLevel.WARN, null, null, null));
        assertEquals("WARN", log.result());
    }

    @Test
    void errorCode() {
        assertNull(log.errorCode());

        log.process(new LogEvent("logger", null, LogLevel.WARN, null, null, null));
        assertEquals("UNASSIGNED", log.errorCode());
    }

    @Test
    void truncateErrorMessage() {
        log.process(new LogEvent("logger", null, LogLevel.WARN, longString(300), null, null));

        assertEquals(200, log.errorMessage.length());
    }

    @Test
    void stat() {
        log.stat("stat", 1);
        assertEquals(1, log.stats.get("stat").intValue());

        log.stat("stat", 1);
        assertEquals(2, log.stats.get("stat").intValue());
    }

    @Test
    void track() {
        log.track("db", 1000, 1, 0);
        PerformanceStat stat = log.performanceStats.get("db");
        assertEquals(1, stat.count);
        assertEquals(1000, stat.totalElapsed);
        assertEquals(1, stat.readEntries.intValue());
        assertEquals(0, stat.writeEntries.intValue());

        log.track("db", 1000, 1, 1);
        stat = log.performanceStats.get("db");
        assertEquals(2, stat.count);
        assertEquals(2000, stat.totalElapsed);
        assertEquals(2, stat.readEntries.intValue());
        assertEquals(1, stat.writeEntries.intValue());
    }

    private String longString(int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append('x');
        }
        return builder.toString();
    }
}
