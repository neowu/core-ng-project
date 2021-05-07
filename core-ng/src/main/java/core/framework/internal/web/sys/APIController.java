package core.framework.internal.web.sys;

import core.framework.http.ContentType;
import core.framework.internal.module.ServiceRegistry;
import core.framework.internal.web.api.APIDefinitionBuilder;
import core.framework.internal.web.api.APIDefinitionResponse;
import core.framework.internal.web.api.APIDefinitionV2Builder;
import core.framework.internal.web.api.APIDefinitionV2Response;
import core.framework.internal.web.http.IPv4AccessControl;
import core.framework.json.JSON;
import core.framework.web.Request;
import core.framework.web.Response;

/**
 * @author neo
 */
public class APIController {
    private final ServiceRegistry registry;
    private final IPv4AccessControl accessControl;

    public APIController(ServiceRegistry registry, IPv4AccessControl accessControl) {
        this.registry = registry;
        this.accessControl = accessControl;
    }

    public Response v1(Request request) {
        accessControl.validate(request.clientIP());

        var builder = new APIDefinitionBuilder();
        registry.serviceInterfaces.forEach(builder::addServiceInterface);
        registry.beanClasses.forEach(builder::parseType);
        APIDefinitionResponse response = builder.build();

        return Response.text(JSON.toJSON(response)).contentType(ContentType.APPLICATION_JSON);
    }

    public Response v2(Request request) {
        accessControl.validate(request.clientIP());

        var builder = new APIDefinitionV2Builder(registry.serviceInterfaces, registry.beanClasses);
        APIDefinitionV2Response response = builder.build();

        return Response.text(JSON.toJSON(response)).contentType(ContentType.APPLICATION_JSON);
    }
}
