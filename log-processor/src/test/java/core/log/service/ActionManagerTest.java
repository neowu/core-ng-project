package core.log.service;

import core.framework.api.search.ElasticSearchType;
import core.framework.api.search.GetRequest;
import core.framework.api.util.Lists;
import core.framework.api.util.Maps;
import core.framework.impl.log.queue.ActionLogMessage;
import core.log.IntegrationTest;
import core.log.domain.ActionDocument;
import core.log.domain.TraceDocument;
import org.junit.Test;

import javax.inject.Inject;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;

import static org.junit.Assert.assertEquals;

/**
 * @author neo
 */
public class ActionManagerTest extends IntegrationTest {
    @Inject
    ActionManager actionManager;

    @Inject
    ElasticSearchType<ActionDocument> actionType;

    @Inject
    ElasticSearchType<TraceDocument> traceType;

    @Test
    public void index() throws Exception {
        ActionLogMessage message1 = new ActionLogMessage();
        message1.id = "1";
        message1.date = Instant.now();
        message1.result = "OK";
        message1.context = Maps.newHashMap("key", "value");
        ActionLogMessage message2 = new ActionLogMessage();
        message2.id = "2";
        message2.date = Instant.now();
        message2.result = "WARN";
        message2.traceLog = "trace";

        LocalDate now = LocalDate.of(2016, Month.JANUARY, 15);
        actionManager.index(Lists.newArrayList(message1, message2), now);

        GetRequest request = new GetRequest();
        request.index = IndexName.name("action", now);
        request.id = message1.id;
        ActionDocument action = actionType.get(request).get();
        assertEquals(message1.result, action.result);

        request = new GetRequest();
        request.index = IndexName.name("trace", now);
        request.id = message2.id;
        TraceDocument trace = traceType.get(request).get();
        assertEquals(message2.id, trace.id);
        assertEquals(message2.traceLog, trace.content);
    }
}