package core.framework.impl.web.management;

import core.framework.http.ContentType;
import core.framework.impl.web.api.TypescriptDefinitionBuilder;
import core.framework.impl.web.http.IPAccessControl;
import core.framework.util.Lists;
import core.framework.web.Controller;
import core.framework.web.Request;
import core.framework.web.Response;

import java.util.List;

/**
 * @author neo
 */
public class APIController implements Controller {
    public final List<Class<?>> serviceInterfaces = Lists.newArrayList();
    private final IPAccessControl accessControl;

    public APIController(IPAccessControl accessControl) {
        this.accessControl = accessControl;
    }

    @Override
    public Response execute(Request request) {
        accessControl.validateClientIP(request.clientIP());

        TypescriptDefinitionBuilder builder = new TypescriptDefinitionBuilder();
        serviceInterfaces.forEach(builder::addServiceInterface);
        String definition = builder.build();

        return Response.text(definition).contentType(ContentType.APPLICATION_JAVASCRIPT);
    }
}
