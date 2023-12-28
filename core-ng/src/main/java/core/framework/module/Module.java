package core.framework.module;

import core.framework.async.Task;
import core.framework.internal.module.Config;
import core.framework.internal.module.ModuleContext;
import core.framework.internal.module.ShutdownHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Optional;

/**
 * @author neo
 */
public abstract class Module {
    private final Logger logger = LoggerFactory.getLogger(Module.class);

    protected ModuleContext context;

    protected void load(Module module) {
        logger.info("load module, module={}", module.getClass().getName());
        module.context = context;
        module.initialize();
    }

    public void onShutdown(Task task) {
        context.shutdownHook.add(ShutdownHook.STAGE_5, timeout -> task.execute());
    }

    public void onStartup(Task task) {
        // put all custom startup task on start stage
        context.startupHook.start.add(task);
    }

    public <T> T bind(Class<T> instanceClass) {
        T instance = context.beanFactory.create(instanceClass);
        return context.bind(instanceClass, null, instance);
    }

    public <T> T bind(T instance) {
        return bind(instance.getClass(), null, instance);
    }

    public <T> T bind(Class<? super T> instanceClass, T instance) {
        return bind(instanceClass, null, instance);
    }

    public <T> T bind(Type type, @Nullable String name, T instance) {
        context.beanFactory.inject(instance);
        return context.bind(type, name, instance);
    }

    public <T> T bean(Class<T> instanceClass) {
        return bean(instanceClass, null);
    }

    @SuppressWarnings("unchecked")
    public <T> T bean(Type type, @Nullable String name) {
        return (T) context.beanFactory.bean(type, name);
    }

    public void loadProperties(String classpath) {
        logger.info("load properties, classpath={}", classpath);
        context.propertyManager.properties.load(classpath);
    }

    public Optional<String> property(String key) {
        return context.property(key);
    }

    public String requiredProperty(String key) {
        return property(key).orElseThrow(() -> new Error("property key not found, key=" + key));
    }

    public LogConfig log() {
        return context.config(LogConfig.class, null);
    }

    public HTTPConfig http() {
        return context.config(HTTPConfig.class, null);
    }

    public WebSocketConfig ws() {
        return context.config(WebSocketConfig.class, null);
    }

    public SiteConfig site() {
        return context.config(SiteConfig.class, null);
    }

    public CacheConfig cache() {
        return context.config(CacheConfig.class, null);
    }

    public SchedulerConfig schedule() {
        return context.config(SchedulerConfig.class, null);
    }

    public APIConfig api() {
        return context.config(APIConfig.class, null);
    }

    public DBConfig db() {
        return db(null);
    }

    public DBConfig db(String name) {
        return context.config(DBConfig.class, name);
    }

    public RedisConfig redis() {
        return context.config(RedisConfig.class, null);
    }

    public RedisConfig redis(String name) {
        return context.config(RedisConfig.class, name);
    }

    public KafkaConfig kafka() {
        return kafka(null);
    }

    public KafkaConfig kafka(String name) {
        return context.config(KafkaConfig.class, name);
    }

    public <T extends Config> T config(Class<T> configClass) {
        return config(configClass, null);
    }

    public <T extends Config> T config(Class<T> configClass, String name) {
        return context.config(configClass, name);
    }

    public void highCPUUsageThreshold(double threshold) {
        context.collector.highCPUUsageThreshold = threshold;
    }

    public void highHeapUsageThreshold(double threshold) {
        context.collector.highHeapUsageThreshold = threshold;
    }

    // monitor java process VmRSS / cgroup ram limit
    public void highMemUsageThreshold(double threshold) {
        context.collector.highMemUsageThreshold = threshold;
    }

    protected abstract void initialize();
}
