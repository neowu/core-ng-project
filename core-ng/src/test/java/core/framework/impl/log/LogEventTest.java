package core.framework.impl.log;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author neo
 */
public class LogEventTest {
    @Test
    public void truncateMessage() {
        LogEvent event = new LogEvent(null, null, null, "long-message-{}{}{}{}{}", new Object[]{1, 2, 3, 4, 5}, null);
        String message = event.message(15);
        Assert.assertEquals("long-message-12...(truncated)", message);
    }
}