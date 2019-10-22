package core.framework.internal.web.site;

import core.framework.internal.web.session.SessionManager;
import core.framework.web.site.WebDirectory;

/**
 * @author neo
 */
public class SiteManager {
    public final WebDirectory webDirectory = new WebDirectory();
    public final SessionManager sessionManager = new SessionManager();
    public final MessageImpl message = new MessageImpl();
    public final TemplateManager templateManager = new TemplateManager(webDirectory, message);
}
