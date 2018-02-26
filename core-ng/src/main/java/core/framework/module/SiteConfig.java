package core.framework.module;

import core.framework.http.HTTPMethod;
import core.framework.impl.module.ModuleContext;
import core.framework.impl.web.site.StaticContentController;
import core.framework.impl.web.site.StaticDirectoryController;
import core.framework.impl.web.site.StaticFileController;
import core.framework.impl.web.site.WebSecurityInterceptor;
import core.framework.util.Exceptions;
import core.framework.web.site.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;

/**
 * @author neo
 */
public final class SiteConfig {
    private final Logger logger = LoggerFactory.getLogger(SiteConfig.class);

    private final ModuleContext context;
    private final State state;

    SiteConfig(ModuleContext context) {
        this.context = context;
        state = context.config.state("site", State::new);
    }

    public SessionConfig session() {
        return new SessionConfig(context);
    }

    public CDNConfig cdn() {
        return new CDNConfig(context);
    }

    public void message(List<String> paths, String... languages) {
        if (state.messageConfigured) {
            throw Exceptions.error("site().message() can only be configured once and must before site().template()");
        }
        state.messageConfigured = true;
        context.beanFactory.bind(Message.class, null, context.httpServer.siteManager.message);
        context.httpServer.siteManager.message.load(paths, languages);
    }

    public void template(String path, Class<?> modelClass) {
        state.messageConfigured = true; // can not configure message() after adding template
        context.httpServer.siteManager.templateManager.add(path, modelClass);
    }

    public StaticContentConfig staticContent(String path) {
        logger.info("add static content path, path={}", path);
        Path contentPath = context.httpServer.siteManager.webDirectory.path(path);
        if (!Files.exists(contentPath, LinkOption.NOFOLLOW_LINKS)) throw Exceptions.error("path does not exist, path={}", path);

        StaticContentController controller;
        if (Files.isDirectory(contentPath)) {
            controller = new StaticDirectoryController(contentPath);
            context.route(HTTPMethod.GET, path + "/:path(*)", controller, true);
        } else {
            controller = new StaticFileController(contentPath);
            context.route(HTTPMethod.GET, path, controller, true);
        }
        return new StaticContentConfig(controller);
    }

    public void enableWebSecurity() {
        context.httpServer.handler.interceptors.add(new WebSecurityInterceptor());
    }

    public static class State {
        boolean messageConfigured;
    }
}
