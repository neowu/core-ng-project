package core.framework.impl.log;

import core.framework.log.Markers;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class LogEventTest {
    @Test
    void appendTrace() {
        var event = new LogEvent("logger", Markers.errorCode("ERROR_CODE"), LogLevel.WARN, "message-{}", new Object[]{1}, new Error());
        var builder = new StringBuilder();
        event.appendTrace(builder, System.nanoTime());
        assertThat(builder.toString()).contains("WARN logger - [ERROR_CODE] message-1");

        builder = new StringBuilder();
        event = new LogEvent("logger", null, LogLevel.DEBUG, "message", null, null);
        event.appendTrace(builder, System.nanoTime());
        assertThat(builder.toString()).contains("logger - message");
    }

    @Test
    void info() {
        var event = new LogEvent("logger", Markers.errorCode("ERROR_CODE"), LogLevel.WARN, "message-{}", new Object[]{1}, new Error());
        String message = event.info();
        assertThat(message).contains("WARN logger - [ERROR_CODE] message-1");
    }

    @Test
    void appendDuration() {
        var event = new LogEvent("logger", null, LogLevel.DEBUG, null, null, null);

        var builder = new StringBuilder();
        event.appendDuration(builder, Duration.ofSeconds(34).plusMillis(145).toNanos());
        assertThat(builder.toString()).isEqualTo("00:34.145000000");

        builder = new StringBuilder();
        event.appendDuration(builder, Duration.ofMinutes(5).plusNanos(34512300).toNanos());
        assertThat(builder.toString()).isEqualTo("05:00.034512300");

        builder = new StringBuilder();
        event.appendDuration(builder, Duration.ofMinutes(30).plusSeconds(1).plusNanos(123).toNanos());
        assertThat(builder.toString()).isEqualTo("30:01.000000123");
    }

    @Test
    void message() {
        var event = new LogEvent("logger", null, LogLevel.ERROR, "error", null, null);
        assertThat(event.message()).isEqualTo("error");

        event = new LogEvent("logger", null, LogLevel.ERROR, "error-{}", new Object[]{1}, null);
        assertThat(event.message()).isEqualTo("error-1");
    }
}
