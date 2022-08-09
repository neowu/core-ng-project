package core.framework.internal.web.api;

import core.framework.internal.log.LogManager;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

/**
 * @author neo
 */
public class APIMessageDefinitionBuilder {
    private final Map<String, Class<?>> topics;
    private final APITypeParser parser = new APITypeParser();

    public APIMessageDefinitionBuilder(Map<String, Class<?>> topics) {
        this.topics = topics;
    }

    public APIMessageDefinitionResponse build() {
        var response = new APIMessageDefinitionResponse();
        response.app = LogManager.APP_NAME;
        response.version = UUID.randomUUID().toString();
        response.topics = new ArrayList<>(topics.size());
        for (Map.Entry<String, Class<?>> entry : topics.entrySet()) {
            var topic = new APIMessageDefinitionResponse.Topic();
            topic.name = entry.getKey();
            topic.messageType = parser.parseBeanType(entry.getValue());
            response.topics.add(topic);
        }
        response.types = parser.types();
        return response;
    }
}
