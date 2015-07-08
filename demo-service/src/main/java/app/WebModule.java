package app;

import app.web.APITestController;
import app.web.AssetController;
import app.web.AsyncTestController;
import app.web.IndexController;
import app.web.ProductController;
import app.web.ProductWebService;
import app.web.SiteController;
import app.web.interceptor.TestInterceptor;
import core.framework.api.Module;
import core.framework.api.http.ContentTypes;
import core.framework.api.http.HTTPStatus;
import core.framework.api.web.Response;

/**
 * @author neo
 */
public class WebModule extends Module {
    // also support to create specific purpose module out of application
    @Override
    protected void initialize() {
        http().intercept(bind(TestInterceptor.class));

        route().get("/hello", request -> Response.text("hello", HTTPStatus.CREATED, ContentTypes.TEXT_PLAIN));
        route().get("/hello/", request -> Response.text("hello with ending slash", HTTPStatus.CREATED, ContentTypes.TEXT_PLAIN));
        route().get("/hello/:name", request -> Response.text("hello " + request.pathParam("name"), HTTPStatus.CREATED, ContentTypes.TEXT_PLAIN));

        route().get("/index", bind(IndexController.class)::index);

        SiteController siteController = bind(SiteController.class);
        route().post("/form", siteController::post);
        route().get("/:all(*)", siteController::wildcard);

        route().get("/static/:asset(*)", new AssetController("static"));

        api().service(ProductWebService.class, bind(ProductController.class));

        api().client(ProductWebService.class, "http://localhost:8080");

        route().get("/api-test", bind(APITestController.class)::get);

        route().get("/async-test", bind(AsyncTestController.class));
    }
}
