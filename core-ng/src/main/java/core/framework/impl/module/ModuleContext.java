package core.framework.impl.module;

import core.framework.async.Task;
import core.framework.http.HTTPMethod;
import core.framework.impl.inject.BeanFactory;
import core.framework.impl.log.DefaultLoggerFactory;
import core.framework.impl.log.LogManager;
import core.framework.impl.log.stat.Stat;
import core.framework.impl.web.HTTPServer;
import core.framework.impl.web.controller.ControllerClassValidator;
import core.framework.impl.web.controller.ControllerHolder;
import core.framework.impl.web.controller.ControllerInspector;
import core.framework.impl.web.management.MemoryUsageController;
import core.framework.impl.web.management.PropertyController;
import core.framework.impl.web.management.ThreadInfoController;
import core.framework.impl.web.route.PathPatternValidator;
import core.framework.util.ASCII;
import core.framework.util.Lists;
import core.framework.util.Maps;
import core.framework.web.Controller;
import core.framework.web.WebContext;
import core.framework.web.site.WebDirectory;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * @author neo
 */
public class ModuleContext {
    public final BeanFactory beanFactory = new BeanFactory();
    public final List<Task> startupHook = Lists.newArrayList();
    public final ShutdownHook shutdownHook = new ShutdownHook();
    public final PropertyManager propertyManager = new PropertyManager();
    public final HTTPServer httpServer;
    public final LogManager logManager;
    public final Stat stat = new Stat();
    protected final Map<String, Config> configs = Maps.newHashMap();
    private BackgroundTaskExecutor backgroundTask;

    public ModuleContext() {
        logManager = ((DefaultLoggerFactory) LoggerFactory.getILoggerFactory()).logManager;

        httpServer = new HTTPServer(logManager);
        beanFactory.bind(WebContext.class, null, httpServer.handler.webContext);
        beanFactory.bind(WebDirectory.class, null, httpServer.siteManager.webDirectory);
        startupHook.add(httpServer::start);
        shutdownHook.add(ShutdownHook.STAGE_0, timeout -> httpServer.shutdown());
        shutdownHook.add(ShutdownHook.STAGE_1, httpServer::awaitTermination);

        route(HTTPMethod.GET, "/_sys/memory", new MemoryUsageController(), true);
        var threadInfoController = new ThreadInfoController();
        route(HTTPMethod.GET, "/_sys/thread", threadInfoController::threadUsage, true);
        route(HTTPMethod.GET, "/_sys/thread-dump", threadInfoController::threadDump, true);
        var propertyController = new PropertyController(propertyManager);
        route(HTTPMethod.GET, "/_sys/property", propertyController, true);
    }

    public BackgroundTaskExecutor backgroundTask() {
        if (backgroundTask == null) {
            backgroundTask = new BackgroundTaskExecutor();
            startupHook.add(backgroundTask::start);
            shutdownHook.add(ShutdownHook.STAGE_0, timeout -> backgroundTask.shutdown());
            shutdownHook.add(ShutdownHook.STAGE_2, backgroundTask::awaitTermination);
        }
        return backgroundTask;
    }

    public void route(HTTPMethod method, String path, Controller controller, boolean skipInterceptor) {
        new PathPatternValidator(path).validate();
        var inspector = new ControllerInspector(controller);
        new ControllerClassValidator(inspector.targetClass, inspector.targetMethod).validate();
        String action = "http:" + ASCII.toLowerCase(method.name()) + ":" + path;
        httpServer.handler.route.add(method, path, new ControllerHolder(controller, inspector.targetMethod, inspector.controllerInfo, action, skipInterceptor));
    }

    @SuppressWarnings("unchecked")
    public <T extends Config> T config(Class<T> configClass, String name) {
        return (T) configs.computeIfAbsent(configClass.getCanonicalName() + ":" + name, key -> {
            try {
                T config = configClass(configClass).getConstructor().newInstance();
                config.initialize(this, name);
                return config;
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                throw new Error(e);
            }
        });
    }

    public void bind(Type type, String name, Object instance) {
        beanFactory.bind(type, name, instance);
    }

    public void validate() {
        configs.values().forEach(Config::validate);
    }

    protected <T> Class<T> configClass(Class<T> configClass) {
        return configClass;
    }
}
