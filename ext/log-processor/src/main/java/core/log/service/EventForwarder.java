package core.log.service;

import core.framework.kafka.MessagePublisher;
import core.framework.log.message.EventMessage;
import core.log.LogForwardConfig;

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
    private final Set<String> ignoreActions;
    private final Set<String> ignoreErrorCodes;

    public EventForwarder(MessagePublisher<EventMessage> publisher, LogForwardConfig.Forward forward) {
        this.publisher = publisher;
        topic = forward.topic;
        apps = new HashSet<>(forward.apps);
        results = new HashSet<>(forward.results);
        ignoreActions = new HashSet<>(forward.ignoreActions);
        ignoreErrorCodes = new HashSet<>(forward.ignoreErrorCodes);
    }

    public void forward(List<EventMessage> messages) {
        for (EventMessage message : messages) {
            if (apps.contains(message.app)
                && (results.isEmpty() || results.contains(message.result))
                && !ignoreActions.contains(message.action)
                && !ignoreErrorCodes.contains(message.errorCode)) {
                publisher.publish(topic, null, message);
            }
        }
    }
}
