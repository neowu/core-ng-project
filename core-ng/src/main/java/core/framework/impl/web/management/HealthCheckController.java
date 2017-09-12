package core.framework.impl.web.management;

import core.framework.api.http.HTTPStatus;
import core.framework.api.web.Controller;
import core.framework.api.web.Request;
import core.framework.api.web.Response;

/**
 * @author neo
 */
public class HealthCheckController implements Controller {
    @Override
    public Response execute(Request request) throws Exception {
        return Response.empty().status(HTTPStatus.OK);
    }
}
