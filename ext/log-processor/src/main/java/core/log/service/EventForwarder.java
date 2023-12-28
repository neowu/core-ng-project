package core.log.service;

import core.framework.kafka.Message;
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
    private final Set<String> apps;
    private final Set<String> results;
    private final Set<String> ignoreActions;
    private final Set<String> ignoreErrorCodes;

    public EventForwarder(MessagePublisher<EventMessage> publisher, LogForwardConfig.Forward forward) {
        this.publisher = publisher;
        apps = new HashSet<>(forward.apps);
        results = new HashSet<>(forward.results);
        ignoreActions = new HashSet<>(forward.ignoreActions);
        ignoreErrorCodes = new HashSet<>(forward.ignoreErrorCodes);
    }

    public void forward(List<Message<EventMessage>> messages) {
        for (Message<EventMessage> message : messages) {
            EventMessage value = message.value;
            if (apps.contains(value.app)
                && (results.isEmpty() || results.contains(value.result))
                && !ignoreActions.contains(value.action)
                && !ignoreErrorCodes.contains(value.errorCode)) {
                publisher.publish(value);
            }
        }
    }
}
