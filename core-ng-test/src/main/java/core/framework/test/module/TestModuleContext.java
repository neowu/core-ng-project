package core.framework.test.module;

import core.framework.impl.inject.Key;
import core.framework.impl.module.ModuleContext;
import core.framework.util.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public class TestModuleContext extends ModuleContext {
    private final Logger logger = LoggerFactory.getLogger(TestModuleContext.class);
    private final Set<Key> overrideBindings = Sets.newHashSet();
    private final Set<Key> skippedBindings = Sets.newHashSet();     // track overridden beans to detect duplicate binding

    @SuppressWarnings("unchecked")
    public <T> T getConfig(Class<T> configClass, String name) {
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

    @Override
    public void bind(Type type, String name, Object instance) {
        var key = new Key(type, name);
        if (overrideBindings.contains(key)) {
            if (skippedBindings.contains(key)) throw new Error(format("found duplicate bean, type={}, name={}", type.getTypeName(), name));
            skippedBindings.add(key);
            logger.info("skip bean binding, bean is overridden in test context, type={}, name={}", type.getTypeName(), name);
        } else {
            super.bind(type, name, instance);
        }
    }

    @Override
    public void validate() {
        super.validate();
        validateOverrideBindings();
    }

    <T> T overrideBinding(Type type, String name, T instance) {
        bind(type, name, instance);
        overrideBindings.add(new Key(type, name));
        return instance;
    }

    private void validateOverrideBindings() {
        Set<Key> notAppliedBindings = new HashSet<>(overrideBindings);
        notAppliedBindings.removeAll(skippedBindings);
        if (!notAppliedBindings.isEmpty())
            throw new Error(format("found unnecessary override bindings, please check test module, bindings={}", notAppliedBindings));
    }
}
