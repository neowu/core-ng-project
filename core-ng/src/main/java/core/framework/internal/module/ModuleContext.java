package core.framework.internal.module;

import core.framework.async.Task;
import core.framework.http.HTTPMethod;
import core.framework.internal.bean.BeanClassValidator;
import core.framework.internal.inject.BeanFactory;
import core.framework.internal.json.JSONReader;
import core.framework.internal.json.JSONWriter;
import core.framework.internal.log.LogManager;
import core.framework.internal.stat.StatCollector;
import core.framework.internal.validate.Validator;
import core.framework.internal.web.HTTPServer;
import core.framework.internal.web.controller.ControllerClassValidator;
import core.framework.internal.web.controller.ControllerHolder;
import core.framework.internal.web.controller.ControllerInspector;
import core.framework.internal.web.management.DiagnosticController;
import core.framework.internal.web.management.PropertyController;
import core.framework.internal.web.route.PathPatternValidator;
import core.framework.module.LambdaController;
import core.framework.util.ASCII;
import core.framework.util.Lists;
import core.framework.util.Maps;
import core.framework.web.Controller;
import core.framework.web.SessionContext;
import core.framework.web.WebContext;
import core.framework.web.site.WebDirectory;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author neo
 */
public class ModuleContext {
    public final LogManager logManager;
    public final ShutdownHook shutdownHook;
    public final PropertyManager propertyManager = new PropertyManager();
    public final HTTPServer httpServer;
    public final StatCollector collector = new StatCollector();
    public List<Task> startupHook = Lists.newArrayList();
    public BeanFactory beanFactory = new BeanFactory();
    public ServiceRegistry serviceRegistry = new ServiceRegistry();
    public BeanClassValidator beanClassValidator = new BeanClassValidator();
    protected Map<String, Config> configs = Maps.newHashMap();
    PropertyValidator propertyValidator = new PropertyValidator();
    private BackgroundTaskExecutor backgroundTask;

    public ModuleContext(LogManager logManager) {
        this.logManager = logManager;
        shutdownHook = new ShutdownHook(logManager);

        httpServer = new HTTPServer(logManager);

        beanFactory.bind(WebContext.class, null, httpServer.handler.webContext);
        beanFactory.bind(SessionContext.class, null, httpServer.siteManager.sessionManager);
        beanFactory.bind(WebDirectory.class, null, httpServer.siteManager.webDirectory);

        startupHook.add(httpServer::start);
        shutdownHook.add(ShutdownHook.STAGE_0, timeout -> httpServer.shutdown());
        shutdownHook.add(ShutdownHook.STAGE_1, httpServer::awaitRequestCompletion);
        shutdownHook.add(ShutdownHook.STAGE_9, timeout -> httpServer.awaitTermination());

        var diagnosticController = new DiagnosticController();
        route(HTTPMethod.GET, "/_sys/vm", (LambdaController) diagnosticController::vm, true);
        route(HTTPMethod.GET, "/_sys/thread", (LambdaController) diagnosticController::thread, true);
        route(HTTPMethod.GET, "/_sys/heap", (LambdaController) diagnosticController::heap, true);
        route(HTTPMethod.GET, "/_sys/property", new PropertyController(propertyManager), true);
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
    public <T extends Config> T config(Class<T> configClass, @Nullable String name) {
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

    // free object not used anymore
    public void cleanup() {
        propertyValidator = null;
        serviceRegistry = null;
        beanClassValidator = null;
        beanFactory = null;
        configs = null;
        startupHook = null;

        Validator.clearCache();
        JSONReader.clearCache();
        JSONWriter.clearCache();
    }

    public Optional<String> property(String key) {
        propertyValidator.usedProperties.add(key);
        return propertyManager.property(key);
    }

    protected <T> Class<T> configClass(Class<T> configClass) {
        return configClass;
    }
}
