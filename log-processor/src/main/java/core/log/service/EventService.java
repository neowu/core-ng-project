package core.log.service;

import core.framework.inject.Inject;
import core.framework.log.message.EventMessage;
import core.framework.search.BulkIndexRequest;
import core.framework.search.ElasticSearchType;
import core.framework.search.IndexRequest;
import core.framework.util.Maps;
import core.log.domain.EventDocument;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public class EventService {
    @Inject
    IndexService indexService;
    @Inject
    ElasticSearchType<EventDocument> eventType;

    public void index(List<EventMessage> messages) {
        index(messages, LocalDate.now());
    }

    void index(List<EventMessage> messages, LocalDate now) {
        if (messages.size() <= 5) { // use single index in quiet time
            for (EventMessage message : messages) {
                index(message.id, event(message), now);
            }
        } else {
            Map<String, EventDocument> events = Maps.newHashMapWithExpectedSize(messages.size());
            for (EventMessage message : messages) {
                events.put(message.id, event(message));
            }
            index(events, now);
        }
    }

    private void index(String id, EventDocument event, LocalDate now) {
        IndexRequest<EventDocument> request = new IndexRequest<>();
        request.index = indexService.indexName("event", now);
        request.id = id;
        request.source = event;
        eventType.index(request);
    }

    private void index(Map<String, EventDocument> events, LocalDate now) {
        BulkIndexRequest<EventDocument> request = new BulkIndexRequest<>();
        request.index = indexService.indexName("event", now);
        request.sources = events;
        eventType.bulkIndex(request);
    }

    private EventDocument event(EventMessage message) {
        var document = new EventDocument();
        document.timestamp = message.date;
        document.app = message.app;
        document.receivedTime = message.receivedTime;
        document.result = message.result;
        document.action = message.action;
        document.errorCode = message.errorCode;
        document.errorMessage = message.errorMessage;
        document.context = message.context;
        document.stats = message.stats;
        document.info = message.info;
        document.elapsed = message.elapsed;
        return document;
    }
}
