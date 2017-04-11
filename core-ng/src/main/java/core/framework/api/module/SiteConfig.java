package core.framework.api.module;

import core.framework.api.http.HTTPMethod;
import core.framework.api.util.Exceptions;
import core.framework.api.web.site.Message;
import core.framework.impl.module.ModuleContext;
import core.framework.impl.web.ControllerHolder;
import core.framework.impl.web.site.StaticDirectoryController;
import core.framework.impl.web.site.StaticFileController;
import core.framework.impl.web.site.WebSecurityInterceptor;
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
    private final SiteConfig.SiteConfigState state;

    public SiteConfig(ModuleContext context) {
        this.context = context;
        state = context.config.site();
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

    public void staticContent(String path) {
        logger.info("add static content path, path={}", path);
        Path contentPath = context.httpServer.siteManager.webDirectory.path(path);
        if (!Files.exists(contentPath, LinkOption.NOFOLLOW_LINKS)) {
            throw Exceptions.error("path does not exist, path={}", path);
        }
        if (Files.isDirectory(contentPath)) {
            context.httpServer.handler.route.add(HTTPMethod.GET, path + "/:path(*)", new ControllerHolder(new StaticDirectoryController(contentPath), true));
        } else {
            context.httpServer.handler.route.add(HTTPMethod.GET, path, new ControllerHolder(new StaticFileController(contentPath), true));
        }
    }

    public void enableWebSecurity() {
        context.httpServer.handler.interceptors.add(new WebSecurityInterceptor());
    }

    public static class SiteConfigState {
        boolean messageConfigured;
    }
}
