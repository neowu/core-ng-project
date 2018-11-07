package core.framework.impl.module;

import core.framework.async.Task;
import core.framework.http.HTTPMethod;
import core.framework.impl.inject.BeanFactory;
import core.framework.impl.log.LogManager;
import core.framework.impl.web.HTTPServer;
import core.framework.impl.web.controller.ControllerClassValidator;
import core.framework.impl.web.controller.ControllerHolder;
import core.framework.impl.web.controller.ControllerInspector;
import core.framework.impl.web.management.DiagnosticController;
import core.framework.impl.web.management.PropertyController;
import core.framework.impl.web.route.PathPatternValidator;
import core.framework.impl.web.service.ErrorResponse;
import core.framework.impl.web.site.AJAXErrorResponse;
import core.framework.internal.stat.Stat;
import core.framework.util.ASCII;
import core.framework.util.Lists;
import core.framework.util.Maps;
import core.framework.web.Controller;
import core.framework.web.WebContext;
import core.framework.web.site.WebDirectory;

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
    public final LogManager logManager = new LogManager();
    public final HTTPServer httpServer;
    public final Stat stat = new Stat();
    protected final Map<String, Config> configs = Maps.newHashMap();
    private BackgroundTaskExecutor backgroundTask;

    public ModuleContext() {
        httpServer = new HTTPServer(logManager);
        beanFactory.bind(WebContext.class, null, httpServer.handler.webContext);
        beanFactory.bind(WebDirectory.class, null, httpServer.siteManager.webDirectory);
        startupHook.add(httpServer::start);
        shutdownHook.add(ShutdownHook.STAGE_0, timeout -> httpServer.shutdown());
        shutdownHook.add(ShutdownHook.STAGE_1, httpServer::awaitRequestCompletion);
        shutdownHook.add(ShutdownHook.STAGE_9, timeout -> httpServer.awaitTermination());

        httpServer.handler.beanMapperRegistry.register(ErrorResponse.class);
        httpServer.handler.beanMapperRegistry.register(AJAXErrorResponse.class);

        var diagnosticController = new DiagnosticController();
        route(HTTPMethod.GET, "/_sys/vm", diagnosticController::vm, true);
        route(HTTPMethod.GET, "/_sys/thread", diagnosticController::thread, true);
        route(HTTPMethod.GET, "/_sys/heap", diagnosticController::heap, true);
        var propertyController = new PropertyController(propertyManager);
        route(HTTPMethod.GET, "/_sys/property", propertyController, true);
    }

    public BackgroundTaskExecutor backgroundTask() {
        if (backgroundTask == null) {
            backgroundTask = new BackgroundTaskExecutor();
            startupHook.add(backgroundTask::start);
            shutdownHook.add(ShutdownHook.STAGE_2, timeoutInMs -> backgroundTask.shutdown());
            shutdownHook.add(ShutdownHook.STAGE_3, backgroundTask::awaitTermination);
        }
        return backgroundTask;
    }

    public final void route(HTTPMethod method, String path, Controller controller, boolean skipInterceptor) {
        new PathPatternValidator(path, true).validate();
        var inspector = new ControllerInspector(controller);
        new ControllerClassValidator(inspector.targetClass, inspector.targetMethod).validate();
        String action = "http:" + ASCII.toLowerCase(method.name()) + ":" + path;
        httpServer.handler.route.add(method, path, new ControllerHolder(controller, inspector.targetMethod, inspector.controllerInfo, action, skipInterceptor));
    }

    @SuppressWarnings("unchecked")
    public <T extends Config> T config(Class<T> configClass, String name) {
        String key = configClass.getCanonicalName() + ":" + name;   // not using computeIfAbsent, to avoid concurrent modification in nested call, e.g. httpConfig->publishAPIConfig->apiConfig
        T config = (T) configs.get(key);
        if (config == null) {
            try {
                config = configClass(configClass).getConstructor().newInstance();
                config.initialize(this, name);
                configs.put(key, config);
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                throw new Error(e);
            }
        }
        return config;
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
