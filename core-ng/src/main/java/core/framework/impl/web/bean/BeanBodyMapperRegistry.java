package core.framework.impl.web.bean;

import core.framework.impl.reflect.Classes;
import core.framework.internal.validate.Validator;
import core.framework.util.Maps;

import java.util.Map;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public class BeanBodyMapperRegistry {
    public final Map<Class<?>, BeanBodyMapper<?>> beanMappers = Maps.newConcurrentHashMap();
    final Map<String, Class<?>> beanClasses = Maps.newConcurrentHashMap();

    @SuppressWarnings("unchecked")
    public <T> BeanBodyMapper<T> register(Class<T> beanClass) {
        return (BeanBodyMapper<T>) beanMappers.computeIfAbsent(beanClass, type -> {
            new BeanBodyClassValidator(beanClass, this).validate();
            return new BeanBodyMapper<>(beanClass, new Validator(beanClass));
        });
    }

    <T> byte[] toJSON(Class<T> beanClass, T bean) {
        BeanBodyMapper<T> mapper = register(beanClass);
        mapper.validator.validate(bean, false);
        return mapper.mapper.toJSON(bean);
    }

    void validateBeanClassName(Class<?> beanClass) {
        beanClasses.compute(Classes.className(beanClass), (key, previous) -> {
            if (previous != null && !previous.equals(beanClass))
                throw new Error(format("found bean class with duplicate name which can be confusing, please use different class name, previousClass={}, class={}", previous.getCanonicalName(), beanClass.getCanonicalName()));
            return beanClass;
        });
    }
}
