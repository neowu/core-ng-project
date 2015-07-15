package core.framework.api;

import core.framework.api.module.APIConfig;
import core.framework.api.module.CacheConfig;
import core.framework.api.module.DBConfig;
import core.framework.api.module.HTTPConfig;
import core.framework.api.module.LogConfig;
import core.framework.api.module.QueueConfig;
import core.framework.api.module.RouteConfig;
import core.framework.api.module.SchedulerConfig;
import core.framework.api.module.SiteConfig;
import core.framework.api.util.Exceptions;
import core.framework.impl.module.ModuleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author neo
 */
public abstract class Module {
    private final Logger logger = LoggerFactory.getLogger(Module.class);

    ModuleContext context;

    protected void load(Module module) {
        logger.info("load module, module={}", module.getClass().getName());
        module.context = context;
        module.initialize();
    }

    public void onShutdown(Runnable runnable) {
        context.shutdownHook.add(runnable);
    }

    public void onStartup(Runnable runnable) {
        context.startupHook.add(runnable);
    }

    public <T> T bind(Class<T> instanceClass) {
        T instance = context.beanFactory.create(instanceClass);
        return bind(instanceClass, null, instance);
    }

    public <T> T bind(T instance) {
        return bind(instance.getClass(), null, instance);
    }

    public <T> T bind(Class<? super T> type, T instance) {
        return bind(type, null, instance);
    }

    public <T> T bind(Type type, String name, T instance) {
        return bind(type, name, () -> instance);
    }

    public <T> T bind(Type type, String name, Supplier<T> supplier) {
        T instance = supplier.get();
        context.beanFactory.bind(type, name, instance);
        return instance;
    }

    public <T> T bean(Type instanceType, String name) {
        return context.beanFactory.bean(instanceType, name);
    }

    public <T> T bean(Class<T> instanceType) {
        return bean(instanceType, null);
    }

    public void loadProperties(String path) {
        logger.info("load properties, path={}", path);
        context.properties.load(path);
    }

    public Optional<String> property(String key) {
        return context.properties.get(key);
    }

    public String requiredProperty(String key) {
        return property(key).orElseThrow(() -> Exceptions.error("property key not found, key={}", key));
    }

    public LogConfig log() {
        return new LogConfig(context);
    }

    public HTTPConfig http() {
        return context.httpServer;
    }

    public RouteConfig route() {
        return context.httpServer;
    }

    public SiteConfig site() {
        return new SiteConfig(context);
    }

    public CacheConfig cache() {
        return new CacheConfig(context);
    }

    public QueueConfig queue() {
        return new QueueConfig(context);
    }

    public SchedulerConfig schedule() {
        return new SchedulerConfig(context);
    }

    public APIConfig api() {
        return new APIConfig(context);
    }

    public DBConfig db() {
        return db(null);
    }

    public DBConfig db(String name) {
        return new DBConfig(context, name);
    }

    protected abstract void initialize();
}
