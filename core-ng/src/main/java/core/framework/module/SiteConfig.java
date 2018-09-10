package core.framework.module;

import core.framework.http.HTTPMethod;
import core.framework.impl.module.Config;
import core.framework.impl.module.ModuleContext;
import core.framework.impl.web.http.IPAccessControl;
import core.framework.impl.web.management.APIController;
import core.framework.impl.web.site.StaticContentController;
import core.framework.impl.web.site.StaticDirectoryController;
import core.framework.impl.web.site.StaticFileController;
import core.framework.impl.web.site.WebSecurityInterceptor;
import core.framework.web.site.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public class SiteConfig extends Config {
    private final Logger logger = LoggerFactory.getLogger(SiteConfig.class);
    boolean messageConfigured;
    private WebSecurityInterceptor webSecurityInterceptor;
    private ModuleContext context;

    @Override
    protected void initialize(ModuleContext context, String name) {
        this.context = context;
    }

    public SessionConfig session() {
        return context.config(SessionConfig.class, null);
    }

    public CDNConfig cdn() {
        return new CDNConfig(context);
    }

    public void message(List<String> paths, String... languages) {
        if (messageConfigured) {
            throw new Error("site message can only be configured once and must before adding template");
        }
        messageConfigured = true;

        context.beanFactory.bind(Message.class, null, context.httpServer.siteManager.message);
        context.httpServer.siteManager.message.load(paths, languages);
    }

    public void template(String path, Class<?> modelClass) {
        messageConfigured = true; // can not configure message() after adding template
        context.httpServer.siteManager.templateManager.add(path, modelClass);
    }

    public StaticContentConfig staticContent(String path) {
        logger.info("add static content path, path={}", path);
        Path contentPath = context.httpServer.siteManager.webDirectory.path(path);
        if (!Files.exists(contentPath, LinkOption.NOFOLLOW_LINKS)) throw new Error(format("path does not exist, path={}", path));

        StaticContentController controller;
        if (Files.isDirectory(contentPath)) {
            controller = new StaticDirectoryController(contentPath);
            context.route(HTTPMethod.GET, path + "/:path(*)", controller, true);
        } else {
            controller = new StaticFileController(contentPath);
            context.route(HTTPMethod.GET, path, controller, true);
        }
        return new StaticContentConfig(controller);
    }

    public WebSecurityConfig security() {
        if (webSecurityInterceptor == null) {
            webSecurityInterceptor = new WebSecurityInterceptor();
            context.httpServer.handler.interceptors.add(webSecurityInterceptor);
        }
        return new WebSecurityConfig(webSecurityInterceptor);
    }

    public void publishAPI(String... cidrs) {
        APIConfig config = context.config(APIConfig.class, null);

        logger.info("publish typescript api definition, cidrs={}", Arrays.toString(cidrs));
        context.route(HTTPMethod.GET, "/_sys/api", new APIController(config.serviceInterfaces, config.beanClasses, new IPAccessControl(cidrs)), true);
    }
}
