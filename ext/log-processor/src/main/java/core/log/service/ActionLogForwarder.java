package core.log.service;

import core.framework.kafka.MessagePublisher;
import core.framework.log.message.ActionLogMessage;
import core.log.LogForwardConfig;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author neo
 */
public class ActionLogForwarder {
    private final MessagePublisher<ActionLogMessage> publisher;
    private final String topic;
    private final Set<String> apps;
    private final Set<String> results;
    private final Set<String> ignoreActions;
    private final Set<String> ignoreErrorCodes;

    public ActionLogForwarder(MessagePublisher<ActionLogMessage> publisher, LogForwardConfig.Forward forward) {
        this.publisher = publisher;
        this.topic = forward.topic;
        this.apps = new HashSet<>(forward.apps);
        this.results = new HashSet<>(forward.results);
        this.ignoreActions = new HashSet<>(forward.ignoreActions);
        this.ignoreErrorCodes = new HashSet<>(forward.ignoreErrorCodes);
    }

    public void forward(List<ActionLogMessage> messages) {
        for (ActionLogMessage message : messages) {
            if (apps.contains(message.app)
                && (results.isEmpty() || results.contains(message.result))
                && !ignoreActions.contains(message.action)
                && !ignoreErrorCodes.contains(message.errorCode)) {
                publisher.publish(topic, null, message);
            }
        }
    }
}
