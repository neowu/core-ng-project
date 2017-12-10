package core.framework.impl.web.management;

import core.framework.api.http.HTTPStatus;
import core.framework.web.Controller;
import core.framework.web.Request;
import core.framework.web.Response;

/**
 * @author neo
 */
public class HealthCheckController implements Controller {
    @Override
    public Response execute(Request request) {
        return Response.empty().status(HTTPStatus.OK);
    }
}
