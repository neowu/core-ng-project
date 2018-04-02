package core.framework.impl.log;

import core.framework.log.Markers;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class LogEventTest {
    @Test
    void message() {
        LogEvent event = new LogEvent(null, null, null, "message-{}", new Object[]{1}, null);
        String message = event.message();
        assertEquals("message-1", message);
    }

    @Test
    void logMessage() {
        LogEvent event = new LogEvent("logger", Markers.errorCode("ERROR_CODE"), LogLevel.WARN, "message-{}", new Object[]{1}, new Error());
        String message = event.logMessage();
        assertThat(message).contains("WARN logger - [ERROR_CODE] message-1");
    }
}
