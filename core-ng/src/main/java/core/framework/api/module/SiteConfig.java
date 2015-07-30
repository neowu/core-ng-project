package core.framework.api.module;

import core.framework.impl.module.ModuleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author neo
 */
public class SiteConfig {
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

    public void template(String path, Class<?> modelClass) {
        context.httpServer.siteManager.templateManager.add(path, modelClass);
    }

    public void staticContent(String root) {
        logger.info("add static content root, root={}", root);
        context.httpServer.get(root + "/:path(*)", context.httpServer.siteManager.staticContentController(root));
    }
}
