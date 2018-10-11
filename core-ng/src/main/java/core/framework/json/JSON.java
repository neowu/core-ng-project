package core.framework.json;

import com.fasterxml.jackson.databind.JavaType;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;

import static core.framework.internal.json.JSONMapper.OBJECT_MAPPER;

/**
 * @author neo
 */
public final class JSON {
    public static Object fromJSON(Type instanceType, String json) {
        try {
            JavaType javaType = OBJECT_MAPPER.getTypeFactory().constructType(instanceType);
            return OBJECT_MAPPER.readValue(json, javaType);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <T> T fromJSON(Class<T> instanceClass, String json) {
        try {
            return OBJECT_MAPPER.readValue(json, instanceClass);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String toJSON(Object instance) {
        try {
            return OBJECT_MAPPER.writeValueAsString(instance);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <T extends Enum<?>> T fromEnumValue(Class<T> valueClass, String jsonValue) {
        return OBJECT_MAPPER.convertValue(jsonValue, valueClass);
    }

    public static <T extends Enum<?>> String toEnumValue(T value) {
        return OBJECT_MAPPER.convertValue(value, String.class);
    }
}
