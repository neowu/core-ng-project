package core.log.queue;

import core.framework.api.search.ElasticSearchType;
import core.framework.impl.log.queue.ActionLogMessage;
import core.log.IntegrationTest;
import core.log.domain.ActionLogDocument;
import org.junit.Assert;
import org.junit.Test;

import javax.inject.Inject;
import java.time.Instant;

/**
 * @author neo
 */
public class ActionLogMessageHandlerTest extends IntegrationTest {
    @Inject
    ActionLogMessageHandler handler;

    @Inject
    ElasticSearchType<ActionLogDocument> actionType;

    @Test
    public void handle() throws Exception {
        ActionLogMessage message = new ActionLogMessage();
        message.id = "1";
        message.date = Instant.now();
        message.result = "OK";
        handler.handle(message);

        ActionLogDocument action = actionType.get(message.id).get();
        Assert.assertEquals(message.result, action.result);
    }
}