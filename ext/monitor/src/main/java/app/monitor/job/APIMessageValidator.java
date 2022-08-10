package app.monitor.job;

import core.framework.internal.web.api.APIMessageDefinitionResponse;
import core.framework.internal.web.api.APIType;
import core.framework.util.Strings;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author neo
 */
public class APIMessageValidator {

    private final Map<String, String> previousTopics;
    private final Map<String, String> currentTopics;

    private final APITypeValidator typeValidator;
    private final APIWarnings warnings = new APIWarnings();

    APIMessageValidator(APIMessageDefinitionResponse previous, APIMessageDefinitionResponse current) {
        previousTopics = previous.topics.stream().collect(Collectors.toMap(topic -> topic.name, topic -> topic.messageType));
        currentTopics = current.topics.stream().collect(Collectors.toMap(topic -> topic.name, topic -> topic.messageType));

        Map<String, APIType> previousTypes = previous.types.stream().collect(Collectors.toMap(type -> type.name, Function.identity()));
        Map<String, APIType> currentTypes = current.types.stream().collect(Collectors.toMap(type -> type.name, Function.identity()));
        typeValidator = new APITypeValidator(previousTypes, currentTypes, warnings);
    }

    APIWarnings validate() {
        for (Map.Entry<String, String> entry : previousTopics.entrySet()) {
            String previousTopic = entry.getKey();
            String previousMessageClass = entry.getValue();
            String currentMessageClass = currentTopics.remove(previousTopic);
            if (currentMessageClass == null) {
                warnings.add("removed message publishing, topic=" + previousTopic);
            } else if (!Strings.equals(previousMessageClass, currentMessageClass)) {
                warnings.add(true, "renamed message type of {} from {} to {}", previousTopic, previousMessageClass, currentMessageClass);
            }
            typeValidator.validateType(previousMessageClass, currentMessageClass, false);
        }
        if (!currentTopics.isEmpty()) {
            for (String topic : currentTopics.keySet()) {
                warnings.add("added message publishing, topic=" + topic);
            }
        }
        typeValidator.validateTypes();
        return warnings;
    }
}
