package core.framework.test.module;

import core.framework.impl.inject.BeanFactory;
import core.framework.impl.module.ModuleContext;
import core.framework.util.Exceptions;

/**
 * @author neo
 */
public class TestModuleContext extends ModuleContext {
    public TestModuleContext(BeanFactory beanFactory) {
        super(beanFactory);
    }

    @SuppressWarnings("unchecked")
    public <T> T getConfig(Class<T> configClass, String name) {
        T config = (T) configs.get(configClass.getCanonicalName() + ":" + name);
        if (config == null) throw Exceptions.error("can not find config, configClass={}, name={}", configClass.getCanonicalName(), name);
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
}
