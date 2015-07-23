package app.web.site;

import app.web.interceptor.Protected;
import core.framework.api.http.ContentTypes;
import core.framework.api.web.Request;
import core.framework.api.web.Response;

/**
 * @author neo
 */
public class IndexController {
    @Protected(operation = "index")
    public Response index(Request request) {
        IndexPage model = new IndexPage();
        model.name = "world";

//        Session session = request.session();
//        Optional<String> hello = session.get("hello");
//        session.set("hello", "world");
//        response.cookie(CookieConstraints.TEST, null);
//        response.cookie(CookieConstraints.TEST1, null);

        return Response.html("/template/index.html", model);
    }

    public Response css(Request request) {
        IndexPage model = new IndexPage();
        model.backgroundColor = "gainsboro";
        return Response.template("/template/css/main.css", model).contentType(ContentTypes.TEXT_CSS);
    }
}
