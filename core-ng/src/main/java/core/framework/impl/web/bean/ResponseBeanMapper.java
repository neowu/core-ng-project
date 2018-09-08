package core.framework.impl.web.bean;

import core.framework.impl.reflect.GenericTypes;
import core.framework.util.Strings;

import java.lang.reflect.Type;
import java.util.Optional;

/**
 * @author neo
 */
public class ResponseBeanMapper {
    private final BeanMapperRegistry registry;

    public ResponseBeanMapper(BeanMapperRegistry registry) {
        this.registry = registry;
    }

    @SuppressWarnings("unchecked")
    public byte[] toJSON(Object bean) {
        if (bean instanceof Optional) {  // only support Optional<T> as response bean type
            Optional<?> optional = (Optional) bean;
            if (!optional.isPresent()) return Strings.bytes("null");
            Object value = optional.get();
            return registry.toJSON((Class<Object>) value.getClass(), value);
        } else {
            return registry.toJSON((Class<Object>) bean.getClass(), bean);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T fromJSON(Type responseType, byte[] body) {
        BeanMapper<?> mapper = register(responseType);
        Object bean = mapper.reader.fromJSON(body);
        if (GenericTypes.isOptional(responseType)) {
            if (bean == null) return (T) Optional.empty();
            mapper.validator.validate(bean, false);
            return (T) Optional.of(bean);
        } else {
            mapper.validator.validate(bean, false);
            return (T) bean;
        }
    }

    public BeanMapper<?> register(Type responseType) {
        Class<?> beanClass = GenericTypes.isOptional(responseType) ? GenericTypes.optionalValueClass(responseType) : GenericTypes.rawClass(responseType);
        return registry.register(beanClass);
    }
}
