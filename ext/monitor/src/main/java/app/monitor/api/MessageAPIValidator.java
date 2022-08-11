package app.monitor.api;

import core.framework.internal.web.api.APIType;
import core.framework.internal.web.api.MessageAPIDefinitionResponse;
import core.framework.log.Severity;
import core.framework.util.Strings;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author neo
 */
public class MessageAPIValidator {
    private final Map<String, String> previousTopics;
    private final Map<String, String> currentTopics;

    private final APITypeValidator typeValidator;
    private final APIWarnings warnings = new APIWarnings();

    public MessageAPIValidator(MessageAPIDefinitionResponse previous, MessageAPIDefinitionResponse current) {
        previousTopics = previous.topics.stream().collect(Collectors.toMap(topic -> topic.name, topic -> topic.messageType));
        currentTopics = current.topics.stream().collect(Collectors.toMap(topic -> topic.name, topic -> topic.messageType));

        Map<String, APIType> previousTypes = previous.types.stream().collect(Collectors.toMap(type -> type.name, Function.identity()));
        Map<String, APIType> currentTypes = current.types.stream().collect(Collectors.toMap(type -> type.name, Function.identity()));
        typeValidator = new APITypeValidator(previousTypes, currentTypes, warnings);
    }

    public APIWarnings validate() {
        for (Map.Entry<String, String> entry : previousTopics.entrySet()) {
            String previousTopic = entry.getKey();
            String previousMessageClass = entry.getValue();
            String currentMessageClass = currentTopics.remove(previousTopic);
            if (currentMessageClass == null) {
                warnings.add(true, "removed message publisher, topic=" + previousTopic);
                typeValidator.removeReferenceType(previousMessageClass, Severity.WARN);
            } else {
                if (!Strings.equals(previousMessageClass, currentMessageClass)) {
                    warnings.add(true, "renamed message type of {} from {} to {}", previousTopic, previousMessageClass, currentMessageClass);
                }
                typeValidator.validateType(previousMessageClass, currentMessageClass, false);
            }
        }
        if (!currentTopics.isEmpty()) {
            for (String topic : currentTopics.keySet()) {
                warnings.add(true, "added message publisher, topic=" + topic);
            }
        }
        typeValidator.validateTypes();
        return warnings;
    }
}
