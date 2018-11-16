package core.log.service;

import core.framework.inject.Inject;
import core.framework.internal.log.message.ActionLogMessage;
import core.framework.internal.log.message.PerformanceStat;
import core.framework.search.ElasticSearchType;
import core.framework.search.GetRequest;
import core.framework.util.Lists;
import core.log.IntegrationTest;
import core.log.domain.ActionDocument;
import core.log.domain.TraceDocument;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class ActionServiceTest extends IntegrationTest {
    @Inject
    ActionService actionService;
    @Inject
    IndexService indexService;
    @Inject
    ElasticSearchType<ActionDocument> actionType;
    @Inject
    ElasticSearchType<TraceDocument> traceType;

    @Test
    void index() {
        ActionLogMessage message1 = message("1", "OK");
        message1.context = Map.of("key", "value");
        message1.stats = Map.of("count", 1d);
        message1.correlationIds = List.of("id1", "id2");
        message1.clients = List.of("client");
        var stat = new PerformanceStat();
        stat.count = 1;
        stat.totalElapsed = 10L;
        stat.readEntries = 1;
        stat.writeEntries = 2;
        message1.performanceStats = Map.of("redis", stat);

        ActionLogMessage message2 = message("2", "WARN");
        message2.traceLog = "trace";

        LocalDate now = LocalDate.of(2016, Month.JANUARY, 15);
        actionService.index(List.of(message1, message2), now);

        ActionDocument action = actionDocument(now, message1.id);
        assertThat(action.timestamp).isEqualTo(message1.date);
        assertThat(action.result).isEqualTo(message1.result);
        assertThat(action.correlationIds).isEqualTo(message1.correlationIds);
        assertThat(action.refIds).isEqualTo(message1.refIds);
        assertThat(action.clients).isEqualTo(message1.clients);
        assertThat(action.performanceStats.get("redis")).isEqualToComparingFieldByField(message1.performanceStats.get("redis"));

        TraceDocument trace = traceDocument(now, message2.id);
        assertThat(trace.content).isEqualTo(message2.traceLog);
    }

    @Test
    void indexWithDifferentDateFormatValues() {
        ActionLogMessage message = message("1", "OK");
        // instant.toString() outputs without nano fraction if nano is 0, refer to java.time.format.DateTimeFormatter.ISO_INSTANT
        message.date = ZonedDateTime.now().withNano(0).toInstant();
        actionService.index(List.of(message));

        message.date = ZonedDateTime.now().withNano(123456).toInstant();
        actionService.index(List.of(message));
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
        assertThat(action.result).isEqualTo("TRACE");
    }

    private ActionDocument actionDocument(LocalDate now, String id) {
        var request = new GetRequest();
        request.index = indexService.indexName("action", now);
        request.id = id;
        return actionType.get(request).orElseThrow(() -> new Error("not found"));
    }

    private TraceDocument traceDocument(LocalDate now, String id) {
        var request = new GetRequest();
        request.index = indexService.indexName("trace", now);
        request.id = id;
        return traceType.get(request).orElseThrow(() -> new Error("not found"));
    }

    private ActionLogMessage message(String id, String result) {
        var message = new ActionLogMessage();
        message.id = id;
        message.date = Instant.now();
        message.result = result;
        return message;
    }
}
