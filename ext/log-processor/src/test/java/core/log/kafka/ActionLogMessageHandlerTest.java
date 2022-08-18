package core.log.kafka;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import core.framework.inject.Inject;
import core.framework.kafka.Message;
import core.framework.log.message.ActionLogMessage;
import core.framework.log.message.PerformanceStatMessage;
import core.framework.search.ElasticSearch;
import core.framework.search.ElasticSearchType;
import core.framework.search.GetRequest;
import core.framework.search.SearchRequest;
import core.log.IntegrationTest;
import core.log.domain.ActionDocument;
import core.log.domain.TraceDocument;
import core.log.service.IndexService;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static core.framework.search.query.Queries.match;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class ActionLogMessageHandlerTest extends IntegrationTest {
    @Inject
    ActionLogMessageHandler handler;
    @Inject
    IndexService indexService;
    @Inject
    ElasticSearchType<ActionDocument> actionType;
    @Inject
    ElasticSearchType<TraceDocument> traceType;
    @Inject
    ElasticSearch elasticSearch;

    @Test
    void index() {
        LocalDate now = LocalDate.of(2016, Month.JANUARY, 15);

        ActionLogMessage message1 = message("1", "OK");
        message1.context = Map.of("key", List.of("value"));
        message1.stats = Map.of("count", 1d);
        message1.correlationIds = List.of("id1", "id2");
        message1.clients = List.of("client");
        var stat = new PerformanceStatMessage();
        stat.count = 1;
        stat.totalElapsed = 10L;
        stat.readEntries = 1;
        stat.writeEntries = 2;
        message1.performanceStats = Map.of("redis", stat);

        ActionLogMessage message2 = message("2", "WARN");
        message2.traceLog = "trace";

        List<Message<ActionLogMessage>> messages = List.of(new Message<>("k1", message1), new Message<>("k2", message2));
        handler.index(messages, now);

        ActionDocument action = action(now, message1.id);
        assertThat(action.timestamp).isEqualTo(message1.date);
        assertThat(action.result).isEqualTo(message1.result);
        assertThat(action.correlationIds).isEqualTo(message1.correlationIds);
        assertThat(action.refIds).isEqualTo(message1.refIds);
        assertThat(action.clients).isEqualTo(message1.clients);
        assertThat(action.performanceStats.get("redis")).usingRecursiveComparison().isEqualTo(message1.performanceStats.get("redis"));
        assertThat(action.context).containsEntry("key", List.of("value"));

        TraceDocument trace = trace(now, message2.id);
        assertThat(trace.content).isEqualTo(message2.traceLog);

        String index = indexService.indexName("action", now);
        elasticSearch.refreshIndex(index);
        var request = new SearchRequest();
        request.query = new Query.Builder().match(match("context.key", "value")).build();
        request.index = index;
        List<ActionDocument> actions = actionType.search(request).hits;
        assertThat(actions).hasSize(1);
    }

    @Test
    void indexWithDifferentDateFormatValues() {
        ActionLogMessage message = message("1", "OK");
        // instant.toString() outputs without nano fraction if nano is 0, refer to java.time.format.DateTimeFormatter.ISO_INSTANT
        message.date = ZonedDateTime.now().withNano(0).toInstant();
        handler.handle(List.of(new Message<>("k1", message)));

        message.date = ZonedDateTime.now().withNano(123456).toInstant();
        handler.handle(List.of(new Message<>("k1", message)));
    }

    private ActionDocument action(LocalDate now, String id) {
        var request = new GetRequest();
        request.index = indexService.indexName("action", now);
        request.id = id;
        return actionType.get(request).orElseThrow(() -> new Error("not found"));
    }

    private TraceDocument trace(LocalDate now, String id) {
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
