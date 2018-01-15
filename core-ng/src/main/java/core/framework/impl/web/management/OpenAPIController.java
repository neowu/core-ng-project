package core.framework.impl.web.management;

import core.framework.http.ContentType;
import core.framework.impl.web.api.OpenAPIManager;
import core.framework.web.Controller;
import core.framework.web.Request;
import core.framework.web.Response;

/**
 * @author neo
 */
public class OpenAPIController implements Controller {
    private final OpenAPIManager openAPIManager;

    public OpenAPIController(OpenAPIManager openAPIManager) {
        this.openAPIManager = openAPIManager;
    }

    @Override
    public Response execute(Request request) {
        ControllerHelper.assertFromLocalNetwork(request.clientIP());
        return Response.bytes(openAPIManager.document())
                       .contentType(ContentType.APPLICATION_JSON)
                       .header("Access-Control-Allow-Origin", "*");
    }
}
