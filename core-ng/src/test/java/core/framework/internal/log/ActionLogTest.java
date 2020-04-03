package core.framework.internal.log;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        assertThatThrownBy(() -> log.context("key", "x".repeat(ActionLog.MAX_CONTEXT_VALUE_LENGTH + 1)))
                .isInstanceOf(Error.class)
                .hasMessageStartingWith("context value is too long");
    }

    @Test
    void multipleContextValues() {
        log.context("key", "value1");
        log.context("key", "value2");
        assertThat(log.context).containsEntry("key", List.of("value1", "value2"));
    }

    @Test
    void flushTraceLogWithTrace() {
        log.trace = true;

        assertThat(log.flushTraceLog()).isTrue();
    }

    @Test
    void flushTraceLogWithWarning() {
        log.process(new LogEvent("logger", null, LogLevel.WARN, null, null, null));

        assertThat(log.flushTraceLog()).isTrue();
    }

    @Test
    void result() {
        assertThat(log.result()).isEqualTo("OK");

        log.process(new LogEvent("logger", null, LogLevel.WARN, null, null, null));
        assertThat(log.result()).isEqualTo("WARN");
    }

    @Test
    void errorCode() {
        assertThat(log.errorCode()).isNull();

        log.process(new LogEvent("logger", null, LogLevel.WARN, null, null, null));
        assertThat(log.errorCode()).isEqualTo("UNASSIGNED");
    }

    @Test
    void truncateErrorMessage() {
        log.process(new LogEvent("logger", null, LogLevel.WARN, "x".repeat(ActionLog.MAX_CONTEXT_VALUE_LENGTH + 1), null, null));

        assertThat(log.errorMessage.length()).isEqualTo(ActionLog.MAX_CONTEXT_VALUE_LENGTH);
    }

    @Test
    void stat() {
        log.stat("stat", 1);
        assertThat(log.stats.get("stat").intValue()).isEqualTo(1);

        log.stat("stat", 1);
        assertThat(log.stats.get("stat").intValue()).isEqualTo(2);
    }

    @Test
    void track() {
        assertThat(log.track("db", 1000, 1, 0)).isEqualTo(1);
        PerformanceStat stat = log.performanceStats.get("db");
        assertThat(stat.count).isEqualTo(1);
        assertThat(stat.totalElapsed).isEqualTo(1000);
        assertThat(stat.readEntries).isEqualTo(1);
        assertThat(stat.writeEntries).isEqualTo(0);

        assertThat(log.track("db", 1000, 1, 1)).isEqualTo(2);
        stat = log.performanceStats.get("db");
        assertThat(stat.count).isEqualTo(2);
        assertThat(stat.totalElapsed).isEqualTo(2000);
        assertThat(stat.readEntries).isEqualTo(2);
        assertThat(stat.writeEntries).isEqualTo(1);

        assertThat(log.track("http", 3000, 0, 0)).isEqualTo(1);
        stat = log.performanceStats.get("http");
        assertThat(stat.count).isEqualTo(1);
        assertThat(stat.totalElapsed).isEqualTo(3000);
        assertThat(stat.readEntries).isZero();
        assertThat(stat.writeEntries).isZero();
    }
}
