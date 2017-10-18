package core.framework.module;

import core.framework.impl.module.ModuleContext;

/**
 * @author neo
 */
public final class CDNConfig {
    private final ModuleContext context;

    CDNConfig(ModuleContext context) {
        this.context = context;
    }

    public void host(String host) {
        context.httpServer.siteManager.templateManager.cdnManager.host(host);
    }
}
