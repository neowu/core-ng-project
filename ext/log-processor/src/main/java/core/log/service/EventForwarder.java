package core.log.service;

import core.framework.kafka.MessagePublisher;
import core.framework.log.message.EventMessage;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author neo
 */
public class EventForwarder {
    private final MessagePublisher<EventMessage> publisher;
    private final String topic;
    private final Set<String> apps;
    private final Set<String> results;
    private final Set<String> ignoreErrorCodes;

    public EventForwarder(MessagePublisher<EventMessage> publisher, String topic, List<String> apps, List<String> results, List<String> ignoreErrorCodes) {
        this.publisher = publisher;
        this.topic = topic;
        this.apps = new HashSet<>(apps);
        this.results = new HashSet<>(results);
        this.ignoreErrorCodes = new HashSet<>(ignoreErrorCodes);
    }

    public void forward(List<EventMessage> messages) {
        for (EventMessage message : messages) {
            if (apps.contains(message.app)
                && (results.isEmpty() || results.contains(message.result))
                && !ignoreErrorCodes.contains(message.errorCode)) {
                publisher.publish(topic, null, message);
            }
        }
    }
}
