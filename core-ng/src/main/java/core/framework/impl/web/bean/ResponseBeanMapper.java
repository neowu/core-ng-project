package core.framework.impl.web.bean;

import core.framework.api.json.Property;
import core.framework.impl.reflect.GenericTypes;
import core.framework.impl.validate.Validator;
import core.framework.util.Maps;
import core.framework.util.Strings;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;

/**
 * @author neo
 */
public class ResponseBeanMapper {
    private final Map<Class<?>, BeanMapper<?>> mappers = Maps.newConcurrentHashMap();
    private final BeanClassNameValidator classNameValidator;

    public ResponseBeanMapper(BeanClassNameValidator classNameValidator) {
        this.classNameValidator = classNameValidator;
    }

    @SuppressWarnings("unchecked")
    public <T> byte[] toJSON(T bean) {
        if (bean instanceof Optional) {  // only support Optional<T> as response bean type
            Optional<?> optional = (Optional) bean;
            if (!optional.isPresent()) return Strings.bytes("null");
            T value = (T) optional.get();
            return toJSON((Class<T>) value.getClass(), value);
        } else {
            return toJSON((Class<T>) bean.getClass(), bean);
        }
    }

    private <T> byte[] toJSON(Class<T> beanClass, T bean) {
        BeanMapper<T> mapper = register(beanClass);
        mapper.validator.validate(bean);
        return mapper.writer.toJSON(bean);
    }

    @SuppressWarnings("unchecked")
    public <T> T fromJSON(Type responseType, byte[] body) {
        BeanMapper<T> mapper = register(responseType);
        T bean = mapper.reader.fromJSON(body);
        if (GenericTypes.isOptional(responseType)) {
            if (bean == null) return (T) Optional.empty();
            mapper.validator.validate(bean);
            return (T) Optional.of(bean);
        } else {
            mapper.validator.validate(bean);
            return bean;
        }
    }

    @SuppressWarnings("unchecked")
    public <T> BeanMapper<T> register(Type responseType) {
        Class<T> beanClass = GenericTypes.isOptional(responseType) ? (Class<T>) GenericTypes.optionalValueClass(responseType) : (Class<T>) GenericTypes.rawClass(responseType);
        return (BeanMapper<T>) mappers.computeIfAbsent(beanClass, type -> {
            new BeanClassValidator(beanClass, classNameValidator).validate();
            return new BeanMapper<>(beanClass, new Validator(beanClass, field -> field.getDeclaredAnnotation(Property.class).name()));
        });
    }
}
