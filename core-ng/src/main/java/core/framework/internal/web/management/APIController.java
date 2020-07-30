package core.framework.internal.web.management;

import core.framework.http.ContentType;
import core.framework.internal.module.ServiceRegistry;
import core.framework.internal.web.api.APIDefinitionBuilder;
import core.framework.internal.web.api.APIDefinitionResponse;
import core.framework.internal.web.http.IPv4AccessControl;
import core.framework.json.JSON;
import core.framework.web.Controller;
import core.framework.web.Request;
import core.framework.web.Response;

/**
 * @author neo
 */
public class APIController implements Controller {
    private final ServiceRegistry registry;
    private final IPv4AccessControl accessControl;

    public APIController(ServiceRegistry registry, IPv4AccessControl accessControl) {
        this.registry = registry;
        this.accessControl = accessControl;
    }

    @Override
    public Response execute(Request request) {
        accessControl.validate(request.clientIP());

        var builder = new APIDefinitionBuilder();
        registry.serviceInterfaces.forEach(builder::addServiceInterface);
        registry.beanClasses.forEach(builder::parseType);
        APIDefinitionResponse response = builder.build();

        return Response.text(JSON.toJSON(response)).contentType(ContentType.APPLICATION_JSON);
    }
}
