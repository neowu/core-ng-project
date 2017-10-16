package core.framework.impl.log;

import org.junit.jupiter.api.Test;

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
}
