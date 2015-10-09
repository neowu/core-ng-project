package core.framework.test.inject;

import core.framework.api.util.Sets;
import core.framework.impl.inject.BeanFactory;
import core.framework.impl.inject.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.Set;
import java.util.function.Supplier;

/**
 * @author neo
 */
public final class TestBeanFactory extends BeanFactory {
    private final Logger logger = LoggerFactory.getLogger(TestBeanFactory.class);
    private final Set<Key> overrideBindings = Sets.newHashSet();

    @Override
    public <T> T bindSupplier(Type type, String name, Supplier<T> supplier) {
        if (overrideBindings.contains(new Key(type, name))) {
            logger.info("skip bean binding, bean is overridden in test context, type={}, name={}", type.getTypeName(), name);
            return bean(type, name);
        } else {
            return super.bindSupplier(type, name, supplier);
        }
    }

    public <T> T overrideBinding(Type type, String name, T instance) {
        bind(type, name, instance);
        overrideBindings.add(new Key(type, name));
        return instance;
    }
}
