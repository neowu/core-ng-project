package core.framework.log;

import core.framework.internal.log.ActionLog;
import core.framework.internal.log.LogManager;
import core.framework.internal.log.Trace;
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

        assertThat(ActionLogContext.track("db", 100)).isEqualTo(1);
    }

    @Test
    void withCurrentActionLog() {
        var logManager = new LogManager();
        logManager.begin("begin", null);

        assertThat(ActionLogContext.id()).isNotNull();

        ActionLogContext.get("key");
        assertThat(ActionLogContext.get("key")).isEmpty();
        ActionLogContext.put("key", "value");
        assertThat(ActionLogContext.get("key")).contains("value");

        assertThat(ActionLogContext.track("db", 100)).isEqualTo(1);
        assertThat(ActionLogContext.track("db", 100)).isEqualTo(2);

        logManager.end("end");
    }

    @Test
    void trace() {
        ActionLogContext.triggerTrace(true);

        var logManager = new LogManager();
        logManager.begin("begin", null);
        ActionLogContext.triggerTrace(false);
        assertThat(LogManager.CURRENT_ACTION_LOG.get().trace).isEqualTo(Trace.CURRENT);
        logManager.end("end");
    }

    @Test
    void remainingProcessTime() {
        assertThat(ActionLogContext.remainingProcessTime()).isNull();

        var logManager = new LogManager();
        ActionLog log = logManager.begin("begin", null);
        log.warningContext.maxProcessTimeInNano(Duration.ofSeconds(30).toNanos());
        assertThat(ActionLogContext.remainingProcessTime()).isGreaterThan(Duration.ZERO);
        logManager.end("end");
    }
}
