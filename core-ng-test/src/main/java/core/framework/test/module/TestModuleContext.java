package core.framework.test.module;

import core.framework.async.Executor;
import core.framework.internal.inject.Key;
import core.framework.internal.log.LogManager;
import core.framework.internal.module.ModuleContext;
import core.framework.test.async.MockExecutor;
import core.framework.util.Maps;
import core.framework.util.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public class TestModuleContext extends ModuleContext {
    private final Logger logger = LoggerFactory.getLogger(TestModuleContext.class);
    private Map<Key, Object> overrideBindings = Maps.newHashMap();
    private Set<Key> appliedOverrideBindings = Sets.newHashSet();     // track overridden beans to detect duplicate binding

    public TestModuleContext() {
        super(new LogManager());
    }

    @Override
    public void initialize() {
        beanFactory.bind(Executor.class, null, new MockExecutor());
    }

    public <T> T getConfig(Class<T> configClass, String name) {
        @SuppressWarnings("unchecked")
        T config = (T) configs.get(configClass.getCanonicalName() + ":" + name);
        if (config == null) throw new Error(format("can not find config, configClass={}, name={}", configClass.getCanonicalName(), name));
        return config;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T> Class<T> configClass(Class<T> configClass) {    // try to find override config class for test context
        String testConfigClass = configClass.getPackageName() + ".Test" + configClass.getSimpleName();
        try {
            return (Class<T>) Class.forName(testConfigClass);
        } catch (ClassNotFoundException e) {
            return configClass;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T bind(Type type, String name, T instance) {
        var key = new Key(type, name);
        T overrideBinding = (T) overrideBindings.get(key);
        if (overrideBinding != null) {
            appliedOverrideBindings.add(key);
            logger.info("override binding, type={}, name={}", type.getTypeName(), name);
            return super.bind(type, name, overrideBinding);
        }
        return super.bind(type, name, instance);
    }

    @Override
    public void validate() {
        super.validate();
        validateOverrideBindings();
    }

    <T> T overrideBinding(Type type, String name, T instance) {
        Object previous = overrideBindings.put(new Key(type, name), instance);
        if (previous != null) throw new Error(format("found duplicate override binding, type={}, name={}, previous={}", type.getTypeName(), name, previous));
        return instance;
    }

    private void validateOverrideBindings() {
        Set<Key> notAppliedBindings = new HashSet<>(overrideBindings.keySet());
        notAppliedBindings.removeAll(appliedOverrideBindings);
        if (!notAppliedBindings.isEmpty())
            throw new Error("found unnecessary override bindings, please check test module, bindings=" + notAppliedBindings);
        overrideBindings = null;    // free not used object
        appliedOverrideBindings = null;
    }
}
