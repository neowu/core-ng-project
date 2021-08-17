package core.framework.internal.web.sys;

import core.framework.http.ContentType;
import core.framework.internal.web.api.APIDefinitionBuilder;
import core.framework.internal.web.api.APIDefinitionResponse;
import core.framework.internal.web.api.APIMessageDefinitionBuilder;
import core.framework.internal.web.api.APIMessageDefinitionResponse;
import core.framework.internal.web.http.IPv4AccessControl;
import core.framework.json.JSON;
import core.framework.web.Request;
import core.framework.web.Response;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author neo
 */
public class APIController {
    public final IPv4AccessControl accessControl = new IPv4AccessControl();

    public Set<Class<?>> serviceInterfaces = new LinkedHashSet<>();
    public Set<Class<?>> beanClasses = new LinkedHashSet<>();  // custom bean classes not referred by service interfaces
    public List<MessagePublish> messages = new ArrayList<>();

    private APIDefinitionResponse apiDefinition;
    private APIMessageDefinitionResponse messageDefinition;

    public Response service(Request request) {
        accessControl.validate(request.clientIP());
        APIDefinitionResponse response = apiDefinition();
        return Response.text(JSON.toJSON(response)).contentType(ContentType.APPLICATION_JSON);
    }

    private APIDefinitionResponse apiDefinition() {
        synchronized (this) {
            if (apiDefinition == null) {
                var builder = new APIDefinitionBuilder(serviceInterfaces, beanClasses);
                apiDefinition = builder.build();
                serviceInterfaces = null;
                beanClasses = null;
            }
            return apiDefinition;
        }
    }

    public Response message(Request request) {
        accessControl.validate(request.clientIP());
        APIMessageDefinitionResponse response = messageDefinition();
        return Response.text(JSON.toJSON(response)).contentType(ContentType.APPLICATION_JSON);
    }

    private APIMessageDefinitionResponse messageDefinition() {
        synchronized (this) {
            if (messageDefinition == null) {
                var builder = new APIMessageDefinitionBuilder(messages);
                messageDefinition = builder.build();
                messages = null;
            }
            return messageDefinition;
        }
    }

    public static class MessagePublish {
        public final String topic;  // topic can be null, for dynamic topic message publish
        public final Class<?> messageClass;

        public MessagePublish(String topic, Class<?> messageClass) {
            this.topic = topic;
            this.messageClass = messageClass;
        }
    }
}
