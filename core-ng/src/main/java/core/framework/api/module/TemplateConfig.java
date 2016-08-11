package core.framework.api.module;

import core.framework.impl.module.ModuleContext;

/**
 * @author neo
 */
public final class TemplateConfig {
    private final ModuleContext context;

    public TemplateConfig(ModuleContext context) {
        this.context = context;
    }

    public void add(String path, Class<?> modelClass) {
        if (context.httpServer.siteManager.templateManager.templates.isEmpty()) {
            context.httpServer.siteManager.templateManager.messageManager.validate();
        }
        context.httpServer.siteManager.templateManager.add(path, modelClass);
    }

    public void message(String path) {
        if (!context.httpServer.siteManager.templateManager.templates.isEmpty()) {
            throw new Error("site().template().message() must be called before site().template().add()");
        }
        context.httpServer.siteManager.templateManager.messageManager.load(path);
    }

    public void languages(String... languages) {
        if (!context.httpServer.siteManager.templateManager.templates.isEmpty()) {
            throw new Error("site().template().languages() must be called before site().template().add()");
        }
        if (languages.length == 0) throw new Error("languages must not be empty");
        context.httpServer.siteManager.templateManager.messageManager.languages = languages;
    }
}
