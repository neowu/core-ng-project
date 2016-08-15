package app.web;

import app.web.interceptor.Protected;
import core.framework.api.http.ContentType;
import core.framework.api.web.Request;
import core.framework.api.web.Response;
import core.framework.api.web.Session;
import core.framework.api.web.site.Message;

import javax.inject.Inject;

/**
 * @author neo
 */
public class IndexController {
    @Inject
    Message message;
    @Inject
    LanguageManager languageManager;

    @Protected(operation = "index")
    public Response index(Request request) {
        IndexPage model = new IndexPage();
        model.name = message.get("key.name", languageManager.language()).orElse("world not found");

        Session session = request.session();
//        Optional<String> hello = session.get("hello");
        session.set("hello", "world");
        Response response = Response.html("/template/index.html", model, languageManager.language());
        response.cookie(Cookies.TEST, "1+2");
//        response.cookie(CookieConstraints.TEST1, null);
        return response;
    }

    public Response submit(Request request) {
        return Response.text("hello " + request.formParam("name").get(), ContentType.TEXT_PLAIN);
    }
}
