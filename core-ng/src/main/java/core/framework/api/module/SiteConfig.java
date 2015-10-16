package core.framework.api.module;

import core.framework.api.http.HTTPMethod;
import core.framework.api.util.Exceptions;
import core.framework.impl.module.ModuleContext;
import core.framework.impl.web.ControllerHolder;
import core.framework.impl.web.site.StaticDirectoryController;
import core.framework.impl.web.site.StaticFileController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

/**
 * @author neo
 */
public final class SiteConfig {
    private final Logger logger = LoggerFactory.getLogger(SiteConfig.class);

    private final ModuleContext context;

    public SiteConfig(ModuleContext context) {
        this.context = context;
    }

    public SessionConfig session() {
        return new SessionConfig(context);
    }

    public MessageConfig message() {
        return new MessageConfig(context);
    }

    public CDNConfig cdn() {
        return new CDNConfig(context);
    }

    public void template(String path, Class<?> modelClass) {
        context.httpServer.siteManager.templateManager.add(path, modelClass);
    }

    public void staticContent(String path) {
        logger.info("add static content path, path={}", path);
        Path contentPath = context.httpServer.siteManager.webDirectory.path(path);

        if (!Files.exists(contentPath, LinkOption.NOFOLLOW_LINKS))
            throw Exceptions.error("path does not exist, path={}", path);

        if (Files.isDirectory(contentPath)) {
            context.httpServer.handler.route.add(HTTPMethod.GET, path + "/:path(*)", new ControllerHolder(new StaticDirectoryController(contentPath), true));
        } else {
            context.httpServer.handler.route.add(HTTPMethod.GET, path, new ControllerHolder(new StaticFileController(contentPath), true));
        }
    }
}
