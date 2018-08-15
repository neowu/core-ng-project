package core.framework.impl.log;

import core.framework.impl.log.filter.LogFilter;
import core.framework.log.Markers;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class LogEventTest {
    @Test
    void logMessage() {
        var event = new LogEvent("logger", Markers.errorCode("ERROR_CODE"), LogLevel.WARN, "message-{}", new Object[]{1}, new Error());
        String message = event.logMessage(new LogFilter());
        assertThat(message).contains("WARN logger - [ERROR_CODE] message-1");
    }
}
