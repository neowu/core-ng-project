package core.log.queue;

import core.framework.api.search.ElasticSearchType;
import core.framework.impl.log.queue.TraceLogMessage;
import core.log.IntegrationTest;
import core.log.domain.TraceLogDocument;
import org.junit.Assert;
import org.junit.Test;

import javax.inject.Inject;
import java.time.Instant;

/**
 * @author neo
 */
public class TraceLogMessageHandlerTest extends IntegrationTest {
    @Inject
    TraceLogMessageHandler handler;

    @Inject
    ElasticSearchType<TraceLogDocument> traceType;

    @Test
    public void handle() throws Exception {
        TraceLogMessage message = new TraceLogMessage();
        message.id = "1";
        message.date = Instant.now();
        message.result = "WARN";
        message.content = "line1";
        handler.handle(message);

        TraceLogDocument trace = traceType.get(message.id).get();
        Assert.assertEquals(message.result, trace.result);
        Assert.assertEquals(1, trace.content.size());
        Assert.assertEquals("line1", trace.content.get(0));

        message.result = "ERROR";
        message.content = "line2";
        handler.handle(message);

        trace = traceType.get(message.id).get();
        Assert.assertEquals(message.result, trace.result);
        Assert.assertEquals(2, trace.content.size());
        Assert.assertEquals("line1", trace.content.get(0));
        Assert.assertEquals("line2", trace.content.get(1));
    }
}