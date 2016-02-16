package core.framework.impl.log;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author neo
 */
public class LogEventTest {
    @Test
    public void message() {
        LogEvent event = new LogEvent(null, null, null, "message-{}", new Object[]{1}, null);
        String message = event.message();
        Assert.assertEquals("message-1", message);
    }
}