package core.framework.api.module;

import core.framework.impl.module.ModuleContext;

/**
 * @author neo
 */
public final class CDNConfig {
    private final ModuleContext context;

    public CDNConfig(ModuleContext context) {
        this.context = context;
    }

    public void hosts(String... hosts) {
        context.httpServer.siteManager.templateManager.cdnManager.hosts(hosts);
    }

    public void version(String version) {
        String cdnVersion = version;
        if (version.startsWith("${")) cdnVersion = "local";
        context.httpServer.siteManager.templateManager.cdnManager.version(cdnVersion);
    }
}
