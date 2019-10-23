package core.framework.internal.web.management;

import core.framework.internal.web.api.APIDefinitionBuilder;
import core.framework.internal.web.api.APIDefinitionResponse;
import core.framework.internal.web.http.IPv4AccessControl;
import core.framework.web.Controller;
import core.framework.web.Request;
import core.framework.web.Response;

import java.util.Set;

/**
 * @author neo
 */
public class APIController implements Controller {
    private final Set<Class<?>> serviceInterfaces;
    private final Set<Class<?>> beanClasses;
    private final IPv4AccessControl accessControl;

    public APIController(Set<Class<?>> serviceInterfaces, Set<Class<?>> beanClasses, IPv4AccessControl accessControl) {
        this.serviceInterfaces = serviceInterfaces;
        this.beanClasses = beanClasses;
        this.accessControl = accessControl;
    }

    @Override
    public Response execute(Request request) {
        accessControl.validate(request.clientIP());

        var builder = new APIDefinitionBuilder();
        serviceInterfaces.forEach(builder::addServiceInterface);
        beanClasses.forEach(builder::parseType);
        APIDefinitionResponse response = builder.build();

        return Response.bean(response);
    }
}
