package core.framework.internal.web.sys;

import core.framework.http.ContentType;
import core.framework.internal.web.api.APIDefinitionBuilder;
import core.framework.internal.web.api.APIDefinitionResponse;
import core.framework.internal.web.api.MessageAPIDefinitionBuilder;
import core.framework.internal.web.api.MessageAPIDefinitionResponse;
import core.framework.internal.web.http.IPv4AccessControl;
import core.framework.internal.web.service.ErrorResponse;
import core.framework.json.JSON;
import core.framework.web.Request;
import core.framework.web.Response;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author neo
 */
public class APIController {
    public final IPv4AccessControl accessControl = new IPv4AccessControl();

    public Set<Class<?>> serviceInterfaces = new LinkedHashSet<>();
    public Set<Class<?>> beanClasses = new LinkedHashSet<>();  // custom bean classes not referred by service interfaces
    public Map<String, Class<?>> topics = new LinkedHashMap<>();   // topic -> messageClass

    private APIDefinitionResponse serviceDefinition;
    private MessageAPIDefinitionResponse messageDefinition;

    public APIController() {
        beanClasses.add(ErrorResponse.class);   // publish default error response
    }

    public Response service(Request request) {
        accessControl.validate(request.clientIP());
        APIDefinitionResponse response = serviceDefinition();
        return Response.text(JSON.toJSON(response)).contentType(ContentType.APPLICATION_JSON);
    }

    public Response message(Request request) {
        accessControl.validate(request.clientIP());
        MessageAPIDefinitionResponse response = messageDefinition();
        return Response.text(JSON.toJSON(response)).contentType(ContentType.APPLICATION_JSON);
    }

    APIDefinitionResponse serviceDefinition() {
        synchronized (this) {
            if (serviceDefinition == null) {
                var builder = new APIDefinitionBuilder(serviceInterfaces, beanClasses);
                serviceDefinition = builder.build();
                serviceInterfaces = null;   // release memory
                beanClasses = null;
            }
            return serviceDefinition;
        }
    }

    MessageAPIDefinitionResponse messageDefinition() {
        synchronized (this) {
            if (messageDefinition == null) {
                var builder = new MessageAPIDefinitionBuilder(topics);
                messageDefinition = builder.build();
                topics = null;    // release memory
            }
            return messageDefinition;
        }
    }
}
