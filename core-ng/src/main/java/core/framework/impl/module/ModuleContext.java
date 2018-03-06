package core.framework.impl.module;

import core.framework.async.Executor;
import core.framework.http.HTTPMethod;
import core.framework.impl.async.ExecutorImpl;
import core.framework.impl.inject.BeanFactory;
import core.framework.impl.inject.ShutdownHook;
import core.framework.impl.log.DefaultLoggerFactory;
import core.framework.impl.log.LogManager;
import core.framework.impl.log.stat.Stat;
import core.framework.impl.web.ControllerActionBuilder;
import core.framework.impl.web.ControllerClassValidator;
import core.framework.impl.web.ControllerHolder;
import core.framework.impl.web.ControllerInspector;
import core.framework.impl.web.HTTPServer;
import core.framework.impl.web.management.MemoryUsageController;
import core.framework.impl.web.management.PropertyController;
import core.framework.impl.web.management.ThreadInfoController;
import core.framework.impl.web.route.PathPatternValidator;
import core.framework.util.Lists;
import core.framework.web.Controller;
import core.framework.web.WebContext;
import core.framework.web.site.WebDirectory;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author neo
 */
public final class ModuleContext {
    public final BeanFactory beanFactory;
    public final List<Runnable> startupHook = Lists.newArrayList();
    public final ShutdownHook shutdownHook = new ShutdownHook();
    public final PropertyManager propertyManager = new PropertyManager();
    public final HTTPServer httpServer;
    public final LogManager logManager;
    public final MockFactory mockFactory;
    public final Stat stat = new Stat();
    public final Config config = new Config();
    private BackgroundTaskExecutor backgroundTask;

    public ModuleContext(BeanFactory beanFactory, MockFactory mockFactory) {
        this.beanFactory = beanFactory;
        this.mockFactory = mockFactory;

        logManager = ((DefaultLoggerFactory) LoggerFactory.getILoggerFactory()).logManager;

        httpServer = new HTTPServer(logManager);
        beanFactory.bind(WebContext.class, null, httpServer.handler.webContext);
        beanFactory.bind(WebDirectory.class, null, httpServer.siteManager.webDirectory);
        startupHook.add(httpServer::start);
        shutdownHook.add(httpServer::stop);

        Executor executor;
        if (!isTest()) {
            executor = new ExecutorImpl(logManager);
            shutdownHook.add(((ExecutorImpl) executor)::stop);
        } else {
            executor = mockFactory.create(Executor.class);
        }
        beanFactory.bind(Executor.class, null, executor);

        route(HTTPMethod.GET, "/_sys/memory", new MemoryUsageController(), true);
        ThreadInfoController threadInfoController = new ThreadInfoController();
        route(HTTPMethod.GET, "/_sys/thread", threadInfoController::threadUsage, true);
        route(HTTPMethod.GET, "/_sys/thread-dump", threadInfoController::threadDump, true);
        PropertyController propertyController = new PropertyController(propertyManager);
        route(HTTPMethod.GET, "/_sys/property", propertyController, true);
    }

    public BackgroundTaskExecutor backgroundTask() {
        if (backgroundTask == null) {
            backgroundTask = new BackgroundTaskExecutor();
            startupHook.add(backgroundTask::start);
            shutdownHook.add(backgroundTask::stop);
        }
        return backgroundTask;
    }

    public void route(HTTPMethod method, String path, Controller controller, boolean skipInterceptor) {
        new PathPatternValidator(path).validate();
        ControllerInspector inspector = new ControllerInspector(controller);
        new ControllerClassValidator(inspector.targetClass, inspector.targetMethod).validate();
        String action = new ControllerActionBuilder(method, path).build();
        httpServer.handler.route.add(method, path, new ControllerHolder(controller, inspector.targetMethod, inspector.controllerInfo, action, skipInterceptor));
    }

    public boolean isTest() {
        return mockFactory != null;
    }
}
