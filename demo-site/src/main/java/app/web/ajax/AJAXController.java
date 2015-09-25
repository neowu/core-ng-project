package app.web.ajax;

import core.framework.api.http.ContentTypes;
import core.framework.api.web.Request;
import core.framework.api.web.Response;

/**
 * @author neo
 */
public class AJAXController {
    public Response ajax(Request request) {
        Bean bean = request.bean(Bean.class);
        return Response.text("hello " + bean.name, ContentTypes.TEXT_PLAIN);
    }
}
