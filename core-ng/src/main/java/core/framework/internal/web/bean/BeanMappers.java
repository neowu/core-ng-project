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

    public <T> void register(Class<T> beanClass, BeanClassNameValidator beanClassNameValidator) {
        if (!mappers.containsKey(beanClass)) {
            new BeanClassValidator(beanClass, beanClassNameValidator).validate();
            mappers.put(beanClass, new BeanMapper<>(beanClass));
        }
    }

    <T> BeanMapper<T> mapper(Class<T> beanClass) {
        @SuppressWarnings("unchecked")
        BeanMapper<T> mapper = (BeanMapper<T>) mappers.get(beanClass);
        if (mapper == null) throw new Error("bean class is not registered, please use http().bean() to register, class=" + beanClass.getCanonicalName());
        return mapper;
    }
}
