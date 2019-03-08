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
    private static final Map<Class<?>, Validator> VALIDATORS = new HashMap<>(); // always requires register beanClass during startup, so not be thread safe

    public static void register(Class<?> beanClass) {
        if (VALIDATORS.containsKey(beanClass)) throw new Error("bean class is already registered, beanClass=" + beanClass.getCanonicalName());
        new JSONClassValidator(beanClass).validate();
        VALIDATORS.put(beanClass, Validator.of(beanClass));
    }

    public static <T> T fromJSON(Class<T> beanClass, String json) {
        Validator validator = validator(beanClass);
        try {
            T instance = OBJECT_MAPPER.readValue(json, beanClass);
            validator.validate(instance, false);
            return instance;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String toJSON(Object bean) {
        Validator validator = validator(bean.getClass());
        validator.validate(bean, false);
        try {
            return OBJECT_MAPPER.writeValueAsString(bean);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static <T> Validator validator(Class<T> beanClass) {
        Validator validator = VALIDATORS.get(beanClass);
        if (validator == null) throw new Error("bean class is not registered, beanClass=" + beanClass.getCanonicalName());
        return validator;
    }
}
