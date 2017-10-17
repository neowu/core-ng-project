package core.framework.test.inject;

import core.framework.impl.inject.BeanFactory;
import core.framework.impl.inject.Key;
import core.framework.util.Exceptions;
import core.framework.util.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

/**
 * @author neo
 */
public final class TestBeanFactory extends BeanFactory {
    private final Logger logger = LoggerFactory.getLogger(TestBeanFactory.class);
    private final Set<Key> overrideBindings = Sets.newHashSet();
    private final Set<Key> skippedBindings = Sets.newHashSet();     // track overridden beans to detect duplicate binding

    @Override
    public void bind(Type type, String name, Object instance) {
        Key key = new Key(type, name);

        if (overrideBindings.contains(key)) {
            if (skippedBindings.contains(key)) throw Exceptions.error("found duplicate bean, type={}, name={}", type.getTypeName(), name);
            skippedBindings.add(key);
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

    public void validateOverrideBindings() {
        Set<Key> notAppliedBindings = new HashSet<>(overrideBindings);
        notAppliedBindings.removeAll(skippedBindings);
        if (!notAppliedBindings.isEmpty())
            throw Exceptions.error("found unnecessary override bindings, please check test module, bindings={}", notAppliedBindings);
    }
}
