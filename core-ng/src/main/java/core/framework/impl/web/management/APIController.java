package core.framework.impl.web.management;

import core.framework.impl.web.api.APIDefinitionBuilder;
import core.framework.impl.web.api.APIDefinitionResponse;
import core.framework.impl.web.http.IPAccessControl;
import core.framework.web.Controller;
import core.framework.web.Request;
import core.framework.web.Response;

import java.util.Map;

/**
 * @author neo
 */
public class APIController implements Controller {
    private final Map<String, Class<?>> serviceInterfaces;
    private final IPAccessControl accessControl;

    public APIController(Map<String, Class<?>> serviceInterfaces, IPAccessControl accessControl) {
        this.serviceInterfaces = serviceInterfaces;
        this.accessControl = accessControl;
    }

    @Override
    public Response execute(Request request) {
        accessControl.validate(request.clientIP());

        APIDefinitionBuilder builder = new APIDefinitionBuilder();
        serviceInterfaces.values().forEach(builder::addServiceInterface);
        APIDefinitionResponse response = builder.build();

        return Response.bean(response);
    }
}
