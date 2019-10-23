package core.framework.internal.module;

import core.framework.api.web.service.QueryParam;
import core.framework.async.Task;
import core.framework.http.HTTPMethod;
import core.framework.internal.inject.BeanFactory;
import core.framework.internal.log.LogManager;
import core.framework.internal.stat.Stat;
import core.framework.internal.web.HTTPServer;
import core.framework.internal.web.HTTPServerMetrics;
import core.framework.internal.web.bean.BeanMappers;
import core.framework.internal.web.bean.RequestBeanMapper;
import core.framework.internal.web.controller.ControllerClassValidator;
import core.framework.internal.web.controller.ControllerHolder;
import core.framework.internal.web.controller.ControllerInspector;
import core.framework.internal.web.management.DiagnosticController;
import core.framework.internal.web.management.PropertyController;
import core.framework.internal.web.management.ServiceController;
import core.framework.internal.web.management.ServiceResponse;
import core.framework.internal.web.route.PathPatternValidator;
import core.framework.internal.web.service.ErrorResponse;
import core.framework.internal.web.site.AJAXErrorResponse;
import core.framework.module.LambdaController;
import core.framework.util.ASCII;
import core.framework.util.Lists;
import core.framework.util.Maps;
import core.framework.web.Controller;
import core.framework.web.SessionContext;
import core.framework.web.WebContext;
import core.framework.web.site.WebDirectory;

import java.lang.reflect.Field;
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
    public final BeanFactory beanFactory = new BeanFactory();
    public final List<Task> startupHook = Lists.newArrayList();
    public final ShutdownHook shutdownHook;
    public final PropertyManager propertyManager = new PropertyManager();
    public final HTTPServer httpServer;
    public final Stat stat = new Stat();
    public final ServiceRegistry serviceRegistry = new ServiceRegistry();
    protected final Map<String, Config> configs = Maps.newHashMap();
    PropertyValidator propertyValidator = new PropertyValidator();
    private BackgroundTaskExecutor backgroundTask;

    public ModuleContext(LogManager logManager) {
        this.logManager = logManager;
        shutdownHook = new ShutdownHook(logManager);

        httpServer = new HTTPServer(logManager);
        stat.metrics.add(new HTTPServerMetrics(httpServer));

        beanFactory.bind(WebContext.class, null, httpServer.handler.webContext);
        beanFactory.bind(SessionContext.class, null, httpServer.siteManager.sessionManager);
        beanFactory.bind(WebDirectory.class, null, httpServer.siteManager.webDirectory);

        startupHook.add(httpServer::start);
        shutdownHook.add(ShutdownHook.STAGE_0, timeout -> httpServer.shutdown());
        shutdownHook.add(ShutdownHook.STAGE_1, httpServer::awaitRequestCompletion);
        shutdownHook.add(ShutdownHook.STAGE_9, timeout -> httpServer.awaitTermination());

        bean(ErrorResponse.class);
        bean(AJAXErrorResponse.class);

        var diagnosticController = new DiagnosticController();
        route(HTTPMethod.GET, "/_sys/vm", (LambdaController) diagnosticController::vm, true);
        route(HTTPMethod.GET, "/_sys/thread", (LambdaController) diagnosticController::thread, true);
        route(HTTPMethod.GET, "/_sys/heap", (LambdaController) diagnosticController::heap, true);
        route(HTTPMethod.GET, "/_sys/property", new PropertyController(propertyManager), true);
        route(HTTPMethod.GET, "/_sys/service", new ServiceController(serviceRegistry), true);
        bean(ServiceResponse.class);
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

    // register http body bean and query param bean
    public final void bean(Class<?> beanClass) {
        if (isQueryParamBean(beanClass)) {
            RequestBeanMapper requestBeanMapper = httpServer.handler.requestBeanMapper;
            if (requestBeanMapper.queryParamMappers.containsKey(beanClass)) {
                throw new Error("query param bean class is already registered or referred by service interface, class=" + beanClass.getCanonicalName());
            }
            requestBeanMapper.registerQueryParamBean(beanClass, serviceRegistry.beanClassNameValidator);
        } else {
            BeanMappers beanMappers = httpServer.handler.beanMappers;
            if (beanMappers.mappers.containsKey(beanClass))
                throw new Error("bean class is already registered or referred by service interface, class=" + beanClass.getCanonicalName());
            beanMappers.register(beanClass, serviceRegistry.beanClassNameValidator);
        }
    }

    private boolean isQueryParamBean(Class<?> beanClass) {
        Field[] fields = beanClass.getDeclaredFields();
        if (fields.length == 0) return false;
        return fields[0].isAnnotationPresent(QueryParam.class);
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

    public <T> T bind(Type type, String name, T instance) {
        beanFactory.bind(type, name, instance);
        return instance;
    }

    public void validate() {
        Set<String> keys = propertyManager.properties.keys();
        propertyValidator.validate(keys);
        propertyValidator = null;   // free object not used anymore
        serviceRegistry.beanClassNameValidator = null;

        configs.values().forEach(Config::validate);
    }

    public Optional<String> property(String key) {
        propertyValidator.usedProperties.add(key);
        return propertyManager.property(key);
    }

    protected <T> Class<T> configClass(Class<T> configClass) {
        return configClass;
    }
}
