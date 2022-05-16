package core.log.service;

import core.framework.kafka.MessagePublisher;
import core.framework.log.message.ActionLogMessage;

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
    private final Set<String> ignoreErrorCodes;

    public ActionLogForwarder(MessagePublisher<ActionLogMessage> publisher, String topic, List<String> apps, List<String> results, List<String> ignoreErrorCodes) {
        this.publisher = publisher;
        this.topic = topic;
        this.apps = new HashSet<>(apps);
        this.results = new HashSet<>(results);
        this.ignoreErrorCodes = new HashSet<>(ignoreErrorCodes);
    }

    public void forward(List<ActionLogMessage> messages) {
        for (ActionLogMessage message : messages) {
            if (apps.contains(message.app)
                && (results.isEmpty() || results.contains(message.result))
                && !ignoreErrorCodes.contains(message.errorCode)) {
                publisher.publish(topic, null, message);
            }
        }
    }
}
