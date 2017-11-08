package core.log.service;

import core.framework.inject.Inject;
import core.framework.log.message.ActionLogMessage;
import core.framework.log.message.PerformanceStatMessage;
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
        ActionLogMessage message1 = new ActionLogMessage();
        message1.id = "1";
        message1.date = Instant.now();
        message1.result = "OK";
        message1.context = Maps.newHashMap("key", "value");
        message1.stats = Maps.newHashMap("count", 1d);
        PerformanceStatMessage stat = new PerformanceStatMessage();
        stat.count = 1;
        stat.totalElapsed = 10L;
        message1.performanceStats = Maps.newHashMap("redis", stat);

        ActionLogMessage message2 = new ActionLogMessage();
        message2.id = "2";
        message2.date = Instant.now();
        message2.result = "WARN";
        message2.traceLog = "trace";

        LocalDate now = LocalDate.of(2016, Month.JANUARY, 15);
        actionService.index(Lists.newArrayList(message1, message2), now);

        GetRequest request = new GetRequest();
        request.index = IndexName.name("action", now);
        request.id = message1.id;
        ActionDocument action = actionType.get(request).orElseThrow(() -> new Error("not found"));
        assertEquals(message1.result, action.result);

        request = new GetRequest();
        request.index = IndexName.name("trace", now);
        request.id = message2.id;
        TraceDocument trace = traceType.get(request).orElseThrow(() -> new Error("not found"));
        assertEquals(message2.id, trace.id);
        assertEquals(message2.traceLog, trace.content);
    }
}
