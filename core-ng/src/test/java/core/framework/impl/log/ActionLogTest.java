package core.framework.impl.log;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author neo
 */
class ActionLogTest {
    @Test
    void contextValueIsTooLong() {
        ActionLog log = new ActionLog("begin");
        Error error = assertThrows(Error.class, () -> log.context("key", longString(1001)));
        assertTrue(error.getMessage().startsWith("context value is too long"));
    }

    @Test
    void duplicateContextKey() {
        ActionLog log = new ActionLog("begin");
        Error error = assertThrows(Error.class, () -> {
            log.context("key", "value1");
            log.context("key", "value2");
        });
        assertThat(error.getMessage(), containsString("found duplicate context key"));
    }

    @Test
    void flushTraceLogWithTrace() {
        ActionLog log = new ActionLog("begin");
        log.trace = true;

        assertTrue(log.flushTraceLog());
    }

    @Test
    void flushTraceLogWithWarning() {
        ActionLog log = new ActionLog("begin");
        log.process(new LogEvent("logger", null, LogLevel.WARN, null, null, null));

        assertTrue(log.flushTraceLog());
    }

    @Test
    void result() {
        ActionLog log = new ActionLog("begin");

        assertEquals("OK", log.result());

        log.process(new LogEvent("logger", null, LogLevel.WARN, null, null, null));
        assertEquals("WARN", log.result());
    }

    private String longString(int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append('x');
        }
        return builder.toString();
    }
}
