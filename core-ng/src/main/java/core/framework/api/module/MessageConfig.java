package core.framework.api.module;

import core.framework.api.web.site.LanguageProvider;
import core.framework.impl.module.ModuleContext;

/**
 * @author neo
 */
public final class MessageConfig {
    private final ModuleContext context;

    public MessageConfig(ModuleContext context) {
        this.context = context;
    }

    public void loadProperties(String path) {
        context.httpServer.siteManager.templateManager.messageManager.loadProperties(path);
    }

    public void language(LanguageProvider provider) {
        context.httpServer.siteManager.templateManager.languageProvider = provider;
    }
}
