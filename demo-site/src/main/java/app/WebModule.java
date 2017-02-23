package app;

import app.web.IndexController;
import app.web.IndexPage;
import app.web.LanguageManager;
import app.web.UploadController;
import app.web.UploadPage;
import app.web.WildcardController;
import app.web.ajax.AJAXController;
import app.web.interceptor.TestInterceptor;
import core.framework.api.Module;
import core.framework.api.http.ContentType;
import core.framework.api.http.HTTPStatus;
import core.framework.api.util.Lists;
import core.framework.api.web.Response;

import java.util.List;

/**
 * @author neo
 */
public class WebModule extends Module {
    @Override
    protected void initialize() {
        http().intercept(bind(TestInterceptor.class));

        route().get("/hello", request -> Response.text("hello", HTTPStatus.CREATED, ContentType.TEXT_PLAIN));
        route().get("/hello/", request -> Response.text("hello with trailing slash", HTTPStatus.CREATED, ContentType.TEXT_PLAIN));
        route().get("/hello/:name", request -> Response.text("hello " + request.pathParam("name"), HTTPStatus.CREATED, ContentType.TEXT_PLAIN));
        route().get("/hello-redirect", request -> Response.redirect("/hello"));

        site().staticContent("/static");
        site().staticContent("/favicon.ico");
        site().staticContent("/robots.txt");

        List<String> messages = Lists.newArrayList("messages/main.properties", "messages/main_en.properties", "messages/main_en_CA.properties");
        site().message(messages, "en_US", "en_CA");

        site().template("/template/index.html", IndexPage.class);
        site().template("/template/upload.html", UploadPage.class);

        bind(LanguageManager.class);

        IndexController index = bind(IndexController.class);
        route().get("/", index::index);
        route().post("/submit", index::submit);
        route().get("/logout", index::logout);

        UploadController upload = bind(UploadController.class);
        route().get("/upload", upload::get);
        route().post("/upload", upload::post);

        route().post("/ajax", bind(AJAXController.class)::ajax);

        WildcardController wildcardController = bind(WildcardController.class);
        route().get("/:all(*)", wildcardController::wildcard);
    }
}