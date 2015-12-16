package core.log.queue;

import core.framework.api.search.ElasticSearchType;
import core.framework.api.util.Lists;
import core.framework.impl.log.queue.ActionLogMessage;
import core.framework.impl.log.queue.ActionLogMessages;
import core.log.IntegrationTest;
import core.log.domain.ActionLogDocument;
import core.log.domain.TraceLogDocument;
import org.junit.Assert;
import org.junit.Test;

import javax.inject.Inject;
import java.time.Instant;

/**
 * @author neo
 */
public class ActionLogMessagesHandlerTest extends IntegrationTest {
    @Inject
    ActionLogMessagesHandler handler;

    @Inject
    ElasticSearchType<ActionLogDocument> actionType;

    @Inject
    ElasticSearchType<TraceLogDocument> traceType;

    @Test
    public void handle() throws Exception {
        ActionLogMessage message1 = new ActionLogMessage();
        message1.id = "1";
        message1.date = Instant.now();
        message1.result = "OK";
        ActionLogMessage message2 = new ActionLogMessage();
        message2.id = "2";
        message2.date = Instant.now();
        message2.result = "WARN";
        message2.traceLog = "trace";

        ActionLogMessages messages = new ActionLogMessages();
        messages.logs = Lists.newArrayList(message1, message2);
        handler.handle(messages);

        ActionLogDocument action = actionType.get(message1.id).get();
        Assert.assertEquals(message1.result, action.result);

        TraceLogDocument trace = traceType.get(message2.id).get();
        Assert.assertEquals(message2.id, trace.id);
        Assert.assertEquals(message2.traceLog, trace.content);
    }
}