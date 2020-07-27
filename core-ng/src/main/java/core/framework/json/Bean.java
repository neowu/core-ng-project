package core.framework.json;

import core.framework.internal.json.JSONClassValidator;
import core.framework.internal.validate.Validator;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;

import static core.framework.internal.json.JSONMapper.OBJECT_MAPPER;

/**
 * @author neo
 */
public final class Bean {
    private static final Map<Class<?>, Validator<?>> VALIDATORS = new HashMap<>(); // always requires register beanClass during startup, so not be thread safe

    public static void register(Class<?> beanClass) {
        VALIDATORS.compute(beanClass, (key, value) -> {
            if (value != null) throw new Error("bean class is already registered, beanClass=" + key.getCanonicalName());
            new JSONClassValidator(key).validate();
            return new Validator<>(key);
        });
    }

    public static <T> T fromJSON(Class<T> beanClass, String json) {
        Validator<T> validator = validator(beanClass);
        try {
            T instance = OBJECT_MAPPER.readValue(json, beanClass);
            validator.validate(instance, false);
            return instance;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <T> String toJSON(T bean) {
        @SuppressWarnings("unchecked")
        Validator<T> validator = validator((Class<T>) bean.getClass());
        validator.validate(bean, false);
        try {
            return OBJECT_MAPPER.writeValueAsString(bean);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static <T> Validator<T> validator(Class<T> beanClass) {
        @SuppressWarnings("unchecked")
        Validator<T> validator = (Validator<T>) VALIDATORS.get(beanClass);
        if (validator == null) throw new Error("bean class is not registered, beanClass=" + beanClass.getCanonicalName());
        return validator;
    }
}
