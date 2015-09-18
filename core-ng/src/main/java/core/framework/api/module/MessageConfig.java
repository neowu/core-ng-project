package core.framework.api.module;

import core.framework.api.web.site.MessageProvider;
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
        context.httpServer.siteManager.messageManager.loadProperties(path);
    }

    public void messageProvider(MessageProvider messageProvider) {
        context.httpServer.siteManager.messageManager.messageProvider = messageProvider;
    }
}
