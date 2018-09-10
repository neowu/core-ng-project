package core.framework.impl.web.bean;

import core.framework.api.json.Property;
import core.framework.impl.reflect.Classes;
import core.framework.impl.validate.Validator;
import core.framework.util.Maps;

import java.util.Map;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public class BeanMapperRegistry {
    public final Map<Class<?>, BeanMapper<?>> beanMappers = Maps.newConcurrentHashMap();
    final Map<String, Class<?>> beanClasses = Maps.newConcurrentHashMap();

    @SuppressWarnings("unchecked")
    public <T> BeanMapper<T> register(Class<T> beanClass) {
        return (BeanMapper<T>) beanMappers.computeIfAbsent(beanClass, type -> {
            new BeanClassValidator(beanClass, this).validate();
            return new BeanMapper<>(beanClass, new Validator(beanClass, field -> field.getDeclaredAnnotation(Property.class).name()));
        });
    }

    <T> byte[] toJSON(Class<T> beanClass, T bean) {
        BeanMapper<T> mapper = register(beanClass);
        mapper.validator.validate(bean, false);
        return mapper.writer.toJSON(bean);
    }

    void validateBeanClassName(Class<?> beanClass) {
        beanClasses.compute(Classes.className(beanClass), (key, previous) -> {
            if (previous != null && !previous.equals(beanClass))
                throw new Error(format("found bean class with duplicate name which can be confusing, please use different class name, previousClass={}, class={}", previous.getCanonicalName(), beanClass.getCanonicalName()));
            return beanClass;
        });
    }
}
