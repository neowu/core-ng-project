package core.framework.impl.web.management;

import core.framework.impl.web.api.APIDefinitionBuilder;
import core.framework.impl.web.api.APIDefinitionResponse;
import core.framework.impl.web.http.IPAccessControl;
import core.framework.web.Controller;
import core.framework.web.Request;
import core.framework.web.Response;

import java.util.Map;
import java.util.Set;

/**
 * @author neo
 */
public class APIController implements Controller {
    private final Map<String, Class<?>> serviceInterfaces;
    private final Set<Class<?>> beanClasses;
    private final Set<Class<?>> queryParamBeanClasses;

    private final IPAccessControl accessControl;

    public APIController(Map<String, Class<?>> serviceInterfaces, Set<Class<?>> beanClasses, Set<Class<?>> queryParamBeanClasses, IPAccessControl accessControl) {
        this.serviceInterfaces = serviceInterfaces;
        this.beanClasses = beanClasses;
        this.queryParamBeanClasses = queryParamBeanClasses;
        this.accessControl = accessControl;
    }

    @Override
    public Response execute(Request request) {
        accessControl.validate(request.clientIP());

        var builder = new APIDefinitionBuilder();
        beanClasses.forEach(builder::parseBeanType);
        queryParamBeanClasses.forEach(builder::parseBeanType);
        serviceInterfaces.values().forEach(builder::addServiceInterface);
        APIDefinitionResponse response = builder.build();

        return Response.bean(response);
    }
}
