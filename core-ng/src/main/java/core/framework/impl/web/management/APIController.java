package core.framework.impl.web.management;

import core.framework.http.ContentType;
import core.framework.impl.web.api.TypescriptDefinitionBuilder;
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

    @Override
    public Response execute(Request request) {
        ControllerHelper.assertFromLocalNetwork(request.clientIP());

        TypescriptDefinitionBuilder builder = new TypescriptDefinitionBuilder();
        serviceInterfaces.forEach(builder::addServiceInterface);
        String definition = builder.build();

        return Response.text(definition).contentType(ContentType.APPLICATION_JAVASCRIPT);
    }
}
