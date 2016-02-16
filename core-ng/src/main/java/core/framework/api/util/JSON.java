package core.framework.api.util;

import com.fasterxml.jackson.databind.JavaType;
import core.framework.impl.json.JSONMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;

/**
 * @author neo
 */
public final class JSON {
    public static <T> T fromJSON(Type instanceType, String json) {
        try {
            JavaType javaType = JSONMapper.OBJECT_MAPPER.getTypeFactory().constructType(instanceType);
            return JSONMapper.OBJECT_MAPPER.readValue(json, javaType);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <T> T fromJSON(Class<T> instanceType, String json) {
        try {
            return JSONMapper.OBJECT_MAPPER.readValue(json, instanceType);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String toJSON(Object instance) {
        try {
            return JSONMapper.OBJECT_MAPPER.writeValueAsString(instance);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
