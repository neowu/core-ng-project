package core.log.kafka;

import core.framework.inject.Inject;
import core.framework.kafka.BulkMessageHandler;
import core.framework.kafka.Message;
import core.framework.log.message.EventMessage;
import core.log.service.EventService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author neo
 */
public class EventMessageHandler implements BulkMessageHandler<EventMessage> {
    @Inject
    EventService eventService;

    @Override
    public void handle(List<Message<EventMessage>> messages) {
        List<EventMessage> stats = new ArrayList<>(messages.size());
        for (Message<EventMessage> message : messages) {
            stats.add(message.value);
        }
        eventService.index(stats);
    }
}
