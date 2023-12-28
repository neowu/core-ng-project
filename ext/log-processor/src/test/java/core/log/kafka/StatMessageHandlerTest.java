package core.log.kafka;

import core.framework.inject.Inject;
import core.framework.kafka.Message;
import core.framework.log.message.StatMessage;
import core.framework.search.ElasticSearchType;
import core.framework.search.GetRequest;
import core.log.IntegrationTest;
import core.log.domain.StatDocument;
import core.log.service.IndexService;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author neo
 */
class StatMessageHandlerTest extends IntegrationTest {
    @Inject
    StatMessageHandler handler;
    @Inject
    IndexService indexService;
    @Inject
    ElasticSearchType<StatDocument> statType;

    @Test
    void index() {
        LocalDate now = LocalDate.of(2017, Month.OCTOBER, 10);

        StatMessage message = message("1");
        message.info = Map.of("key", "value");
        List<Message<StatMessage>> messages = List.of(new Message<>("k1", message), new Message<>("k2", message("2")));

        handler.index(messages, now);

        StatDocument stat = get(now, message.id);
        assertThat(stat.stats).isEqualTo(message.stats);
        assertThat(stat.info).isEqualTo(message.info);
    }

    private StatDocument get(LocalDate now, String id) {
        var request = new GetRequest();
        request.index = indexService.indexName("stat", now);
        request.id = id;
        return statType.get(request).orElseThrow(() -> new Error("not found"));
    }

    private StatMessage message(String id) {
        var message = new StatMessage();
        message.id = id;
        message.date = Instant.now();
        message.stats = Map.of("thread_count", 10d);
        return message;
    }
}
