package core.framework.internal.module;

import core.framework.async.Executor;
import core.framework.http.HTTPMethod;
import core.framework.internal.async.ExecutorImpl;
import core.framework.internal.async.ThreadPools;
import core.framework.internal.bean.BeanClassValidator;
import core.framework.internal.inject.BeanFactory;
import core.framework.internal.log.LogManager;
import core.framework.internal.stat.StatCollector;
import core.framework.internal.web.HTTPServer;
import core.framework.internal.web.HTTPServerConfig;
import core.framework.internal.web.HTTPServerMetrics;
import core.framework.internal.web.controller.ControllerClassValidator;
import core.framework.internal.web.controller.ControllerHolder;
import core.framework.internal.web.controller.ControllerInspector;
import core.framework.internal.web.route.PathPatternValidator;
import core.framework.internal.web.sys.APIController;
import core.framework.internal.web.sys.DiagnosticController;
import core.framework.internal.web.sys.PropertyController;
import core.framework.module.LambdaController;
import core.framework.util.ASCII;
import core.framework.util.Maps;
import core.framework.web.Controller;
import core.framework.web.SessionContext;
import core.framework.web.WebContext;
import core.framework.web.site.WebDirectory;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author neo
 */
public class ModuleContext {    // after core.framework.module.App.start(), entire ModuleContext will be GCed
    public final LogManager logManager;
    public final StartupHook startupHook = new StartupHook();
    public final ShutdownHook shutdownHook;
    public final ReadinessProbe probe = new ReadinessProbe();
    public final BeanFactory beanFactory = new BeanFactory();
    public final PropertyManager propertyManager = new PropertyManager();
    public final StatCollector collector = new StatCollector();
    public final HTTPServer httpServer;
    public final HTTPServerConfig httpServerConfig = new HTTPServerConfig();
    public final APIController apiController = new APIController();
    public final BeanClassValidator beanClassValidator = new BeanClassValidator();
    protected final Map<String, Config> configs = Maps.newHashMap();
    final PropertyValidator propertyValidator = new PropertyValidator();
    private BackgroundTaskExecutor backgroundTask;

    public ModuleContext(LogManager logManager) {
        this.logManager = logManager;
        shutdownHook = new ShutdownHook(logManager);
        httpServer = createHTTPServer();
    }

    // create builtin beans can be overridden by test context
    public void initialize() {
        var executor = new ExecutorImpl(ThreadPools.virtualThreadExecutor("executor-"), logManager, shutdownHook.shutdownTimeoutInNano);
        beanFactory.bind(Executor.class, null, executor);
        shutdownHook.add(ShutdownHook.STAGE_2, timeout -> executor.shutdown());
        shutdownHook.add(ShutdownHook.STAGE_3, executor::awaitTermination);

        var diagnosticController = new DiagnosticController();
        route(HTTPMethod.GET, "/_sys/vm", (LambdaController) diagnosticController::vm, true);
        route(HTTPMethod.GET, "/_sys/thread", (LambdaController) diagnosticController::thread, true);
        route(HTTPMethod.GET, "/_sys/thread/virtual", (LambdaController) diagnosticController::virtualThread, true);
        route(HTTPMethod.GET, "/_sys/heap", (LambdaController) diagnosticController::heap, true);
        route(HTTPMethod.GET, "/_sys/proc", (LambdaController) diagnosticController::proc, true);
        route(HTTPMethod.GET, "/_sys/property", new PropertyController(propertyManager), true);
        route(HTTPMethod.GET, "/_sys/api", (LambdaController) apiController::service, true);
        route(HTTPMethod.GET, "/_sys/api/message", (LambdaController) apiController::message, true);
    }

    private HTTPServer createHTTPServer() {
        var httpServer = new HTTPServer(logManager);
        beanFactory.bind(WebContext.class, null, httpServer.handler.webContext);
        beanFactory.bind(SessionContext.class, null, httpServer.siteManager.sessionManager);
        beanFactory.bind(WebDirectory.class, null, httpServer.siteManager.webDirectory);

        startupHook.start.add(() -> httpServer.start(httpServerConfig));
        shutdownHook.add(ShutdownHook.STAGE_0, timeout -> httpServer.shutdown());
        shutdownHook.add(ShutdownHook.STAGE_1, httpServer::awaitRequestCompletion);
        shutdownHook.add(ShutdownHook.STAGE_8, timeout -> httpServer.awaitTermination());

        collector.metrics.add(new HTTPServerMetrics(httpServer));
        return httpServer;
    }

    public BackgroundTaskExecutor backgroundTask() {
        if (backgroundTask == null) {
            var backgroundTask = new BackgroundTaskExecutor();
            startupHook.start.add(backgroundTask::start);
            shutdownHook.add(ShutdownHook.STAGE_2, timeout -> backgroundTask.shutdown());
            shutdownHook.add(ShutdownHook.STAGE_3, backgroundTask::awaitTermination);
            this.backgroundTask = backgroundTask;
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

    public <T extends Config> T config(Class<T> configClass, @Nullable String name) {
        String key = configClass.getCanonicalName() + ":" + name;   // not using computeIfAbsent, to avoid concurrent modification in nested call, e.g. httpConfig->publishAPIConfig->apiConfig
        @SuppressWarnings("unchecked")
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

    public <T> T bind(Type type, @Nullable String name, T instance) {
        beanFactory.bind(type, name, instance);
        return instance;
    }

    public void validate() {
        Set<String> keys = propertyManager.properties.keys();
        propertyValidator.validate(keys);

        for (Config config : configs.values()) {
            config.validate();
        }
    }

    public Optional<String> property(String key) {
        propertyValidator.usedProperties.add(key);
        return propertyManager.property(key);
    }

    protected <T> Class<T> configClass(Class<T> configClass) {
        return configClass;
    }
}
