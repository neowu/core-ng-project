package core.log.kafka;

import core.framework.inject.Inject;
import core.framework.kafka.BulkMessageHandler;
import core.framework.kafka.Message;
import core.framework.log.message.EventMessage;
import core.framework.search.BulkIndexRequest;
import core.framework.search.ElasticSearchType;
import core.framework.util.Maps;
import core.log.domain.EventDocument;
import core.log.service.EventForwarder;
import core.log.service.IndexService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public class EventMessageHandler implements BulkMessageHandler<EventMessage> {
    final EventForwarder forwarder;

    @Inject
    IndexService indexService;
    @Inject
    ElasticSearchType<EventDocument> eventType;

    public EventMessageHandler(EventForwarder forwarder) {
        this.forwarder = forwarder;
    }

    @Override
    public void handle(List<Message<EventMessage>> messages) {
        index(messages, LocalDate.now());

        if (forwarder != null) forwarder.forward(messages);
    }

    void index(List<Message<EventMessage>> messages, LocalDate now) {
        Map<String, EventDocument> events = Maps.newHashMapWithExpectedSize(messages.size());
        for (Message<EventMessage> message : messages) {
            events.put(message.value.id, event(message.value));
        }
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
