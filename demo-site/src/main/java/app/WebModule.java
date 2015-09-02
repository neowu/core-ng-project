package app;

import app.web.IndexController;
import app.web.IndexPage;
import app.web.WildcardController;
import app.web.interceptor.TestInterceptor;
import core.framework.api.Module;
import core.framework.api.http.ContentTypes;
import core.framework.api.http.HTTPStatus;
import core.framework.api.web.Response;

/**
 * @author neo
 */
public class WebModule extends Module {
    @Override
    protected void initialize() {
        http().intercept(bind(TestInterceptor.class));

        route().get("/hello", request -> Response.text("hello", HTTPStatus.CREATED, ContentTypes.TEXT_PLAIN));
        route().get("/hello/", request -> Response.text("hello with ending slash", HTTPStatus.CREATED, ContentTypes.TEXT_PLAIN));
        route().get("/hello/:name", request -> Response.text("hello " + request.pathParam("name"), HTTPStatus.CREATED, ContentTypes.TEXT_PLAIN));

        site().template("/template/index.html", IndexPage.class);
        site().staticContent("/static");
        site().message().loadProperties("messages/main.properties");

        IndexController controller = bind(IndexController.class);
        route().get("/", controller::index);
        route().get("/css/main.css", controller::css);

        WildcardController wildcardController = bind(WildcardController.class);
        route().get("/:all(*)", wildcardController::wildcard);
    }
}