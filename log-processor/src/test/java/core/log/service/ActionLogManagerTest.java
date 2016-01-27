package core.log.service;

import core.framework.api.search.ElasticSearchType;
import core.framework.api.search.GetRequest;
import core.framework.api.util.Lists;
import core.framework.impl.log.queue.ActionLogMessage;
import core.log.IntegrationTest;
import core.log.domain.ActionLogDocument;
import core.log.domain.TraceLogDocument;
import org.junit.Test;

import javax.inject.Inject;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;

import static org.junit.Assert.assertEquals;

/**
 * @author neo
 */
public class ActionLogManagerTest extends IntegrationTest {
    @Inject
    ActionLogManager actionLogManager;

    @Inject
    ElasticSearchType<ActionLogDocument> actionType;

    @Inject
    ElasticSearchType<TraceLogDocument> traceType;

    @Test
    public void index() throws Exception {
        ActionLogMessage message1 = new ActionLogMessage();
        message1.id = "1";
        message1.date = Instant.now();
        message1.result = "OK";
        ActionLogMessage message2 = new ActionLogMessage();
        message2.id = "2";
        message2.date = Instant.now();
        message2.result = "WARN";
        message2.traceLog = "trace";

        LocalDate now = LocalDate.of(2016, Month.JANUARY, 15);
        actionLogManager.index(Lists.newArrayList(message1, message2), now);

        GetRequest request = new GetRequest();
        request.index = actionLogManager.indexName("action", now);
        request.id = message1.id;
        ActionLogDocument action = actionType.get(request).get();
        assertEquals(message1.result, action.result);

        request = new GetRequest();
        request.index = actionLogManager.indexName("trace", now);
        request.id = message2.id;
        TraceLogDocument trace = traceType.get(request).get();
        assertEquals(message2.id, trace.id);
        assertEquals(message2.traceLog, trace.content);
    }

    @Test
    public void indexName() {
        assertEquals("action-2016-01-15", actionLogManager.indexName("action", LocalDate.of(2016, Month.JANUARY, 15)));
    }
}