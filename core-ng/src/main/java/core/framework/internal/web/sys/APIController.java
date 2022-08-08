package core.framework.internal.web.sys;

import core.framework.http.ContentType;
import core.framework.internal.web.api.APIDefinitionBuilder;
import core.framework.internal.web.api.APIDefinitionResponse;
import core.framework.internal.web.api.APIMessageDefinitionBuilder;
import core.framework.internal.web.api.APIMessageDefinitionResponse;
import core.framework.internal.web.http.IPv4AccessControl;
import core.framework.internal.web.service.ErrorResponse;
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

    private APIDefinitionResponse serviceDefinition;
    private APIMessageDefinitionResponse messageDefinition;

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
        APIMessageDefinitionResponse response = messageDefinition();
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

    APIMessageDefinitionResponse messageDefinition() {
        synchronized (this) {
            if (messageDefinition == null) {
                var builder = new APIMessageDefinitionBuilder(messages);
                messageDefinition = builder.build();
                messages = null;    // release memory
            }
            return messageDefinition;
        }
    }

    /**
     * @param topic topic can be null, for dynamic topic message publish
     */
    public record MessagePublish(String topic, Class<?> messageClass) {
    }
}
