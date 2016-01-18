package app.web;

import app.web.interceptor.Protected;
import core.framework.api.http.ContentType;
import core.framework.api.util.Files;
import core.framework.api.web.Request;
import core.framework.api.web.Response;
import core.framework.api.web.Session;
import core.framework.api.web.site.WebDirectory;

import javax.inject.Inject;
import java.util.Optional;

/**
 * @author neo
 */
public class IndexController {
    @Inject
    WebDirectory webDirectory;

    @Protected(operation = "index")
    public Response index(Request request) {
        IndexPage model = new IndexPage();
        model.name = "world";

        Session session = request.session();
        Optional<String> hello = session.get("hello");
        session.set("hello", "world");
        Response response = Response.html("/template/index.html", model);
        response.cookie(Cookies.TEST, "1+2");
//        response.cookie(CookieConstraints.TEST1, null);
        return response;
    }

    // just simple demo for non-html template, real project needs sophisticated impl
    public Response css(Request request) {
        String cssTemplate = Files.text(webDirectory.path("/template/css/main.css"));
        String css = cssTemplate.replaceAll("$\\{backgroundColor\\}", "gainsboro");
        return Response.text(css, ContentType.TEXT_CSS);
    }

    public Response submit(Request request) {
        return Response.text("hello " + request.formParam("name").get(), ContentType.TEXT_PLAIN);
    }
}
