package core.framework.log;

import core.framework.internal.log.LogManager;
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
    void remainingProcessTime() {
        assertThat(ActionLogContext.remainingProcessTime()).isNull();

        var logManager = new LogManager();
        logManager.begin("begin", null);
        assertThat(ActionLogContext.remainingProcessTime()).isGreaterThanOrEqualTo(Duration.ZERO);
        logManager.end("end");
    }

    @Test
    void trace() {
        assertThat(ActionLogContext.trace()).isNull();

        var logManager = new LogManager();
        logManager.begin("begin", null);
        assertThat(ActionLogContext.trace()).isNotEmpty();
        logManager.end("end");
    }
}
