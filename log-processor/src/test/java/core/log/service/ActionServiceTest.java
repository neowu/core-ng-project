package core.log.service;

import core.framework.impl.log.message.ActionLogMessage;
import core.framework.impl.log.message.PerformanceStatMessage;
import core.framework.inject.Inject;
import core.framework.search.ElasticSearchType;
import core.framework.search.GetRequest;
import core.framework.util.Lists;
import core.framework.util.Maps;
import core.log.IntegrationTest;
import core.log.domain.ActionDocument;
import core.log.domain.TraceDocument;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author neo
 */
class ActionServiceTest extends IntegrationTest {
    @Inject
    ActionService actionService;

    @Inject
    ElasticSearchType<ActionDocument> actionType;

    @Inject
    ElasticSearchType<TraceDocument> traceType;

    @Test
    void index() {
        ActionLogMessage message1 = message("1", "OK");
        message1.context = Maps.newHashMap("key", "value");
        message1.stats = Maps.newHashMap("count", 1d);
        PerformanceStatMessage stat = new PerformanceStatMessage();
        stat.count = 1;
        stat.totalElapsed = 10L;
        stat.readEntries = 1;
        stat.writeEntries = 2;
        message1.performanceStats = Maps.newHashMap("redis", stat);

        ActionLogMessage message2 = message("2", "WARN");
        message2.traceLog = "trace";

        LocalDate now = LocalDate.of(2016, Month.JANUARY, 15);
        actionService.index(Lists.newArrayList(message1, message2), now);

        ActionDocument action = actionDocument(now, message1.id);
        assertEquals(message1.result, action.result);
        assertEquals(message1.performanceStats.get("redis").count, action.performanceStats.get("redis").count);
        assertEquals(message1.performanceStats.get("redis").readEntries, action.performanceStats.get("redis").readEntries);

        TraceDocument trace = traceDocument(now, message2.id);
        assertEquals(message2.id, trace.id);
        assertEquals(message2.traceLog, trace.content);
    }

    @Test
    void bulkIndex() {
        List<ActionLogMessage> messages = Lists.newArrayList();
        for (int i = 0; i < 6; i++) {
            ActionLogMessage message = message("bulk-" + i, "TRACE");
            message.traceLog = "trace";
            messages.add(message);
        }

        LocalDate now = LocalDate.of(2016, Month.JANUARY, 15);
        actionService.index(messages, now);

        ActionDocument action = actionDocument(now, messages.get(0).id);
        assertEquals("TRACE", action.result);
    }

    private ActionDocument actionDocument(LocalDate now, String id) {
        GetRequest request = new GetRequest();
        request.index = IndexName.name("action", now);
        request.id = id;
        return actionType.get(request).orElseThrow(() -> new Error("not found"));
    }

    private TraceDocument traceDocument(LocalDate now, String id) {
        GetRequest request = new GetRequest();
        request.index = IndexName.name("trace", now);
        request.id = id;
        return traceType.get(request).orElseThrow(() -> new Error("not found"));
    }

    private ActionLogMessage message(String id, String result) {
        ActionLogMessage message = new ActionLogMessage();
        message.id = id;
        message.date = Instant.now();
        message.result = result;
        return message;
    }
}
