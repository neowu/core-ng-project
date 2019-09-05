package core.log.service;

import core.framework.inject.Inject;
import core.framework.log.message.EventMessage;
import core.framework.search.ElasticSearchType;
import core.framework.search.GetRequest;
import core.framework.util.Lists;
import core.log.IntegrationTest;
import core.log.domain.EventDocument;
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
class EventServiceTest extends IntegrationTest {
    @Inject
    EventService eventService;
    @Inject
    IndexService indexService;
    @Inject
    ElasticSearchType<EventDocument> eventType;

    @Test
    void index() {
        EventMessage message = message("1");
        message.info = Map.of("param", "value");

        var now = LocalDate.of(2019, Month.MARCH, 19);
        eventService.index(List.of(message), now);

        EventDocument event = eventDocument(now, message.id);
        assertThat(event.context).isEqualTo(message.context);
        assertThat(event.info).containsEntry("param", "value");
    }

    @Test
    void bulkIndex() {
        List<EventMessage> messages = Lists.newArrayList();
        for (int i = 0; i < 6; i++) {
            messages.add(message("bulk-" + i));
        }

        var now = LocalDate.of(2019, Month.MARCH, 19);
        eventService.index(messages, now);

        EventDocument event = eventDocument(now, messages.get(0).id);
        assertThat(event.context).isEqualTo(messages.get(0).context);
    }

    private EventDocument eventDocument(LocalDate now, String id) {
        var request = new GetRequest();
        request.index = indexService.indexName("event", now);
        request.id = id;
        return eventType.get(request).orElseThrow(() -> new Error("not found"));
    }

    private EventMessage message(String id) {
        var message = new EventMessage();
        message.id = id;
        message.app = "test";
        message.timestamp = Instant.now();
        message.result = "OK";
        message.action = "test";
        message.context = Map.of("clientIP", "127.0.0.1");
        message.eventTime = Instant.now();
        return message;
    }
}
