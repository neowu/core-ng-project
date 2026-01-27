package core.framework.log;

import core.framework.internal.log.LogManager;
import core.framework.internal.log.Trace;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class ActionLogContextTest {
    @Test
    void withoutCurrentActionLog() {
        assertThat(ActionLogContext.id()).isNull();

        ActionLogContext.put("key", "value");
        assertThat(ActionLogContext.get("key")).isEmpty();

        ActionLogContext.stat("stat", 1);

        ActionLogContext.track("db", 100);
    }

    @Test
    void withCurrentActionLog() {
        var logManager = new LogManager();

        logManager.run("test", null, actionLog -> {
            assertThat(ActionLogContext.id()).isNotNull();

            assertThat(ActionLogContext.get("key")).isEmpty();
            ActionLogContext.put("key", "value");
            assertThat(ActionLogContext.get("key")).contains("value");

            putNullContextValue();
            assertThat(ActionLogContext.get("nullValue")).contains("null");

            ActionLogContext.track("db", 100);
            return null;
        });
    }

    @SuppressFBWarnings("NP_LOAD_OF_KNOWN_NULL_VALUE")
    private void putNullContextValue() {
        String value = null;
        ActionLogContext.put("nullValue", value);
    }

    @Test
    void trace() {
        ActionLogContext.triggerTrace(true);

        var logManager = new LogManager();
        logManager.run("test", null, actionLog -> {
            ActionLogContext.triggerTrace(false);
            assertThat(actionLog.trace).isEqualTo(Trace.CURRENT);
            return null;
        });
    }

    @Test
    void remainingProcessTime() {
        assertThat(ActionLogContext.remainingProcessTime()).isNull();

        var logManager = new LogManager();
        logManager.run("test", null, actionLog -> {
            ActionLogContext.maxProcessTime(Duration.ofMinutes(30));
            assertThat(ActionLogContext.remainingProcessTime()).isGreaterThan(Duration.ofMinutes(20));
            return null;
        });
    }
}
