package core.framework.impl.web.site;

import core.framework.api.web.site.WebDirectory;
import core.framework.impl.web.session.SessionManager;

/**
 * @author neo
 */
public class SiteManager {
    public final WebDirectory webDirectory = new WebDirectory();
    public final SessionManager sessionManager = new SessionManager();
    public final MessageManager messageManager = new MessageManager();
    public final TemplateManagerImpl templateManager = new TemplateManagerImpl(webDirectory, messageManager);
}
