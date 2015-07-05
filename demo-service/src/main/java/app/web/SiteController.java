package app.web;

import core.framework.api.http.ContentTypes;
import core.framework.api.web.Request;
import core.framework.api.web.Response;

/**
 * @author neo
 */
public class SiteController {
    public Response post(Request request) {
        return Response.text("post param test=" + request.queryParam("test"), ContentTypes.TEXT_PLAIN);
    }

    public Response wildcard(Request request) {
        return Response.text("catch full url", "text/html");
    }
}
