package core.framework.internal.web.sys;

import core.framework.http.ContentType;
import core.framework.internal.module.ServiceRegistry;
import core.framework.internal.web.api.APIDefinitionBuilder;
import core.framework.internal.web.api.APIDefinitionResponse;
import core.framework.internal.web.api.APIMessageDefinitionBuilder;
import core.framework.internal.web.api.APIMessageDefinitionResponse;
import core.framework.internal.web.http.IPv4AccessControl;
import core.framework.json.JSON;
import core.framework.web.Request;
import core.framework.web.Response;

/**
 * @author neo
 */
public class APIController {
    public final ServiceRegistry registry = new ServiceRegistry();
    public final IPv4AccessControl accessControl = new IPv4AccessControl();

    public Response service(Request request) {
        accessControl.validate(request.clientIP());
        var builder = new APIDefinitionBuilder(registry.serviceInterfaces, registry.beanClasses);
        APIDefinitionResponse response = builder.build();
        return Response.text(JSON.toJSON(response)).contentType(ContentType.APPLICATION_JSON);
    }

    public Response message(Request request) {
        accessControl.validate(request.clientIP());
        var builder = new APIMessageDefinitionBuilder(registry.messages);
        APIMessageDefinitionResponse response = builder.build();
        return Response.text(JSON.toJSON(response)).contentType(ContentType.APPLICATION_JSON);
    }
}
