package core.framework.impl.web.bean;

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

    <T> byte[] toJSON(Class<T> beanClass, T bean) {
        BeanMapper<T> mapper = mapper(beanClass);
        mapper.validator.validate(bean, false);
        return mapper.mapper.toJSON(bean);
    }

    <T> BeanMapper<T> mapper(Class<T> beanClass) {
        @SuppressWarnings("unchecked")
        BeanMapper<T> mapper = (BeanMapper<T>) mappers.get(beanClass);
        if (mapper == null) throw new Error("bean class is not registered, please use http().bean() to register, class=" + beanClass.getCanonicalName());
        return mapper;
    }
}
