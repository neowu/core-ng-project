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
        if (context.httpServer.siteManager.templateManager.messageManager.initialized)
            throw new Error("site().message().loadProperties() must not be called after site().template()");

        if (context.httpServer.siteManager.templateManager.languageProvider != null)
            throw new Error("site().message().loadProperties() must not be called after site().message().language()");

        context.httpServer.siteManager.templateManager.messageManager.loadProperties(path);
    }

    public void language(LanguageProvider provider, String... languages) {
        if (provider == null) throw new Error("language provider must not be null");
        if (languages.length == 0) throw new Error("languages must not be empty");

        context.httpServer.siteManager.templateManager.languageProvider = provider;
        context.httpServer.siteManager.templateManager.messageManager.languages = languages;
    }
}
