package core.log.queue;

import core.framework.impl.log.queue.ActionLogMessage;
import core.log.IntegrationTest;
import org.junit.Test;

import javax.inject.Inject;
import java.time.Instant;

/**
 * @author neo
 */
public class ActionLogMessageHandlerTest extends IntegrationTest {
    @Inject
    ActionLogMessageHandler handler;

    @Test
    public void handle() throws Exception {
        ActionLogMessage message = new ActionLogMessage();
        message.id = "1";
        message.date = Instant.now();
        message.result = "OK";
        handler.handle(message);
    }
}