package core.framework.api.module;

import core.framework.impl.module.ModuleContext;

/**
 * @author neo
 */
public class SiteConfig {
    private final ModuleContext context;

    public SiteConfig(ModuleContext context) {
        this.context = context;
    }

    public SessionConfig session() {
        return new SessionConfig(context);
    }
}
