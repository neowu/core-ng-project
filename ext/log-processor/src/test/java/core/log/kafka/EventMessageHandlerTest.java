package core.log.kafka;

import core.framework.inject.Inject;
import core.framework.kafka.Message;
import core.framework.log.message.EventMessage;
import core.framework.search.ElasticSearchType;
import core.framework.search.GetRequest;
import core.log.IntegrationTest;
import core.log.domain.EventDocument;
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
class EventMessageHandlerTest extends IntegrationTest {
    @Inject
    EventMessageHandler handler;
    @Inject
    IndexService indexService;
    @Inject
    ElasticSearchType<EventDocument> eventType;

    @Test
    void index() {
        var now = LocalDate.of(2019, Month.MARCH, 19);
        EventMessage message = message("1");
        message.info = Map.of("param", "value");
        List<Message<EventMessage>> messages = List.of(new Message<>("k1", message), new Message<>("k2", message("2")));

        handler.index(messages, now);
        EventDocument event = get(now, message.id);
        assertThat(event.context).isEqualTo(message.context);
        assertThat(event.info).containsEntry("param", "value");
    }

    private EventDocument get(LocalDate now, String id) {
        var request = new GetRequest();
        request.index = indexService.indexName("event", now);
        request.id = id;
        return eventType.get(request).orElseThrow(() -> new Error("not found"));
    }

    private EventMessage message(String id) {
        var message = new EventMessage();
        message.id = id;
        message.app = "test";
        message.date = Instant.now();
        message.result = "OK";
        message.action = "test";
        message.context = Map.of("clientIP", "127.0.0.1");
        message.receivedTime = Instant.now();
        return message;
    }
}
