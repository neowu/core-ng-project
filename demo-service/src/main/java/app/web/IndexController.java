package app.web;

import app.web.interceptor.Protected;
import core.framework.api.util.Maps;
import core.framework.api.web.Request;
import core.framework.api.web.Response;

/**
 * @author neo
 */
public class IndexController {
    @Protected(operation = "index")
    public Response index(Request request) {
        Response response = Response.html("template/index.html", Maps.newHashMap());
//        Session session = request.session();
//
//        Optional<String> hello = session.get("hello");
//
//        session.set("hello", "world");
//        response.cookie(CookieConstraints.TEST, null);
//        response.cookie(CookieConstraints.TEST1, null);
        return response;
    }
}
