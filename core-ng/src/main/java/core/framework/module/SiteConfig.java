package core.framework.module;

import core.framework.http.HTTPMethod;
import core.framework.internal.module.Config;
import core.framework.internal.module.ModuleContext;
import core.framework.internal.web.http.IPv4Ranges;
import core.framework.internal.web.site.MessageImpl;
import core.framework.internal.web.site.StaticContentController;
import core.framework.internal.web.site.StaticDirectoryController;
import core.framework.internal.web.site.StaticFileController;
import core.framework.internal.web.site.WebSecurityInterceptor;
import core.framework.web.site.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;

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
        context.httpServer.handler.requestParser.logSiteHeaders = true;
    }

    public SessionConfig session() {
        return context.config(SessionConfig.class, null);
    }

    public CDNConfig cdn() {
        return new CDNConfig(context);
    }

    public void message(List<String> paths, String... languages) {
        if (messageConfigured) {
            throw new Error("site().message() can only be configured once and must before adding template");
        }
        messageConfigured = true;

        context.httpServer.siteManager.message.load(paths, languages);
        context.beanFactory.bind(Message.class, null, message(context.httpServer.siteManager.message));
    }

    Message message(MessageImpl message) {
        return message;
    }

    public void template(String path, Class<?> modelClass) {
        messageConfigured = true; // can not configure message() after adding template
        context.httpServer.siteManager.templateManager.add(path, modelClass);
    }

    // this is only for POC or local testing, in cloud env, all static content should be served from LB + AWS S3/Google Storage/Azure Storage Account
    public StaticContentConfig staticContent(String path) {
        logger.info("add static content path, path={}", path);
        Path contentPath = context.httpServer.siteManager.webDirectory.path(path);
        if (!Files.exists(contentPath, LinkOption.NOFOLLOW_LINKS)) throw new Error("path does not exist, path=" + path);

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
            context.httpServerConfig.interceptors.add(webSecurityInterceptor);
        }
        return new WebSecurityConfig(webSecurityInterceptor);
    }

    public void allowAPI(List<String> cidrs) {
        logger.info("allow /_sys/api access, cidrs={}", cidrs);
        context.apiController.accessControl.allow = new IPv4Ranges(cidrs);
    }
}
