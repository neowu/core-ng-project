package core.framework.json;

import com.fasterxml.jackson.databind.JavaType;
import core.framework.api.json.Property;
import core.framework.impl.validate.Validator;
import core.framework.impl.validate.type.JSONClassValidator;
import core.framework.util.Maps;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.util.Map;

import static core.framework.internal.json.JSONMapper.OBJECT_MAPPER;

/**
 * @author neo
 */
public final class JSON {
    private static final Map<Class<?>, Validator> VALIDATORS = Maps.newConcurrentHashMap();

    public static Object fromJSON(Type instanceType, String json) {
        try {
            JavaType javaType = OBJECT_MAPPER.getTypeFactory().constructType(instanceType);
            return OBJECT_MAPPER.readValue(json, javaType);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // JSON is designed to be flexible, allow to serialize/deserialize any types including null, here only validate only if it's app defined class,
    // and not include List<T> or Map<String, T>
    public static <T> T fromJSON(Class<T> instanceClass, String json) {
        try {
            T instance = OBJECT_MAPPER.readValue(json, instanceClass);
            if (instance != null && !isJDKClass(instanceClass)) {
                validate(instanceClass, instance);
            }
            return instance;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String toJSON(Object instance) {
        if (instance != null && !isJDKClass(instance.getClass())) {
            validate(instance.getClass(), instance);
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(instance);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static boolean isJDKClass(Class<?> instanceClass) {
        return instanceClass.getPackageName().startsWith("java");
    }

    private static void validate(Class<?> instanceClass, Object instance) {
        Validator validator = VALIDATORS.computeIfAbsent(instanceClass, key -> {
            new JSONClassValidator(instanceClass).validate();
            return new Validator(key, field -> field.getDeclaredAnnotation(Property.class).name());
        });
        validator.validate(instance, false);
    }

    public static <T extends Enum<?>> T fromEnumValue(Class<T> valueClass, String jsonValue) {
        return OBJECT_MAPPER.convertValue(jsonValue, valueClass);
    }

    public static <T extends Enum<?>> String toEnumValue(T value) {
        return OBJECT_MAPPER.convertValue(value, String.class);
    }
}
