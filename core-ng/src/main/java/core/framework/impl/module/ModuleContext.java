package core.framework.impl.module;

import core.framework.api.concurrent.AsyncExecutor;
import core.framework.api.http.HTTPMethod;
import core.framework.api.util.Lists;
import core.framework.api.util.Properties;
import core.framework.api.web.WebContext;
import core.framework.api.web.site.TemplateManager;
import core.framework.impl.cache.CacheManager;
import core.framework.impl.concurrent.Executor;
import core.framework.impl.inject.BeanFactory;
import core.framework.impl.inject.ShutdownHook;
import core.framework.impl.log.DefaultLoggerFactory;
import core.framework.impl.log.LogManager;
import core.framework.impl.scheduler.Scheduler;
import core.framework.impl.web.ControllerHolder;
import core.framework.impl.web.HTTPServer;
import core.framework.impl.web.management.HealthCheckController;
import core.framework.impl.web.management.MemoryUsageController;
import core.framework.impl.web.management.ThreadInfoController;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author neo
 */
public class ModuleContext {
    public final boolean test;

    public final BeanFactory beanFactory;
    public final List<Runnable> startupHook = Lists.newArrayList();
    public final ShutdownHook shutdownHook = new ShutdownHook();
    public final Properties properties = new Properties();

    public final HTTPServer httpServer;
    public final Executor executor;
    public Scheduler scheduler;
    public CacheManager cacheManager;
    public final QueueManager queueManager = new QueueManager();
    public final LogManager logManager;

    public ModuleContext(BeanFactory beanFactory, boolean test) {
        this.beanFactory = beanFactory;
        this.test = test;

        this.logManager = ((DefaultLoggerFactory) LoggerFactory.getILoggerFactory()).logManager;
        if (!test) {
            shutdownHook.add(logManager::shutdown);
        }

        httpServer = new HTTPServer(logManager);
        beanFactory.bind(WebContext.class, null, httpServer.handler.webContext);
        beanFactory.bind(TemplateManager.class, null, httpServer.siteManager.templateManager);
        if (!test) {
            startupHook.add(httpServer::start);
        }
        executor = new Executor(logManager);
        shutdownHook.add(executor::shutdown);

        beanFactory.bind(AsyncExecutor.class, null, new AsyncExecutor(executor, logManager));

        if (!test) {
            httpServer.handler.route.add(HTTPMethod.GET, "/health-check", new ControllerHolder(new HealthCheckController(), true));
            httpServer.handler.route.add(HTTPMethod.GET, "/monitor/memory", new ControllerHolder(new MemoryUsageController(), true));
            ThreadInfoController threadInfoController = new ThreadInfoController();
            httpServer.handler.route.add(HTTPMethod.GET, "/monitor/thread", new ControllerHolder(threadInfoController::threadUsage, true));
            httpServer.handler.route.add(HTTPMethod.GET, "/monitor/thread-dump", new ControllerHolder(threadInfoController::threadDump, true));
        }
    }
}
