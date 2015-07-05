package core.framework.test.inject;

import core.framework.api.util.Sets;
import core.framework.impl.inject.BeanFactory;
import core.framework.impl.inject.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.Set;

/**
 * @author neo
 */
public class TestBeanFactory extends BeanFactory {
    private final Logger logger = LoggerFactory.getLogger(TestBeanFactory.class);
    private final Set<Key> overrideBindings = Sets.newHashSet();

    @Override
    public void bind(Type type, String name, Object instance) {
        if (overrideBindings.contains(new Key(type, name))) {
            logger.info("skip bean binding, bean is overridden in test context, type={}, name={}", type.getTypeName(), name);
        } else {
            super.bind(type, name, instance);
        }
    }

    public <T> T overrideBinding(Type type, String name, T instance) {
        bind(type, name, instance);
        overrideBindings.add(new Key(type, name));
        return instance;
    }
}
