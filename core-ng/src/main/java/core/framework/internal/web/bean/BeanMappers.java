package core.framework.internal.web.bean;

import core.framework.internal.bean.BeanClassNameValidator;
import core.framework.internal.bean.BeanClassValidator;
import core.framework.util.Maps;

import java.util.Map;

/**
 * @author neo
 */
public class BeanMappers {
    public final Map<Class<?>, BeanMapper<?>> mappers = Maps.newHashMap();

    public <T> BeanMapper<T> register(Class<T> beanClass, BeanClassNameValidator beanClassNameValidator) {
        @SuppressWarnings("unchecked")
        BeanMapper<T> mapper = (BeanMapper<T>) mappers.get(beanClass);
        if (!mappers.containsKey(beanClass)) {
            new BeanClassValidator(beanClass, beanClassNameValidator).validate();
            mapper = new BeanMapper<>(beanClass);
            mappers.put(beanClass, mapper);
        }
        return mapper;
    }

    public <T> BeanMapper<T> mapper(Class<T> beanClass) {
        @SuppressWarnings("unchecked")
        BeanMapper<T> mapper = (BeanMapper<T>) mappers.get(beanClass);
        if (mapper == null) {
            if (beanClass.getPackageName().startsWith("java")) {   // provide better error message for developer, rather than return class is not registered message
                throw new Error("bean class must not be java built-in class, class=" + beanClass.getCanonicalName());
            }
            throw new Error("bean class is not registered, please use http().bean() to register, class=" + beanClass.getCanonicalName());
        }
        return mapper;
    }
}
