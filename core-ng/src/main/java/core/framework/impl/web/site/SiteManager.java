package core.framework.impl.web.site;

import core.framework.api.web.Controller;
import core.framework.impl.web.session.SessionManager;

/**
 * @author neo
 */
public class SiteManager {
    private final WebDirectory webDirectory = new WebDirectory();
    public final SessionManager sessionManager = new SessionManager();
    public final MessageManager messageManager = new MessageManager();
    public final TemplateManagerImpl templateManager = new TemplateManagerImpl(webDirectory, messageManager);

    public Controller staticContentController(String root) {
        return new StaticContentController(webDirectory, root);
    }
}
