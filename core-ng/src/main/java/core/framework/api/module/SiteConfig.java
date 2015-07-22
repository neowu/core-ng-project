package core.framework.api.module;

import core.framework.impl.module.ModuleContext;
import core.framework.impl.web.StaticContentController;
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

    public void template(String path, Class<?> modelClass) {
        context.httpServer.templateManager.addTemplate(path, modelClass);
    }

    public void staticContent(String root) {
        logger.info("add static content root, root={}", root);
        context.httpServer.get(root + "/:path(*)", new StaticContentController(context.httpServer.webDirectory, root));
    }
}
