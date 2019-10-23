package core.framework.internal.web.bean;

import core.framework.internal.bean.BeanClassNameValidator;
import core.framework.internal.reflect.GenericTypes;
import core.framework.util.Strings;

import java.lang.reflect.Type;
import java.util.Optional;

/**
 * @author neo
 */
public class ResponseBeanMapper {
    private final BeanMappers beanMappers;

    public ResponseBeanMapper(BeanMappers beanMappers) {
        this.beanMappers = beanMappers;
    }

    @SuppressWarnings("unchecked")
    public byte[] toJSON(Object bean) {
        if (bean instanceof Optional) {  // only support Optional<T> as response bean type
            Optional<?> optional = (Optional) bean;
            if (optional.isEmpty()) return Strings.bytes("null");
            Object value = optional.get();
            BeanMapper<Object> mapper = beanMappers.mapper((Class<Object>) value.getClass());
            mapper.validator.validate(value, false);
            return mapper.mapper.toJSON(value);
        } else if (bean.getClass().getPackageName().startsWith("java")) {   // provide better error message for developer, rather than return class is not registered message
            throw new Error("response body class must be bean class, class=" + bean.getClass().getCanonicalName());
        } else {
            BeanMapper<Object> mapper = beanMappers.mapper((Class<Object>) bean.getClass());
            mapper.validator.validate(bean, false);
            return mapper.mapper.toJSON(bean);
        }
    }

    public Object fromJSON(Type responseType, byte[] body) {
        if (void.class == responseType) return null;

        Class<?> beanClass = beanClass(responseType);
        BeanMapper<?> mapper = beanMappers.mapper(beanClass);
        Object bean = mapper.mapper.fromJSON(body);
        if (GenericTypes.isOptional(responseType)) {
            if (bean == null) return Optional.empty();
            mapper.validator.validate(bean, false);
            return Optional.of(bean);
        } else {
            mapper.validator.validate(bean, false);
            return bean;
        }
    }

    public void register(Type responseType, BeanClassNameValidator beanClassNameValidator) {
        beanMappers.register(beanClass(responseType), beanClassNameValidator);
    }

    private Class<?> beanClass(Type responseType) {
        return GenericTypes.isOptional(responseType) ? GenericTypes.optionalValueClass(responseType) : GenericTypes.rawClass(responseType);
    }
}
